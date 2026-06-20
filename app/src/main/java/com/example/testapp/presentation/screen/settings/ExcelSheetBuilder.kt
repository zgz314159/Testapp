package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import com.example.testapp.R
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.model.AIAnalysisData
import com.example.testapp.core.util.parseFillAnswerPartDescriptor
import com.example.testapp.core.util.splitFillAnswerParts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExcelSheetBuilder {

    suspend fun writeWorkbookToUri(
        context: Context,
        uri: Uri,
        sheets: Map<String, List<List<String>>>,
        highlightedRowsBySheet: Map<String, Set<Int>> = emptyMap()
    ) {
        val workbook = org.apache.poi.xssf.usermodel.XSSFWorkbook()
        val editedRowStyle = workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply { color = org.apache.poi.ss.usermodel.Font.COLOR_RED.toShort() })
        }

        for ((name, rows) in sheets) {
            val sanitizedName = sanitizeSheetName(name)
            val sheet = workbook.createSheet(sanitizedName)
            val highlightedRows = highlightedRowsBySheet[sanitizedName].orEmpty()
            for ((rowIndex, columns) in rows.withIndex()) {
                val row = sheet.createRow(rowIndex)
                for ((columnIndex, cellValue) in columns.withIndex()) {
                    val cell = row.createCell(columnIndex)
                    cell.setCellValue(toExcelCellValue(cellValue))
                    if (rowIndex in highlightedRows) cell.cellStyle = editedRowStyle
                }
            }
        }

        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { out -> workbook.write(out) }
                ?: throw LocalizedException(IOConstants.CANNOT_WRITE_FILE)
            workbook.close()
        }
    }

    internal fun buildStructuredQuestionExportRows(
        context: Context,
        title: String,
        description: String,
        rows: List<QuestionExportSnapshot>
    ): List<List<String>> = buildList {
        add(listOf(context.getString(R.string.export_meta_title), title, ""))
        add(listOf(context.getString(R.string.export_meta_description), description, ""))
        add(
            listOf(
                context.getString(R.string.export_meta_time),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                ""
            )
        )
        add(buildQuestionExportHeaders(context))
        rows.forEach { add(buildQuestionExportRow(it)) }
    }

    fun sanitizeSheetName(name: String): String {
        var result = name.replace(Regex("[\\\\/:*?\\[\\]]"), "_")
        if (result.length > 31) result = result.substring(0, 31)
        return if (result.isBlank()) "Sheet" else result
    }

    internal fun buildQuestionExportSnapshots(
        questions: List<Question>,
        supplementary: Map<Int, SupplementaryData>
    ): List<QuestionExportSnapshot> = questions.mapIndexed { index, q ->
        QuestionExportSnapshot(
            question = q,
            questionNumber = index + 1,
            analysis = supplementary[q.id]?.analysis,
            note = supplementary[q.id]?.note.orEmpty()
        )
    }

    fun buildWrongBookExportSheets(
        context: Context,
        wrongs: List<WrongQuestion>,
        supplementary: Map<Int, SupplementaryData>,
        defaultSheetName: String
    ): Map<String, List<List<String>>> {
        if (wrongs.isEmpty()) return mapOf(
            sanitizeSheetName(defaultSheetName) to buildStructuredQuestionExportRows(
                context = context,
                title = defaultSheetName,
                description = context.getString(R.string.export_wrong_empty_description),
                rows = emptyList()
            )
        )
        return wrongs.groupBy { it.question.fileName ?: defaultSheetName }.mapValues { (groupName, list) ->
            buildStructuredQuestionExportRows(
                context = context,
                title = groupName,
                description = context.getString(R.string.export_wrong_single_description, groupName),
                rows = buildQuestionExportSnapshots(list.map { it.question }, supplementary)
            )
        }
    }

    fun buildFavoriteExportSheets(
        context: Context,
        favorites: List<FavoriteQuestion>,
        supplementary: Map<Int, SupplementaryData>,
        defaultSheetName: String
    ): Map<String, List<List<String>>> {
        if (favorites.isEmpty()) return mapOf(
            sanitizeSheetName(defaultSheetName) to buildStructuredQuestionExportRows(
                context = context,
                title = defaultSheetName,
                description = context.getString(R.string.export_favorite_empty_description),
                rows = emptyList()
            )
        )
        return favorites.groupBy { it.question.fileName ?: defaultSheetName }.mapValues { (groupName, list) ->
            buildStructuredQuestionExportRows(
                context = context,
                title = groupName,
                description = context.getString(R.string.export_favorite_single_description, groupName),
                rows = buildQuestionExportSnapshots(list.map { it.question }, supplementary)
            )
        }
    }

    private fun buildQuestionExportHeaders(context: Context): List<String> = buildList {
        add(context.getString(R.string.export_header_content))
        add(context.getString(R.string.export_header_question_number))
        add(context.getString(R.string.export_header_type))
        repeat(50) { index ->
            add(context.getString(R.string.export_header_answer_part_format, index + 1))
            add(context.getString(R.string.export_header_answer_category_format, index + 1))
            add(context.getString(R.string.export_header_answer_score_format, index + 1))
        }
        repeat(7) { index -> add(context.getString(R.string.option_label, (index + 1).toString())) }
        add(context.getString(R.string.export_header_explanation))
        add(context.getString(R.string.export_header_deepseek))
        add(context.getString(R.string.export_header_spark))
        add(context.getString(R.string.export_header_baidu))
        add(context.getString(R.string.export_header_note))
    }

    private fun buildQuestionExportRow(snapshot: QuestionExportSnapshot): List<String> {
        val answerParts = if (QuestionTypes.isInlineBlank(snapshot.question.type)) {
            splitFillAnswerParts(snapshot.question.answer).take(50).map(::parseFillAnswerPartDescriptor)
        } else {
            listOf(parseFillAnswerPartDescriptor(snapshot.question.answer))
        }
        return buildList {
            add(snapshot.question.content)
            add(snapshot.questionNumber.toString())
            add(buildExportQuestionTypeLabel(snapshot.question.type))
            repeat(50) { index ->
                val part = answerParts.getOrNull(index)
                add(part?.answerText.orEmpty())
                add(part?.category.orEmpty())
                add(part?.score?.let { "$it" }.orEmpty())
            }
            repeat(7) { index -> add(snapshot.question.options.getOrNull(index).orEmpty()) }
            add(snapshot.question.explanation)
            add(snapshot.analysis?.deepSeekAnalysis.orEmpty())
            add(snapshot.analysis?.sparkAnalysis.orEmpty())
            add(snapshot.analysis?.baiduAnalysis.orEmpty())
            add(snapshot.note)
        }
    }

    private fun buildExportQuestionTypeLabel(type: String): String = when {
        QuestionTypes.isEssay(type) -> "论述题"
        QuestionTypes.isComprehensive(type) -> "综合题"
        QuestionTypes.isCalculation(type) -> "计算题"
        QuestionTypes.isShort(type) -> "简答题"
        QuestionTypes.isSingle(type) -> "单选题"
        QuestionTypes.isMulti(type) -> "多选题"
        QuestionTypes.isJudge(type) -> "判断题"
        QuestionTypes.isFill(type) -> "填空题"
        else -> type
    }

    private fun toExcelCellValue(value: String): String {
        val sanitized = value.replace("\u0000", "")
        return if (sanitized.length > MAX_EXCEL_CELL_TEXT_LENGTH) {
            sanitized.take(MAX_EXCEL_CELL_TEXT_LENGTH)
        } else {
            sanitized
        }
    }

    private companion object {
        const val MAX_EXCEL_CELL_TEXT_LENGTH = 32_767
    }
}

internal data class QuestionExportSnapshot(
    val question: Question,
    val questionNumber: Int,
    val analysis: AIAnalysisData?,
    val note: String
)
