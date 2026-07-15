package com.example.testapp.presentation.screen.home.model

/**
 * PowerAI 首页仪表板 UI State。
 *
 * 由 [HomeDashboardPipeline] 从 HomeViewModel 原始数据聚合生成，
 * 单项数据流：ViewModel → Pipeline → UI.collectAsState。
 */
data class HomeDashboardUiState(
    val greeting: String = "",
    val subtitle: String = "",
    val continueFileName: String = "",
    val continueFileDisplayName: String = "",
    val continueProgressPercent: Int = 0,
    val totalQuestions: Int = 0,
    val wrongCount: Int = 0,
    val favoriteCount: Int = 0,
    val completedCount: Int = 0,
    val questionBankItems: List<QuestionBankItem> = emptyList(),
    val showContinueCard: Boolean = false,
) {
    /**
     * 首页单题库 item（用于题库列表渲染）。
     */
    data class QuestionBankItem(
        val fileName: String,
        val displayName: String,
        val questionCount: Int,
        val wrongCount: Int,
        val favoriteCount: Int,
        val progressPercent: Int,
        val isSelected: Boolean,
    )
}
