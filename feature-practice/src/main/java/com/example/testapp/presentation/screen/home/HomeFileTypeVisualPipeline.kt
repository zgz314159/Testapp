package com.example.testapp.presentation.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.usecase.FileStatistics

enum class HomeQuestionBankVisualKind {
    Spreadsheet,
    Document,
    Database,
    Data,
    Text,
    Drawing,
    Fill,
    Judge,
    SingleChoice,
    MultipleChoice,
    Calculation,
    Written,
    Mixed,
    Generic,
}

/** Resolves a relevant icon from both the imported file source and its actual question types. */
object HomeFileTypeVisualPipeline {

    data class FileTypeVisual(
        val kind: HomeQuestionBankVisualKind,
        val icon: ImageVector,
        val gradientStart: Color,
        val gradientEnd: Color,
    )

    fun resolve(
        fileName: String,
        statistics: FileStatistics = FileStatistics(),
    ): FileTypeVisual = visualFor(resolveKind(fileName, statistics))

    fun resolveKind(
        fileName: String,
        statistics: FileStatistics,
    ): HomeQuestionBankVisualKind {
        val lowerName = fileName.lowercase()
        val extension = fileName.substringAfterLast('.', "").lowercase()

        explicitQuestionKind(lowerName)?.let { return it }

        return when (extension) {
            "xlsx", "xls", "csv" -> HomeQuestionBankVisualKind.Spreadsheet
            "docx", "doc" -> HomeQuestionBankVisualKind.Document
            "db", "sqlite", "sqlite3" -> HomeQuestionBankVisualKind.Database
            "json" -> HomeQuestionBankVisualKind.Data
            "txt", "md", "markdown" -> HomeQuestionBankVisualKind.Text
            "png", "jpg", "jpeg", "webp" -> HomeQuestionBankVisualKind.Drawing
            else -> questionStatisticsKind(statistics)
        }
    }

    private fun explicitQuestionKind(lowerName: String): HomeQuestionBankVisualKind? = when {
        containsAny(lowerName, "绘图", "画图", "作图", "drawing", "image") ->
            HomeQuestionBankVisualKind.Drawing
        containsAny(lowerName, "填空", "fill", "blank") -> HomeQuestionBankVisualKind.Fill
        containsAny(lowerName, "判断", "judge", "true false") -> HomeQuestionBankVisualKind.Judge
        containsAny(lowerName, "多选", "multiple choice", "multi choice") ->
            HomeQuestionBankVisualKind.MultipleChoice
        containsAny(lowerName, "单选", "single choice") -> HomeQuestionBankVisualKind.SingleChoice
        containsAny(lowerName, "计算题", "计算", "calculation") -> HomeQuestionBankVisualKind.Calculation
        containsAny(lowerName, "简答", "论述", "问答", "essay", "short answer") ->
            HomeQuestionBankVisualKind.Written
        else -> null
    }

    private fun questionStatisticsKind(statistics: FileStatistics): HomeQuestionBankVisualKind {
        if (statistics.questionTypeStats.size > 1) return HomeQuestionBankVisualKind.Mixed
        val type = statistics.primaryQuestionType
        return when {
            QuestionTypes.isDrawing(type) -> HomeQuestionBankVisualKind.Drawing
            QuestionTypes.isInlineBlank(type) -> HomeQuestionBankVisualKind.Fill
            QuestionTypes.isJudge(type) -> HomeQuestionBankVisualKind.Judge
            QuestionTypes.isMulti(type) -> HomeQuestionBankVisualKind.MultipleChoice
            QuestionTypes.isSingle(type) -> HomeQuestionBankVisualKind.SingleChoice
            QuestionTypes.isCalculation(type) -> HomeQuestionBankVisualKind.Calculation
            QuestionTypes.isTextResponse(type) -> HomeQuestionBankVisualKind.Written
            else -> HomeQuestionBankVisualKind.Generic
        }
    }

    private fun visualFor(kind: HomeQuestionBankVisualKind): FileTypeVisual = when (kind) {
        HomeQuestionBankVisualKind.Spreadsheet ->
            FileTypeVisual(kind, Icons.Default.TableChart, Color(0xFF7C3AED), Color(0xFFA78BFA))
        HomeQuestionBankVisualKind.Document ->
            FileTypeVisual(kind, Icons.AutoMirrored.Filled.Article, Color(0xFF2563EB), Color(0xFF60A5FA))
        HomeQuestionBankVisualKind.Database ->
            FileTypeVisual(kind, Icons.Default.Storage, Color(0xFF0891B2), Color(0xFF22D3EE))
        HomeQuestionBankVisualKind.Data ->
            FileTypeVisual(kind, Icons.Default.DataObject, Color(0xFF475569), Color(0xFF94A3B8))
        HomeQuestionBankVisualKind.Text ->
            FileTypeVisual(kind, Icons.AutoMirrored.Filled.TextSnippet, Color(0xFF4F46E5), Color(0xFF818CF8))
        HomeQuestionBankVisualKind.Drawing ->
            FileTypeVisual(kind, Icons.Default.Image, Color(0xFFEC4899), Color(0xFFFB7185))
        HomeQuestionBankVisualKind.Fill ->
            FileTypeVisual(kind, Icons.Default.EditNote, Color(0xFF0D9488), Color(0xFF2DD4BF))
        HomeQuestionBankVisualKind.Judge ->
            FileTypeVisual(kind, Icons.AutoMirrored.Filled.FactCheck, Color(0xFF16A34A), Color(0xFF4ADE80))
        HomeQuestionBankVisualKind.SingleChoice ->
            FileTypeVisual(kind, Icons.Default.RadioButtonChecked, Color(0xFF4F46E5), Color(0xFF818CF8))
        HomeQuestionBankVisualKind.MultipleChoice ->
            FileTypeVisual(kind, Icons.Default.Checklist, Color(0xFFD97706), Color(0xFFFBBF24))
        HomeQuestionBankVisualKind.Calculation ->
            FileTypeVisual(kind, Icons.Default.Calculate, Color(0xFFEA580C), Color(0xFFFB923C))
        HomeQuestionBankVisualKind.Written ->
            FileTypeVisual(kind, Icons.AutoMirrored.Filled.Assignment, Color(0xFF7C3AED), Color(0xFFC084FC))
        HomeQuestionBankVisualKind.Mixed ->
            FileTypeVisual(kind, Icons.AutoMirrored.Filled.LibraryBooks, Color(0xFF2563EB), Color(0xFF8B5CF6))
        HomeQuestionBankVisualKind.Generic ->
            FileTypeVisual(kind, Icons.AutoMirrored.Filled.Assignment, Color(0xFF4F8CFF), Color(0xFF79C9FF))
    }

    private fun containsAny(value: String, vararg candidates: String): Boolean =
        candidates.any(value::contains)
}
