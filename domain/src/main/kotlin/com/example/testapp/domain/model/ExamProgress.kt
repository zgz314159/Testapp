package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

data class ExamProgress(
    val id: String = "exam_default",
    val currentIndex: Int,
    val selectedOptions: List<List<Int>>,
    val showResultList: List<Boolean>,
    val analysisList: List<String>,
    val sparkAnalysisList: List<String> = emptyList(),
    val baiduAnalysisList: List<String> = emptyList(),
    val noteList: List<String>,
    val finished: Boolean,
    val timestamp: Long,
    val sessionId: String = "",
    val fixedQuestionOrder: List<Int> = emptyList(),
    val questionStateMap: Map<Int, UnifiedQuestionState> = emptyMap()
)

/**
 * @deprecated 改用 UnifiedQuestionState，保留旧名作为 JSON 反序列化兼容入口。
 *             序列化时请使用 UnifiedQuestionState。
 */
@Serializable
@Deprecated("Use UnifiedQuestionState instead", ReplaceWith("UnifiedQuestionState"))
data class ExamQuestionState(
    val questionId: Int,
    val selectedOptions: List<Int> = emptyList(),
    val textAnswer: String = "",
    val showResult: Boolean = false,
    val analysis: String = "",
    val sparkAnalysis: String = "",
    val baiduAnalysis: String = "",
    val note: String = ""
)
