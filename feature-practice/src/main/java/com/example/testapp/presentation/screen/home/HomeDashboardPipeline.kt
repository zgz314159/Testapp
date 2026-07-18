package com.example.testapp.presentation.screen.home

import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.model.HomeDashboardUiState

/**
 * PowerAI 首页仪表板聚合 Pipeline。
 *
 * 纯函数管道：从原始数据（fileNames、fileStatistics、practiceProgressCompleted）
 * 计算出 [HomeDashboardUiState]（仅 header / Hero，不构建题库列表）。
 */
object HomeDashboardPipeline {

    fun buildDashboard(
        fileNames: List<String>,
        fileStatistics: Map<String, FileStatistics>,
        practiceProgressCompleted: Map<String, Int>,
        storedFileName: String,
        recentFileNames: List<String> = emptyList(),
    ): HomeDashboardUiState {
        val greeting = resolveGreeting()
        val subtitle = resolveSubtitle()
        val totalQuestions = fileStatistics.values.sumOf { it.questionCount }
        val wrongCount = fileStatistics.values.sumOf { it.wrongCount }
        val favoriteCount = fileStatistics.values.sumOf { it.favoriteCount }
        val completedCount = practiceProgressCompleted.values.sum()

        val heroFileName = when {
            storedFileName.isNotBlank() && storedFileName in fileNames -> storedFileName
            recentFileNames.isNotEmpty() && recentFileNames.first() in fileNames -> recentFileNames.first()
            fileNames.isNotEmpty() -> fileNames.first()
            else -> ""
        }
        val showContinueCard = heroFileName.isNotBlank()

        val continueProgressPercent = if (showContinueCard) {
            val heroStats = fileStatistics[heroFileName]
            val answered = practiceProgressCompleted[heroFileName] ?: 0
            val total = heroStats?.questionCount?.takeIf { it > 0 } ?: 1
            (answered * 100 / total).coerceIn(0, 100)
        } else {
            0
        }

        return HomeDashboardUiState(
            greeting = greeting,
            subtitle = subtitle,
            continueFileName = heroFileName,
            continueFileDisplayName = cleanupDisplayName(heroFileName),
            continueProgressPercent = continueProgressPercent,
            totalQuestions = totalQuestions,
            wrongCount = wrongCount,
            favoriteCount = favoriteCount,
            completedCount = completedCount,
            showContinueCard = showContinueCard,
        )
    }

    fun resolveGreeting(): String {
        val hour = java.time.LocalTime.now().hour
        return when {
            hour in 5..11 -> "早上好，继续学习吧 👋"
            hour in 12..17 -> "下午好，继续学习吧 👋"
            else -> "晚上好，继续学习吧 👋"
        }
    }

    fun resolveSubtitle(): String = "从今天的题库开始吧"

    fun reorderFileNames(
        fileNames: List<String>,
        storedFileName: String,
        recentFileNames: List<String>,
    ): List<String> {
        val primary = if (storedFileName.isNotBlank() && storedFileName in fileNames) {
            listOf(storedFileName)
        } else {
            emptyList()
        }
        val recentOrdered = recentFileNames.filter { it in fileNames && it != storedFileName }
        val remaining = fileNames.filter { it != storedFileName && it !in recentOrdered }
        return primary + recentOrdered + remaining
    }

    /**
     * 生成展示名：仅去掉文件扩展名（.txt, .json 等），其余部分完整保留。
     */
    fun cleanupDisplayName(fileName: String): String {
        val noExt = fileName
            .removeSuffix(".txt")
            .removeSuffix(".json")
            .removeSuffix(".xlsx")
            .removeSuffix(".xls")
            .removeSuffix(".docx")
            .removeSuffix(".doc")
            .trim()
        return noExt.ifEmpty { fileName }
    }

    fun resolveHomeColumnCount(widthDp: Float): Int = when {
        widthDp >= 600 -> 2
        else -> 1
    }

    enum class QuestionBankCardLayout {
        Wide,
        Compact,
        Dense,
    }

    fun resolveQuestionBankCardLayout(
        useGridLayout: Boolean,
        parentWidthDp: Float,
    ): QuestionBankCardLayout = when {
        useGridLayout -> QuestionBankCardLayout.Dense
        parentWidthDp < 380f -> QuestionBankCardLayout.Compact
        else -> QuestionBankCardLayout.Wide
    }
}
