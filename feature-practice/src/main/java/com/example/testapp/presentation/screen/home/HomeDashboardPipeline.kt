package com.example.testapp.presentation.screen.home

import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.model.HomeDashboardUiState

/**
 * PowerAI 首页仪表板聚合 Pipeline。
 *
 * 纯函数管道：从原始数据（fileNames、fileStatistics、practiceProgressCompleted）
 * 计算出 [HomeDashboardUiState]。
 *
 * 职责：
 * - 问候语按时间动态生成
 * - 副标题无真实学习数据时只显示"继续学习吧"
 * - Hero 文件推导
 * - 四项统计汇总（总题数、错题数、收藏数、已完成）
 * - 题库列表构建
 * - 进度百分比限制在 0..100
 */
object HomeDashboardPipeline {

    /**
     * 构建完整首页 UI State。
     *
     * @param fileNames 所有题库文件名
     * @param fileStatistics fileName → FileStatistics
     * @param practiceProgressCompleted fileName → 已完成题数（从 [com.example.testapp.presentation.screen.practice.buildHomePracticeProgressMap] 获取）
     * @param storedFileName DataStore 中最近选中文件名
     * @param selectedFileName 当前 UI 选中文件名
     * @param recentFileNames 最近使用文件排序列表
     */
    fun buildDashboard(
        fileNames: List<String>,
        fileStatistics: Map<String, FileStatistics>,
        practiceProgressCompleted: Map<String, Int>,
        storedFileName: String,
        selectedFileName: String,
        recentFileNames: List<String>,
    ): HomeDashboardUiState {
        val greeting = resolveGreeting()
        val subtitle = resolveSubtitle()
        val totalQuestions = fileStatistics.values.sumOf { it.questionCount }
        val wrongCount = fileStatistics.values.sumOf { it.wrongCount }
        val favoriteCount = fileStatistics.values.sumOf { it.favoriteCount }
        val completedCount = practiceProgressCompleted.values.sum()

        // Hero（继续学习）文件选择：优先 storedFileName，否则第一个题库
        val heroFileName = when {
            storedFileName.isNotBlank() && storedFileName in fileNames -> storedFileName
            fileNames.isNotEmpty() -> fileNames.first()
            else -> ""
        }
        val showContinueCard = heroFileName.isNotBlank()

        val continueProgressPercent = if (showContinueCard) {
            val heroStats = fileStatistics[heroFileName]
            val answered = practiceProgressCompleted[heroFileName] ?: 0
            val total = heroStats?.questionCount?.takeIf { it > 0 } ?: 1
            (answered * 100 / total).coerceIn(0, 100)
        } else 0

        // 题库列表：按最近使用排序
        val orderedFileNames = reorderFileNames(fileNames, storedFileName, recentFileNames)
        val questionBankItems = orderedFileNames.map { fileName ->
            val stats = fileStatistics[fileName] ?: FileStatistics()
            val progressCount = practiceProgressCompleted[fileName] ?: 0
            val total = stats.questionCount
            val pct = if (total > 0) (progressCount * 100 / total).coerceIn(0, 100) else 0
            HomeDashboardUiState.QuestionBankItem(
                fileName = fileName,
                displayName = cleanupDisplayName(fileName),
                questionCount = stats.questionCount,
                wrongCount = stats.wrongCount,
                favoriteCount = stats.favoriteCount,
                progressPercent = pct,
                isSelected = fileName == selectedFileName,
            )
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
            questionBankItems = questionBankItems,
            showContinueCard = showContinueCard,
        )
    }

    /**
     * 按本地时间动态生成问候语。
     * 05:00-11:59 → 早上好
     * 12:00-17:59 → 下午好
     * 18:00-04:59 → 晚上好
     */
    fun resolveGreeting(): String {
        val hour = java.time.LocalTime.now().hour
        return when {
            hour in 5..11 -> "早上好，继续学习吧 👋"
            hour in 12..17 -> "下午好，继续学习吧 👋"
            else -> "晚上好，继续学习吧 👋"
        }
    }

    /**
     * 副标题：没有真实学习时长时使用"继续学习吧"。
     * 不得显示虚构分钟数和连续天数。
     */
    fun resolveSubtitle(): String = "从今天的题库开始吧"

    /**
     * 文件排序：优先 storedFileName（最近选中），再按 recentFileNames 排序，
     * 其余保持原始顺序。
     */
    fun reorderFileNames(
        fileNames: List<String>,
        storedFileName: String,
        recentFileNames: List<String>,
    ): List<String> {
        val primary = if (storedFileName.isNotBlank() && storedFileName in fileNames) {
            listOf(storedFileName)
        } else emptyList()
        val recentOrdered = recentFileNames.filter { it in fileNames && it != storedFileName }
        val remaining = fileNames.filter { it != storedFileName && it !in recentOrdered }
        return primary + recentOrdered + remaining
    }

    /**
     * 清理文件名中的技术后缀，生成友好的展示名。
     *
     * - 去掉文件扩展名（.txt, .json 等）
     * - 去掉明显的生成后缀：_final、_final_数字、日期、编号后缀
     * - 不伪造业务名称
     */
    fun cleanupDisplayName(fileName: String): String {
        val noExt = fileName
            .removeSuffix(".txt")
            .removeSuffix(".json")
            .removeSuffix(".xlsx")
            .removeSuffix(".xls")
            .removeSuffix(".docx")
            .removeSuffix(".doc")
        return noExt
            .replace(Regex("[（(]\\s*\\d+\\s*题\\s*[）)]"), "")
            .replace(Regex("(?i)_final.*$"), "")
            .replace(Regex("(?i)_v[\\d]+$"), "")
            .replace(Regex("_20\\d{2}.*$"), "")
            .trim()
            .ifEmpty { noExt }
    }

    /**
     * 响应式列数推算。
     * 360-599dp → 1列；600-839dp → 2列；≥840dp → 2列居中。
     */
    fun resolveHomeColumnCount(widthDp: Float): Int = when {
        widthDp >= 600 -> 2
        else -> 1
    }
}
