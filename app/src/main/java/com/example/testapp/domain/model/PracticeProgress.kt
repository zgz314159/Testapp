package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

data class PracticeProgress(
    val id: String = "practice_default",
    val currentIndex: Int,
    val answeredList: List<Int>,
    val selectedOptions: List<List<Int>>, // 支持多选！
    val showResultList: List<Boolean>, // 新增：每题是否已提交/显示解析
    val analysisList: List<String>,
    val sparkAnalysisList: List<String> = emptyList(),
    val baiduAnalysisList: List<String> = emptyList(),
    val noteList: List<String>,
    val timestamp: Long,
    // 🚀 新增：固定题序支持
    val sessionId: String = "", // 会话ID，用于区分不同轮次的练习
    val fixedQuestionOrder: List<Int> = emptyList(), // 固定的题目ID顺序
    val questionStateMap: Map<Int, QuestionAnswerState> = emptyMap() // 题目ID -> 答题状态映射
)

/**
 * 题目答题状态
 * 与题目ID严格绑定，不依赖位置
 */
@Serializable
data class QuestionAnswerState(
    val questionId: Int,
    val selectedOptions: List<Int> = emptyList(),
    val showResult: Boolean = false,
    val analysis: String = "",
    val sparkAnalysis: String = "",
    val baiduAnalysis: String = "",
    val note: String = "",
    val sessionAnswerTime: Long = 0L // 本会话答题时间戳
)
