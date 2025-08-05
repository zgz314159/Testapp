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
    // 🚀 新增：固定题序支持
    val sessionId: String = "", // 会话ID，用于区分不同轮次的考试
    val fixedQuestionOrder: List<Int> = emptyList(), // 固定的题目ID顺序
    val questionStateMap: Map<Int, ExamQuestionState> = emptyMap() // 题目ID -> 答题状态映射
)

/**
 * 考试题目答题状态
 * 与题目ID严格绑定，不依赖位置
 */
@Serializable
data class ExamQuestionState(
    val questionId: Int,
    val selectedOptions: List<Int> = emptyList(),
    val showResult: Boolean = false,
    val analysis: String = "",
    val sparkAnalysis: String = "",
    val baiduAnalysis: String = "",
    val note: String = ""
)