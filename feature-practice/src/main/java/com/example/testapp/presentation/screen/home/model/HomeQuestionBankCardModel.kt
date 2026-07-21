package com.example.testapp.presentation.screen.home.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import com.example.testapp.presentation.screen.home.HomeFileTypeVisualPipeline
import com.example.testapp.presentation.screen.home.HomeQuestionBankVisualKind

@Immutable
data class HomeQuestionBankCardModel(
    val fileName: String,
    val displayName: String,
    val progressPercent: Int,
    val questionCount: Int,
    val wrongCount: Int,
    val favoriteCount: Int,
    val progressCount: Int,
    val statistics: FileStatistics,
    val visualKind: HomeQuestionBankVisualKind,
    val icon: ImageVector,
    val gradientStart: Color,
    val gradientEnd: Color,
    val progressPercentLabel: String,
    val questionCountLabel: String,
    val wrongCountLabel: String,
    val favoriteCountLabel: String,
    val ctaLabel: String,
)

object HomeQuestionBankCardModelPipeline {
    fun build(
        fileName: String,
        statistics: FileStatistics,
        progressCount: Int,
    ): HomeQuestionBankCardModel {
        val questionCount = statistics.questionCount
        val progressPercent = if (questionCount > 0) {
            (progressCount * 100 / questionCount).coerceIn(0, 100)
        } else {
            0
        }
        val visual = HomeFileTypeVisualPipeline.resolve(fileName, statistics)
        return HomeQuestionBankCardModel(
            fileName = fileName,
            displayName = HomeDashboardPipeline.cleanupDisplayName(fileName),
            progressPercent = progressPercent,
            questionCount = questionCount,
            wrongCount = statistics.wrongCount,
            favoriteCount = statistics.favoriteCount,
            progressCount = progressCount,
            statistics = statistics,
            visualKind = visual.kind,
            icon = visual.icon,
            gradientStart = visual.gradientStart,
            gradientEnd = visual.gradientEnd,
            progressPercentLabel = "$progressPercent%",
            questionCountLabel = questionCount.toString(),
            wrongCountLabel = statistics.wrongCount.toString(),
            favoriteCountLabel = statistics.favoriteCount.toString(),
            ctaLabel = if (progressPercent > 0) "继续学习" else "开始练习",
        )
    }

    fun buildList(
        fileNames: List<String>,
        fileStatistics: Map<String, FileStatistics>,
        practiceProgress: Map<String, Int>,
    ): List<HomeQuestionBankCardModel> = fileNames.map { fileName ->
        build(
            fileName = fileName,
            statistics = fileStatistics[fileName] ?: FileStatistics(),
            progressCount = practiceProgress[fileName] ?: 0,
        )
    }
}
