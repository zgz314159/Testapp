package com.example.testapp.presentation.screen.home.model

/**
 * PowerAI 首页仪表板 UI State。
 *
 * 由 [com.example.testapp.presentation.screen.home.HomeDashboardPipeline]
 * 从 HomeViewModel 原始数据聚合生成，单项数据流：ViewModel → Pipeline → UI。
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
    val showContinueCard: Boolean = false,
)
