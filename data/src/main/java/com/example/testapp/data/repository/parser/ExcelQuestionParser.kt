package com.example.testapp.data.repository.parser

import com.example.testapp.data.repository.ImportedQuestionPayload
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.util.FILL_PART_DELIMITER
import com.example.testapp.domain.util.guessQuestionType
import com.example.testapp.domain.util.splitFillAnswerParts
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFPicture
import org.apache.poi.xssf.usermodel.XSSFShape
import java.io.File

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelQuestionParser @Inject constructor() : QuestionFileParser {

    companion object {
        val IMPORTED_FILL_SPACE_REGEX = Regex("[\\u0020\\t\\u00A0\\u3000]{2,}")
        val IMPORTED_FILL_BLANK_REGEX = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")
        const val INLINE_BLANK_PLACEHOLDER = "【   】"
    }

    private fun quizStorageDir(): File {
        return File("/data/data/com.example.testapp/files/quiz/").apply {
            if (!exists()) mkdirs()
        }
    }

    private data class EmbeddedExcelImages(
        val stemImagesByRow: Map<Int, List<String>>,
        val answerImagesByRow: Map<Int, List<String>>
    )

    private data class ExcelAnswerPartSlot(
        val order: Int,
        val answerIndex: Int? = null,
        val categoryIndex: Int? = null,
        val scoreIndex: Int? = null
    )

    private data class ExcelHeaderSchema(
        val headerRowIndex: Int,
        val contentIndex: Int,
        val typeIndex: Int?,
        val answerIndex: Int?,
        val explanationIndex: Int?,
        val deepSeekIndex: Int?,
        val sparkIndex: Int?,
        val baiduIndex: Int?,
        val noteIndex: Int?,
        val optionIndices: List<Int>,
        val answerPartSlots: List<ExcelAnswerPartSlot>,
        val stemImageIndices: List<Int> = emptyList()
    )

    override fun parse(file: File, originFileName: String): List<ImportedQuestionPayload> {
        val questions = mutableListOf<ImportedQuestionPayload>()
        val f = DataFormatter()
        var workbookSuggestsShortAnswer = false

        try {
            if (file.length() == 0L) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_EMPTY_KEY, listOf(file.name))

            WorkbookFactory.create(file).use { workbook ->
                if (workbook.numberOfSheets == 0) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_SHEETS_KEY, listOf(file.name))
                val sheet = workbook.getSheetAt(0)
                workbookSuggestsShortAnswer = detectWorkbookShortAnswerHint(sheet, f)
                if (sheet.physicalNumberOfRows <= 1) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_VALID_DATA_KEY, listOf(file.name))
                val headerSchema = detectHeaderSchema(sheet, f)
                val rows = if (headerSchema != null) {
                    sheet.drop(headerSchema.headerRowIndex + 1)
                } else {
                    sheet.drop(1)
                }

                val embeddedImages = extractEmbeddedExcelImages(
                    sheet = sheet,
                    originFileName = originFileName,
                    stemColumnIndices = headerSchema?.stemImageIndices?.toSet().orEmpty(),
                    answerColumnIndex = headerSchema?.answerIndex
                )

                for (row in rows) {
                    val q = if (headerSchema != null) {
                        parseRowByHeader(row, headerSchema, f, workbookSuggestsShortAnswer, originFileName)?.let { payload ->
                            val columnImages = headerSchema.stemImageIndices
                                .mapNotNull { idx -> cellText(row, idx, f).takeIf { it.isNotBlank() } }
                            val rowStemImages = embeddedImages.stemImagesByRow[row.rowNum].orEmpty()
                            val rowAnswerImages = embeddedImages.answerImagesByRow[row.rowNum].orEmpty()
                            val allStemImages = columnImages + rowStemImages
                            var question = payload.question
                            if (allStemImages.isNotEmpty()) {
                                question = question.copy(stemImages = allStemImages)
                            }
                            if (rowAnswerImages.isNotEmpty()) {
                                question = question.copy(
                                    answer = buildDrawingAnswerWithImages(question.answer, rowAnswerImages)
                                )
                            }
                            payload.copy(question = question)
                        }
                    } else {
                        parseFillTemplateRow(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                            ?: parseLegacyCalculationRow(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                            ?: parseRowStyle1(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                            ?: parseRowStyle3(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                            ?: parseRowStyle2(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                    }
                    if (q != null) questions.add(q)
                }
            }
        } catch (e: LocalizedException) {
            throw e
        } catch (t: Throwable) {
            val msg = (t.message ?: t::class.simpleName ?: "解析失败").take(80)
            throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_PARSE_KEY, listOf(msg))
        }

        if (questions.isEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_VALID_DATA_KEY, listOf(file.name))
        return questions
    }

    private fun cellText(row: Row, index: Int, f: DataFormatter): String {
        return row.getCell(index)?.let { f.formatCellValue(it) }?.trim().orEmpty()
    }

    private fun contentCellText(row: Row, index: Int, f: DataFormatter): String {
        return row.getCell(index)?.let { f.formatCellValue(it) }?.trimStart().orEmpty()
    }

    private fun rowValues(row: Row, f: DataFormatter): List<String> {
        val lastCell = row.lastCellNum.toInt().coerceAtLeast(0)
        return (0 until lastCell).map { index -> cellText(row, index, f) }
    }

    private fun normalizeHeader(text: String): String {
        return text.trim().replace(Regex("\\s+"), "")
    }

    private fun isOptionHeader(header: String): Boolean {
        val normalized = normalizeHeader(header)
        return normalized.startsWith("选项") || normalized.matches(Regex("^[A-GＡ-Ｇ][、.)）]?.*"))
    }

    private fun isAnswerPartHeader(header: String): Boolean {
        return normalizeHeader(header).startsWith("答案")
    }

    private fun extractIndexedHeaderNumber(header: String, vararg prefixes: String): Int? {
        val normalized = normalizeHeader(header)
        for (prefix in prefixes) {
            val match = Regex("^${Regex.escape(normalizeHeader(prefix))}(\\d+)$").matchEntire(normalized)
            if (match != null) {
                return match.groupValues[1].toIntOrNull()
            }
        }
        return null
    }

    private fun normalizeScoreLabel(rawScore: String): String? {
        val trimmed = rawScore.trim()
        if (trimmed.isBlank()) return null
        val score = Regex("10|[1-9]").find(trimmed)?.value?.toIntOrNull() ?: return null
        return "${score}分"
    }

    private fun buildAnnotatedAnswerPart(answerText: String, category: String, scoreText: String): String {
        val trimmedAnswer = answerText.trim()
        if (trimmedAnswer.isBlank()) return ""
        return buildString {
            append(trimmedAnswer)
            category.trim().takeIf { it.isNotBlank() }?.let {
                append("【")
                append(it)
                append("】")
            }
            normalizeScoreLabel(scoreText)?.let {
                append("【")
                append(it)
                append("】")
            }
        }
    }

    private fun matchesAnyHeader(value: String, vararg aliases: String): Boolean {
        val normalized = normalizeHeader(value)
        return aliases.any { normalized == normalizeHeader(it) }
    }

    private fun detectWorkbookShortAnswerHint(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        f: DataFormatter
    ): Boolean {
        val sampleText = sheet.take(6)
            .flatMap { row -> rowValues(row, f) }
            .joinToString(" ")
        return Regex("简答题|简答|问答题|综合题|综合|论述题|论述|计算题|计算分析题|计算|绘图题|绘图|画图题|画图|作图题|作图").containsMatchIn(sampleText)
    }

    private fun detectHeaderSchema(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        f: DataFormatter
    ): ExcelHeaderSchema? {
        for (row in sheet) {
            val values = rowValues(row, f)
            if (values.isEmpty()) continue

            var contentIndex: Int? = null
            var typeIndex: Int? = null
            val directAnswerIndices = mutableListOf<Int>()
            var explanationIndex: Int? = null
            var deepSeekIndex: Int? = null
            var sparkIndex: Int? = null
            var baiduIndex: Int? = null
            var noteIndex: Int? = null
            val optionIndices = mutableListOf<Int>()
            val answerPartSlots = linkedMapOf<Int, ExcelAnswerPartSlot>()
            val stemImageIndices = mutableListOf<Int>()

            values.forEachIndexed { index, value ->
                when {
                    matchesAnyHeader(value, "题干图片", "题干图", "图片题干", "附图", "图示") -> {
                        stemImageIndices.add(index)
                    }
                    matchesAnyHeader(value, "题干", "题目", "内容", "试题", "问题", "题目内容", "题干内容") -> {
                        if (contentIndex == null) {
                            contentIndex = index
                        } else {
                            stemImageIndices.add(index)
                        }
                    }
                    matchesAnyHeader(value, "题干2", "题干3", "题干图片2", "题干图片3") -> stemImageIndices.add(index)
                    matchesAnyHeader(value, "题型", "类型", "题类", "类别", "试题类型") -> typeIndex = index
                    matchesAnyHeader(value, "答案", "正确答案", "参考答案", "标准答案", "参考答案及评分标准", "答案及解析", "评分标准") -> directAnswerIndices += index
                    matchesAnyHeader(value, "解析", "说明", "解释", "解题过程") -> explanationIndex = index
                    matchesAnyHeader(value, "DeepSeek解析", "DeepSeek", "deepseek", "deepseek解析") -> deepSeekIndex = index
                    matchesAnyHeader(value, "Spark解析", "Spark", "spark", "spark解析", "讯飞星火解析") -> sparkIndex = index
                    matchesAnyHeader(value, "百度AI解析", "百度解析", "Baidu解析", "baidu", "baidu解析") -> baiduIndex = index
                    matchesAnyHeader(value, "笔记", "备注", "Note", "note") -> noteIndex = index
                    else -> {
                        if (isOptionHeader(value)) optionIndices += index

                        val answerOrder = extractIndexedHeaderNumber(value, "答案", "正确答案", "参考答案")
                        if (answerOrder != null) {
                            val current = answerPartSlots[answerOrder]
                            answerPartSlots[answerOrder] = ExcelAnswerPartSlot(
                                order = answerOrder,
                                answerIndex = index,
                                categoryIndex = current?.categoryIndex,
                                scoreIndex = current?.scoreIndex
                            )
                        }

                        val categoryOrder = extractIndexedHeaderNumber(value, "答案分类", "分类", "类别")
                        if (categoryOrder != null) {
                            val current = answerPartSlots[categoryOrder]
                            answerPartSlots[categoryOrder] = ExcelAnswerPartSlot(
                                order = categoryOrder,
                                answerIndex = current?.answerIndex,
                                categoryIndex = index,
                                scoreIndex = current?.scoreIndex
                            )
                        }

                        val scoreOrder = extractIndexedHeaderNumber(value, "答案评分", "答案分值", "评分", "分值")
                        if (scoreOrder != null) {
                            val current = answerPartSlots[scoreOrder]
                            answerPartSlots[scoreOrder] = ExcelAnswerPartSlot(
                                order = scoreOrder,
                                answerIndex = current?.answerIndex,
                                categoryIndex = current?.categoryIndex,
                                scoreIndex = index
                            )
                        }

                        if (isAnswerPartHeader(value) && answerOrder == null && categoryOrder == null && scoreOrder == null) {
                            val fallbackOrder = answerPartSlots.size + 1
                            answerPartSlots[fallbackOrder] = ExcelAnswerPartSlot(order = fallbackOrder, answerIndex = index)
                        }
                    }
                }
            }

            val answerIndex = directAnswerIndices.singleOrNull()
            if (directAnswerIndices.size > 1) {
                directAnswerIndices.forEachIndexed { offset, answerColumnIndex ->
                    val order = offset + 1
                    val current = answerPartSlots[order]
                    answerPartSlots[order] = ExcelAnswerPartSlot(
                        order = order,
                        answerIndex = answerColumnIndex,
                        categoryIndex = current?.categoryIndex,
                        scoreIndex = current?.scoreIndex
                    )
                }
            }

            if (contentIndex != null && (typeIndex != null || answerIndex != null || optionIndices.isNotEmpty() || answerPartSlots.isNotEmpty())) {
                return ExcelHeaderSchema(
                    headerRowIndex = row.rowNum,
                    contentIndex = contentIndex!!,
                    typeIndex = typeIndex,
                    answerIndex = answerIndex,
                    explanationIndex = explanationIndex,
                    deepSeekIndex = deepSeekIndex,
                    sparkIndex = sparkIndex,
                    baiduIndex = baiduIndex,
                    noteIndex = noteIndex,
                    optionIndices = optionIndices.sorted(),
                    answerPartSlots = answerPartSlots.values.sortedBy { it.order },
                    stemImageIndices = stemImageIndices.sorted()
                )
            }
        }
        return null
    }

    private fun resolveType(
        rawType: String, answer: String, content: String,
        workbookSuggestsShortAnswer: Boolean
    ): String {
        val isShortAnswerCandidate = workbookSuggestsShortAnswer &&
            !IMPORTED_FILL_BLANK_REGEX.containsMatchIn(content) &&
            !IMPORTED_FILL_SPACE_REGEX.containsMatchIn(content)

        if (QuestionTypes.isTextResponse(rawType)) return rawType.trim().ifBlank { "简答题" }

        if (rawType.isNotBlank()) {
            if (isShortAnswerCandidate && QuestionTypes.isInlineBlank(rawType)) {
                return "简答题"
            }
            return when {
                QuestionTypes.isSingle(rawType) -> QuestionTypes.SINGLE
                QuestionTypes.isMulti(rawType) -> QuestionTypes.MULTI
                QuestionTypes.isJudge(rawType) -> QuestionTypes.JUDGE
                QuestionTypes.isInlineBlank(rawType) -> QuestionTypes.BLANK
                else -> rawType
            }
        }
        return if (isShortAnswerCandidate) "简答题" else guessQuestionType(answer)
    }

    private fun normalizeOptionsForType(type: String, options: List<String>): List<String> {
        return if (QuestionTypes.isJudge(type) && options.isEmpty()) {
            listOf("对", "错")
        } else {
            options
        }
    }

    private fun normalizeImportedFillContent(content: String, answer: String): String {
        val answerParts = splitFillAnswerParts(answer)
        if (answerParts.isEmpty()) return content

        val existingBlankCount = IMPORTED_FILL_BLANK_REGEX.findAll(content).count()
        if (existingBlankCount >= answerParts.size) return content

        val matches = IMPORTED_FILL_SPACE_REGEX.findAll(content).toList()
        val blanksNeeded = (answerParts.size - existingBlankCount).coerceAtLeast(0)
        if (blanksNeeded == 0) return content

        val builder = StringBuilder(content.length + blanksNeeded * 4)
        var lastIndex = 0
        var replacedCount = 0
        for (match in matches) {
            builder.append(content, lastIndex, match.range.first)
            if (replacedCount < blanksNeeded) {
                builder.append("____")
                replacedCount += 1
            } else {
                builder.append(match.value)
            }
            lastIndex = match.range.last + 1
        }
        builder.append(content.substring(lastIndex))

        var normalized = builder.toString()
        val missingBlankCount = (answerParts.size - IMPORTED_FILL_BLANK_REGEX.findAll(normalized).count()).coerceAtLeast(0)
        if (missingBlankCount == 0) return normalized

        val blankMatches = IMPORTED_FILL_BLANK_REGEX.findAll(normalized).toList()
        if (blankMatches.isNotEmpty()) {
            val lastBlank = blankMatches.last()
            val suffix = buildString(missingBlankCount * 5) {
                repeat(missingBlankCount) { append("、____") }
            }
            normalized = buildString(normalized.length + suffix.length) {
                append(normalized, 0, lastBlank.range.last + 1)
                append(suffix)
                append(normalized.substring(lastBlank.range.last + 1))
            }
            return normalized
        }

        val trailingPunctuation = Regex("[。．；;，,、：:！？!?）)】\\]]+$")
        val punctuationMatch = trailingPunctuation.find(normalized)
        val appendedBlanks = buildString(missingBlankCount * 5) {
            repeat(missingBlankCount) {
                append(if (isEmpty()) "____" else "、____")
            }
        }

        return if (punctuationMatch != null) {
            buildString(normalized.length + appendedBlanks.length + 1) {
                append(normalized, 0, punctuationMatch.range.first)
                if (isNotEmpty() && this[lastIndex] != ' ') append(' ')
                append(appendedBlanks)
                append(normalized.substring(punctuationMatch.range.first))
            }
        } else {
            normalized + " " + appendedBlanks
        }
    }

    private fun parseRowByHeader(
        row: Row, schema: ExcelHeaderSchema, f: DataFormatter,
        workbookSuggestsShortAnswer: Boolean, originFileName: String
    ): ImportedQuestionPayload? {
        val content = contentCellText(row, schema.contentIndex, f)
        if (content.isBlank()) return null
        if (normalizeHeader(content) in setOf("题干", "题目", "内容")) return null

        val rawType = schema.typeIndex?.let { cellText(row, it, f) }.orEmpty()
        val explanation = schema.explanationIndex?.let { cellText(row, it, f) }.orEmpty()
        val answerParts = schema.answerPartSlots
            .map { slot ->
                buildAnnotatedAnswerPart(
                    answerText = slot.answerIndex?.let { cellText(row, it, f) }.orEmpty(),
                    category = slot.categoryIndex?.let { cellText(row, it, f) }.orEmpty(),
                    scoreText = slot.scoreIndex?.let { cellText(row, it, f) }.orEmpty()
                )
            }
            .filter { it.isNotBlank() }
        val answer = when {
            answerParts.isNotEmpty() -> {
                if (answerParts.size == 1) answerParts.first() else answerParts.joinToString(FILL_PART_DELIMITER)
            }
            schema.answerIndex != null -> cellText(row, schema.answerIndex, f)
            else -> ""
        }
        val parsedOptions = schema.optionIndices
            .map { index -> cellText(row, index, f) }
            .filter { it.isNotBlank() }
            .filterNot { normalizeHeader(it) == normalizeHeader(rawType) }

        val type = resolveType(rawType, answer, content, workbookSuggestsShortAnswer)
        val normalizedContent = if (QuestionTypes.isInlineBlank(type)) {
            normalizeImportedFillContent(content, answer)
        } else {
            content
        }
        val options = normalizeOptionsForType(type, parsedOptions)
        val hasValidOptions = options.isNotEmpty() ||
            QuestionTypes.isFill(type) ||
            QuestionTypes.isJudge(type) ||
            QuestionTypes.isTextResponse(type)

        val isDrawingOrTextResponse = QuestionTypes.isTextResponse(type)
        val hasValidAnswer = answer.isNotBlank() || isDrawingOrTextResponse
        val finalAnswer = if (answer.isBlank() && isDrawingOrTextResponse) "略" else answer

        return if (normalizedContent.isNotBlank() && hasValidOptions && hasValidAnswer) {
            ImportedQuestionPayload(
                question = Question(
                    id = 0, content = normalizedContent, type = type,
                    options = options, answer = finalAnswer, explanation = explanation,
                    isFavorite = false, isWrong = false, fileName = originFileName
                ),
                deepSeekAnalysis = schema.deepSeekIndex?.let { cellText(row, it, f) }.orEmpty(),
                sparkAnalysis = schema.sparkIndex?.let { cellText(row, it, f) }.orEmpty(),
                baiduAnalysis = schema.baiduIndex?.let { cellText(row, it, f) }.orEmpty(),
                note = schema.noteIndex?.let { cellText(row, it, f) }.orEmpty()
            )
        } else null
    }

    private fun parseLegacyCalculationRow(
        row: Row, f: DataFormatter, workbookSuggestsShortAnswer: Boolean, originFileName: String
    ): Question? {
        if (!workbookSuggestsShortAnswer) return null
        val values = rowValues(row, f)
        if (values.count { it.isNotBlank() } < 2) return null
        if (values.any { normalizeHeader(it) in setOf("题干", "题目", "内容", "试题", "问题", "答案", "参考答案", "评分标准") }) {
            return null
        }

        val first = values.getOrNull(0).orEmpty()
        val firstLooksLikeNumber = first.matches(Regex("\\d+(?:[.、．)]|\\.0)?"))
        val contentIndex = if (firstLooksLikeNumber && values.getOrNull(1).orEmpty().isNotBlank()) 1 else 0
        val content = values.getOrNull(contentIndex).orEmpty()
        if (content.isBlank()) return null

        val tail = values.drop(contentIndex + 1).filter { it.isNotBlank() }
        if (tail.isEmpty()) return null

        val rawTypeIndex = tail.indexOfFirst { QuestionTypes.isCalculation(it) || QuestionTypes.isTextResponse(it) }
        val rawType = tail.getOrNull(rawTypeIndex).orEmpty()
        val answerCandidates = if (rawTypeIndex >= 0) {
            tail.filterIndexed { index, _ -> index != rawTypeIndex }
        } else {
            tail
        }
        val answer = answerCandidates.firstOrNull { it.length > 1 } ?: answerCandidates.firstOrNull().orEmpty()
        if (answer.isBlank()) return null

        val explanation = answerCandidates.dropWhile { it != answer }.drop(1).joinToString("\n")
        val type = if (QuestionTypes.isTextResponse(rawType)) rawType else "计算题"

        return Question(
            id = 0, content = content, type = type,
            options = emptyList(), answer = answer, explanation = explanation,
            isFavorite = false, isWrong = false, fileName = originFileName
        )
    }

    private fun parseRowStyle1(
        row: Row, f: DataFormatter, workbookSuggestsShortAnswer: Boolean, originFileName: String
    ): Question? {
        val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
        val rawType = row.getCell(1)?.let { f.formatCellValue(it) } ?: ""
        val parsedOptions = (2..8)
            .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
            .filter { it.isNotBlank() }
        val explanation = row.getCell(9)?.let { f.formatCellValue(it) } ?: ""
        val answer = row.getCell(10)?.let { f.formatCellValue(it) } ?: ""
        val type = resolveType(rawType, answer, content, workbookSuggestsShortAnswer)
        val options = normalizeOptionsForType(type, parsedOptions)
        val hasValidOptions = options.isNotEmpty() || QuestionTypes.isFill(type)
        return if (content.isNotBlank() && hasValidOptions && answer.isNotBlank()) {
            Question(
                id = 0, content = content, type = type,
                options = options, answer = answer, explanation = explanation,
                isFavorite = false, isWrong = false, fileName = originFileName
            )
        } else null
    }

    private fun parseFillTemplateRow(
        row: Row, f: DataFormatter, workbookSuggestsShortAnswer: Boolean, originFileName: String
    ): Question? {
        val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
        val rawType = row.getCell(2)?.let { f.formatCellValue(it) } ?: ""
        if (content.isBlank() || !QuestionTypes.isInlineBlank(rawType)) return null

        val answers = (3 until row.lastCellNum)
            .mapNotNull { index -> row.getCell(index)?.let(f::formatCellValue) }
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (answers.isEmpty()) return null

        val answer = if (answers.size == 1) answers.first() else answers.joinToString(FILL_PART_DELIMITER)
        val normalizedContent = normalizeImportedFillContent(content, answer)

        return Question(
            id = 0, content = normalizedContent, type = QuestionTypes.BLANK,
            options = emptyList(), answer = answer, explanation = "",
            isFavorite = false, isWrong = false, fileName = originFileName
        )
    }

    private fun parseRowStyle2(
        row: Row, f: DataFormatter, workbookSuggestsShortAnswer: Boolean, originFileName: String
    ): Question? {
        val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
        val parsedOptions = (1..3)
            .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
            .filter { it.isNotBlank() }
        val explanation = row.getCell(4)?.let { f.formatCellValue(it) } ?: ""
        val answer = row.getCell(5)?.let { f.formatCellValue(it) } ?: ""
        val type = resolveType("", answer, content, workbookSuggestsShortAnswer)
        val normalizedContent = if (QuestionTypes.isInlineBlank(type)) {
            normalizeImportedFillContent(content, answer)
        } else {
            content
        }
        val options = normalizeOptionsForType(type, parsedOptions)
        val hasValidOptions = options.isNotEmpty() || QuestionTypes.isFill(type)
        return if (normalizedContent.isNotBlank() && hasValidOptions && answer.isNotBlank()) {
            Question(
                id = 0, content = normalizedContent, type = type,
                options = options, answer = answer, explanation = explanation,
                isFavorite = false, isWrong = false, fileName = originFileName
            )
        } else null
    }

    private fun parseRowStyle3(
        row: Row, f: DataFormatter, workbookSuggestsShortAnswer: Boolean, originFileName: String
    ): Question? {
        val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
        val rawType = row.getCell(1)?.let { f.formatCellValue(it) } ?: ""
        val parsedOptions = (2..4)
            .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
            .filter { it.isNotBlank() }
        val explanation = row.getCell(5)?.let { f.formatCellValue(it) } ?: ""
        val answer = row.getCell(6)?.let { f.formatCellValue(it) } ?: ""
        val type = resolveType(rawType, answer, content, workbookSuggestsShortAnswer)
        val normalizedContent = if (QuestionTypes.isInlineBlank(type)) {
            normalizeImportedFillContent(content, answer)
        } else {
            content
        }
        val options = normalizeOptionsForType(type, parsedOptions)
        val hasValidOptions = options.isNotEmpty() || QuestionTypes.isFill(type)
        return if (normalizedContent.isNotBlank() && hasValidOptions && answer.isNotBlank()) {
            Question(
                id = 0, content = normalizedContent, type = type,
                options = options, answer = answer, explanation = explanation,
                isFavorite = false, isWrong = false, fileName = originFileName
            )
        } else null
    }

    private fun buildDrawingAnswerWithImages(baseAnswer: String, imagePaths: List<String>): String {
        if (imagePaths.isEmpty()) return baseAnswer
        val imageTag = imagePaths.joinToString(",")
        return "$baseAnswer\n[DRAWING_IMAGES:$imageTag]"
    }

    private fun extractEmbeddedExcelImages(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        originFileName: String,
        stemColumnIndices: Set<Int>,
        answerColumnIndex: Int?
    ): EmbeddedExcelImages {
        val stemImagesByRow = mutableMapOf<Int, MutableList<String>>()
        val answerImagesByRow = mutableMapOf<Int, MutableList<String>>()
        val xssfSheet = (sheet as? org.apache.poi.xssf.usermodel.XSSFSheet) ?: return EmbeddedExcelImages(emptyMap(), emptyMap())
        val imageDir = File(quizStorageDir(), "images/${originFileName.replace(Regex("[^a-zA-Z0-9_\\-\\u4e00-\\u9fa5]"), "_")}").apply {
            if (!exists()) mkdirs()
        }
        val drawingPatriarch = xssfSheet.drawingPatriarch ?: return EmbeddedExcelImages(emptyMap(), emptyMap())
        val shapes = try { drawingPatriarch.shapes } catch (_: Exception) { emptyList<XSSFShape>() }
        var globalIndex = 0
        for (shape in shapes) {
            if (shape is XSSFPicture) {
                try {
                    val anchor = shape.clientAnchor as? XSSFClientAnchor ?: continue
                    val rowIndex = anchor.row1
                    val colIndex = anchor.col1.toInt()
                    val picData = shape.pictureData
                    if (picData != null) {
                        val ext = picData.suggestFileExtension() ?: "png"
                        val imgFile = File(imageDir, "stem_img_${globalIndex}.$ext")
                        imgFile.writeBytes(picData.data)
                        val target = when {
                            answerColumnIndex != null && colIndex == answerColumnIndex -> answerImagesByRow
                            stemColumnIndices.isNotEmpty() && colIndex in stemColumnIndices -> stemImagesByRow
                            answerColumnIndex == null && stemColumnIndices.isEmpty() -> stemImagesByRow
                            else -> null
                        }
                        target?.getOrPut(rowIndex) { mutableListOf() }?.add(imgFile.absolutePath)
                        globalIndex++
                    }
                } catch (_: Exception) { /* skip problematic image */ }
            }
        }
        return EmbeddedExcelImages(stemImagesByRow, answerImagesByRow)
    }
}
