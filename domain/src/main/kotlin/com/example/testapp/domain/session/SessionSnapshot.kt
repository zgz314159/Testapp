package com.example.testapp.domain.session

/** 不可变会话快照（V5 §5）；Session 内部状态每次 map 为新 copy */
data class QuestionSnapshot(
    val id: Int,
    val content: String,
    val type: String,
    val showResult: Boolean = false,
    val isCorrect: Boolean? = null
)

data class UiSnapshot(
    val currentIndex: Int = 0,
    val progressLoaded: Boolean = false
)

data class AnalysisSnapshot(
    val deepSeek: List<String> = emptyList(),
    val spark: List<String> = emptyList(),
    val baidu: List<String> = emptyList()
)

data class StatisticsSnapshot(
    val totalCount: Int = 0,
    val answeredCount: Int = 0,
    val sessionScore: Int = 0
)

data class SessionSnapshot(
    val kind: QuestionSessionKind? = null,
    val currentIndex: Int = 0,
    val questions: List<QuestionSnapshot> = emptyList(),
    val ui: UiSnapshot = UiSnapshot(),
    val analysis: AnalysisSnapshot = AnalysisSnapshot(),
    val statistics: StatisticsSnapshot = StatisticsSnapshot()
)
