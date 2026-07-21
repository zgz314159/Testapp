package com.example.testapp.data.repository.parser

import com.example.testapp.data.repository.ImportedQuestionPayload
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.util.FILL_PART_DELIMITER

internal fun parseExcelRowByHeader(
    row: ExcelRowData,
    schema: ExcelHeaderSchema,
    workbookSuggestsShortAnswer: Boolean,
    originFileName: String
): ImportedQuestionPayload? {
    val content = excelContentCellText(row, schema.contentIndex)
    val rawType = schema.typeIndex?.let { excelCellText(row, it) }.orEmpty()
    if (ExcelImportAnswerNormalizePipeline.shouldSkipInstructionRow(content, rawType)) return null

    val explanation = schema.explanationIndex?.let { excelCellText(row, it) }.orEmpty()
    val answerParts = schema.answerPartSlots
        .map { slot ->
            buildAnnotatedAnswerPart(
                answerText = slot.answerIndex?.let { excelCellText(row, it) }.orEmpty(),
                category = slot.categoryIndex?.let { excelCellText(row, it) }.orEmpty(),
                scoreText = slot.scoreIndex?.let { excelCellText(row, it) }.orEmpty()
            )
        }
        .filter { it.isNotBlank() }
    val directAnswer = schema.answerIndex?.let { excelCellText(row, it) }.orEmpty()
    // 有选项列 + 单列正确答案时，优先单列答案，避免历史误填空槽污染选择题
    val answer = when {
        directAnswer.isNotBlank() && (schema.optionIndices.isNotEmpty() || answerParts.isEmpty()) -> directAnswer
        answerParts.isNotEmpty() -> {
            if (answerParts.size == 1) answerParts.first() else answerParts.joinToString(FILL_PART_DELIMITER)
        }
        else -> directAnswer
    }
    val parsedOptions = schema.optionIndices
        .map { index -> excelCellText(row, index) }
        .filter { it.isNotBlank() }
        .filterNot { normalizeExcelHeader(it) == normalizeExcelHeader(rawType) }

    val type = resolveExcelQuestionType(rawType, answer, content, workbookSuggestsShortAnswer)
    val normalizedContent = if (QuestionTypes.isInlineBlank(type)) {
        normalizeImportedFillContent(content, answer)
    } else {
        content
    }
    val options = normalizeExcelOptionsForType(type, parsedOptions)
    val hasValidOptions = options.isNotEmpty() ||
        QuestionTypes.isFill(type) ||
        QuestionTypes.isJudge(type) ||
        QuestionTypes.isTextResponse(type)

    val isDrawingOrTextResponse = QuestionTypes.isTextResponse(type)
    val normalizedAnswer = when {
        isDrawingOrTextResponse && answer.isBlank() -> "略"
        QuestionTypes.isSingle(type) || QuestionTypes.isMulti(type) || QuestionTypes.isJudge(type) ->
            ExcelImportAnswerNormalizePipeline.normalizeChoiceAnswer(type, answer, options.size)
        else -> answer.takeIf { it.isNotBlank() }
    } ?: return null

    return if (normalizedContent.isNotBlank() && hasValidOptions) {
        ImportedQuestionPayload(
            question = Question(
                id = 0, content = normalizedContent, type = type,
                options = options, answer = normalizedAnswer, explanation = explanation,
                isFavorite = false, isWrong = false, fileName = originFileName
            ),
            deepSeekAnalysis = schema.deepSeekIndex?.let { excelCellText(row, it) }.orEmpty(),
            sparkAnalysis = schema.sparkIndex?.let { excelCellText(row, it) }.orEmpty(),
            baiduAnalysis = schema.baiduIndex?.let { excelCellText(row, it) }.orEmpty(),
            note = schema.noteIndex?.let { excelCellText(row, it) }.orEmpty()
        )
    } else null
}

internal fun parseExcelLegacyCalculationRow(
    row: ExcelRowData,
    workbookSuggestsShortAnswer: Boolean,
    originFileName: String
): Question? {
    if (!workbookSuggestsShortAnswer) return null
    val values = excelRowValues(row)
    if (values.count { it.isNotBlank() } < 2) return null
    if (values.any { normalizeExcelHeader(it) in setOf("题干", "题目", "内容", "试题", "问题", "答案", "参考答案", "评分标准") }) {
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

internal fun parseExcelRowStyle1(
    row: ExcelRowData,
    workbookSuggestsShortAnswer: Boolean,
    originFileName: String
): Question? {
    val content = row.cellText(0)
    val rawType = row.cellText(1)
    val parsedOptions = (2..8)
        .map { row.cellText(it) }
        .filter { it.isNotBlank() }
    val explanation = row.cellText(9)
    val answer = row.cellText(10)
    val type = resolveExcelQuestionType(rawType, answer, content, workbookSuggestsShortAnswer)
    val options = normalizeExcelOptionsForType(type, parsedOptions)
    val hasValidOptions = options.isNotEmpty() || QuestionTypes.isFill(type)
    return if (content.isNotBlank() && hasValidOptions && answer.isNotBlank()) {
        Question(
            id = 0, content = content, type = type,
            options = options, answer = answer, explanation = explanation,
            isFavorite = false, isWrong = false, fileName = originFileName
        )
    } else null
}

internal fun parseExcelFillTemplateRow(
    row: ExcelRowData,
    originFileName: String
): Question? {
    val content = row.cellText(0)
    val rawType = row.cellText(2)
    if (content.isBlank() || !QuestionTypes.isInlineBlank(rawType)) return null

    val answers = (3 until row.lastCellNum)
        .map { index -> row.cellText(index).trim() }
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

internal fun parseExcelRowStyle2(
    row: ExcelRowData,
    workbookSuggestsShortAnswer: Boolean,
    originFileName: String
): Question? {
    val content = row.cellText(0)
    val parsedOptions = (1..3)
        .map { row.cellText(it) }
        .filter { it.isNotBlank() }
    val explanation = row.cellText(4)
    val answer = row.cellText(5)
    val type = resolveExcelQuestionType("", answer, content, workbookSuggestsShortAnswer)
    val normalizedContent = if (QuestionTypes.isInlineBlank(type)) {
        normalizeImportedFillContent(content, answer)
    } else {
        content
    }
    val options = normalizeExcelOptionsForType(type, parsedOptions)
    val hasValidOptions = options.isNotEmpty() || QuestionTypes.isFill(type)
    return if (normalizedContent.isNotBlank() && hasValidOptions && answer.isNotBlank()) {
        Question(
            id = 0, content = normalizedContent, type = type,
            options = options, answer = answer, explanation = explanation,
            isFavorite = false, isWrong = false, fileName = originFileName
        )
    } else null
}

internal fun parseExcelRowStyle3(
    row: ExcelRowData,
    workbookSuggestsShortAnswer: Boolean,
    originFileName: String
): Question? {
    val content = row.cellText(0)
    val rawType = row.cellText(1)
    val parsedOptions = (2..4)
        .map { row.cellText(it) }
        .filter { it.isNotBlank() }
    val explanation = row.cellText(5)
    val answer = row.cellText(6)
    val type = resolveExcelQuestionType(rawType, answer, content, workbookSuggestsShortAnswer)
    val normalizedContent = if (QuestionTypes.isInlineBlank(type)) {
        normalizeImportedFillContent(content, answer)
    } else {
        content
    }
    val options = normalizeExcelOptionsForType(type, parsedOptions)
    val hasValidOptions = options.isNotEmpty() || QuestionTypes.isFill(type)
    return if (normalizedContent.isNotBlank() && hasValidOptions && answer.isNotBlank()) {
        Question(
            id = 0, content = normalizedContent, type = type,
            options = options, answer = answer, explanation = explanation,
            isFavorite = false, isWrong = false, fileName = originFileName
        )
    } else null
}
