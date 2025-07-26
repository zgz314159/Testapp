package com.example.testapp.domain.model

import kotlinx.serialization.Serializable
import com.example.testapp.util.answerLettersToIndices

/**
 * 统一的题目+状态数据模型
 * 用于替代分散的状态管理，实现单一数据源
 */
@Serializable
data class QuestionWithState(
    val question: Question,
    val selectedOptions: List<Int> = emptyList(),
    val showResult: Boolean = false,
    val analysis: String = "",
    val sparkAnalysis: String = "",
    val baiduAnalysis: String = "",
    val note: String = "",
    val isFavorite: Boolean = false,
    val sessionAnswerTime: Long = 0L // 答题时间戳，用于区分不同session
) {
    /**
     * 计算属性：是否已答题
     */
    val isAnswered: Boolean
        get() = selectedOptions.isNotEmpty()
    
    /**
     * 计算属性：是否答对
     */
    val isCorrect: Boolean?
        get() = if (!isAnswered || !showResult) {
            null
        } else {
            selectedOptions.sorted() == answerLettersToIndices(question.answer).sorted()
        }
    /**
     * 计算属性：答题状态
     */
    val answerStatus: AnswerStatus
        get() = when {
            selectedOptions.isEmpty() -> AnswerStatus.UNANSWERED
            showResult && isCorrect == true -> AnswerStatus.CORRECT
            showResult && isCorrect == false -> AnswerStatus.INCORRECT
            else -> AnswerStatus.ANSWERED_NOT_SHOWN
        }
    
    /**
     * 计算属性：是否已显示结果
     */
    val hasShownResult: Boolean
        get() = showResult
    
    /**
     * 计算属性：是否有AI解析
     */
    val hasAnalysis: Boolean
        get() = analysis.isNotBlank() || sparkAnalysis.isNotBlank() || baiduAnalysis.isNotBlank()
    
    /**
     * 计算属性：是否有笔记
     */
    val hasNote: Boolean
        get() = note.isNotBlank()
    
    /**
     * 更新选项
     */
    fun updateSelectedOptions(options: List<Int>): QuestionWithState {
        return copy(selectedOptions = options)
    }
    
    /**
     * 显示答题结果
     */
    fun showResult(): QuestionWithState {
        return copy(
            showResult = true,
            sessionAnswerTime = if (sessionAnswerTime == 0L) System.currentTimeMillis() else sessionAnswerTime
        )
    }
    
    /**
     * 更新AI解析
     */
    fun updateAnalysis(analysis: String): QuestionWithState {
        return copy(analysis = analysis)
    }
    
    /**
     * 更新Spark解析
     */
    fun updateSparkAnalysis(sparkAnalysis: String): QuestionWithState {
        return copy(sparkAnalysis = sparkAnalysis)
    }
    
    /**
     * 更新百度解析
     */
    fun updateBaiduAnalysis(baiduAnalysis: String): QuestionWithState {
        return copy(baiduAnalysis = baiduAnalysis)
    }
    
    /**
     * 更新笔记
     */
    fun updateNote(note: String): QuestionWithState {
        return copy(note = note)
    }
    
    /**
     * 更新收藏状态
     */
    fun updateFavorite(isFavorite: Boolean): QuestionWithState {
        return copy(isFavorite = isFavorite)
    }
}

/**
 * 答题状态枚举
 */
enum class AnswerStatus {
    UNANSWERED,         // 未答题
    ANSWERED_NOT_SHOWN, // 已答题但未显示结果
    CORRECT,            // 答对
    INCORRECT           // 答错
}
