package com.example.testapp.presentation.screen.settings

import android.content.Context
import com.example.testapp.feature.settings.R
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.AIAnalysisData
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.core.util.parseFillAnswerPartDescriptor
import com.example.testapp.core.util.splitFillAnswerParts
import javax.inject.Inject
import javax.inject.Singleton

/** Excel 表结构构建 — 输出 `Map<sheetName, rows>`，写 Uri 由 `SettingsIoUriPipeline` 负责。 */
@Singleton
class ExcelSheetBuilder @Inject constructor() {

    fun buildStructuredQuestionExportRows(
        context: Context,
        title: String,
        description: String,
        rows: List<QuestionExportSnapshot>,
    ): List<List<String>> = buildList {
        add(listOf(context.getString(R.string.export_meta_title), title, ""))
        add(listOf(context.getString(R.string.export_meta_description), description, ""))
        add(
            listOf(
                context.getString(R.string.export_meta_time),
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "",
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

    fun buildQuestionExportSnapshots(
        questions: List<Question>,
        supplementary: Map<Int, SupplementaryData>,
    ): List<QuestionExportSnapshot> = questions.mapIndexed { index, q ->
        QuestionExportSnapshot(
            question = q,
            questionNumber = index + 1,
            analysis = supplementary[q.id]?.analysis,
            note = supplementary[q.id]?.note.orEmpty(),
        )
    }

    fun buildWrongBookExportSheets(
        context: Context,
        wrongs: List<WrongQuestion>,
        supplementary: Map<Int, SupplementaryData>,
        defaultSheetName: String,
    ): Map<String, List<List<String>>> {
        if (wrongs.isEmpty()) {
            return mapOf(
                sanitizeSheetName(defaultSheetName) to buildStructuredQuestionExportRows(
                    context = context,
                    title = defaultSheetName,
                    description = context.getString(R.string.export_wrong_empty_description),
                    rows = emptyList(),
                )
            )
        }
        return wrongs.groupBy { it.question.fileName ?: defaultSheetName }.mapValues { (groupName, list) ->
            buildStructuredQuestionExportRows(
                context = context,
                title = groupName,
                description = context.getString(R.string.export_wrong_single_description, groupName),
                rows = buildQuestionExportSnapshots(list.map { it.question }, supplementary),
            )
        }
    }

    fun buildFavoriteExportSheets(
        context: Context,
        favorites: List<FavoriteQuestion>,
        supplementary: Map<Int, SupplementaryData>,
        defaultSheetName: String,
    ): Map<String, List<List<String>>> {
        if (favorites.isEmpty()) {
            return mapOf(
                sanitizeSheetName(defaultSheetName) to buildStructuredQuestionExportRows(
                    context = context,
                    title = defaultSheetName,
                    description = context.getString(R.string.export_favorite_empty_description),
                    rows = emptyList(),
                )
            )
        }
        return favorites.groupBy { it.question.fileName ?: defaultSheetName }.mapValues { (groupName, list) ->
            buildStructuredQuestionExportRows(
                context = context,
                title = groupName,
                description = context.getString(R.string.export_favorite_single_description, groupName),
                rows = buildQuestionExportSnapshots(list.map { it.question }, supplementary),
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
}

data class QuestionExportSnapshot(
    val question: Question,
    val questionNumber: Int,
    val analysis: AIAnalysisData?,
    val note: String,
)
