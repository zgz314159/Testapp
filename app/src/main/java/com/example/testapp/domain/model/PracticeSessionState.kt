package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

/**
 * 练习会话状态
 * 包含当前练习的所有状态信息
 */
@Serializable
data class PracticeSessionState(
    val sessionId: String = "",
    val questionsWithState: List<QuestionWithState> = emptyList(),
    val currentIndex: Int = 0,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val progressId: String = "",
    val questionsSource: String = "", // 题目来源标识
    val isRandomMode: Boolean = false,
    val questionCount: Int = 0, // 限制题目数量，0表示不限制
    val progressLoaded: Boolean = false
) {
    
    /**
     * 总题目数
     */
    val totalCount: Int
        get() = questionsWithState.size
    
    /**
     * 当前题目
     */
    val currentQuestion: QuestionWithState?
        get() = questionsWithState.getOrNull(currentIndex)
    
    /**
     * 已答题数（所有有选择的题目）
     */
    val answeredCount: Int
        get() = questionsWithState.count { it.isAnswered }
    
    /**
     * 答对题数
     */
    val correctCount: Int
        get() = questionsWithState.count { it.isCorrect == true }
    
    /**
     * 答错题数
     */
    val wrongCount: Int
        get() = questionsWithState.count { it.isCorrect == false }
    
    /**
     * 未答题数
     */
    val unansweredCount: Int
        get() = totalCount - answeredCount
    
    /**
     * 本次会话实际答题数（本次会话开始后已显示结果的题目）
     */
    val sessionAnsweredCount: Int
        get() = questionsWithState.count { it.showResult && it.sessionAnswerTime >= sessionStartTime }
    
    /**
     * 本次会话答对数（只计算本次会话开始后答对的题目）
     */
    val sessionCorrectCount: Int
        get() = questionsWithState.count { 
            it.showResult && it.isCorrect == true && it.sessionAnswerTime >= sessionStartTime 
        }
    
    /**
     * 本次会话答错数（本次会话开始后答错的题目）
     */
    val sessionWrongCount: Int
        get() = questionsWithState.count { 
            it.showResult && it.isCorrect == false && it.sessionAnswerTime >= sessionStartTime 
        }
    
    /**
     * 获取已答题目的下标列表
     */
    val answeredIndices: List<Int>
        get() = questionsWithState.mapIndexedNotNull { index, question ->
            if (question.isAnswered) index else null
        }
    
    /**
     * 提供 questions 属性用于向后兼容
     */
    val questions: List<Question>
        get() = questionsWithState.map { it.question }
    
    /**
     * 检查是否可以进入下一题
     */
    val canGoNext: Boolean
        get() = currentIndex < questionsWithState.size - 1
    
    /**
     * 检查是否可以回到上一题
     */
    val canGoPrevious: Boolean
        get() = currentIndex > 0
    
    /**
     * 检查是否所有题目都已答完
     */
    val isAllAnswered: Boolean
        get() = answeredCount >= totalCount
    
    /**
     * 更新当前题目索引
     */
    fun updateCurrentIndex(index: Int): PracticeSessionState {
        return copy(currentIndex = index.coerceIn(0, questionsWithState.size - 1))
    }
    
    /**
     * 下一题
     */
    fun nextQuestion(): PracticeSessionState {
        return if (canGoNext) copy(currentIndex = currentIndex + 1) else this
    }
    
    /**
     * 上一题
     */
    fun previousQuestion(): PracticeSessionState {
        return if (canGoPrevious) copy(currentIndex = currentIndex - 1) else this
    }
    
    /**
     * 跳转到指定题目
     */
    fun goToQuestion(index: Int): PracticeSessionState {
        return copy(currentIndex = index.coerceIn(0, questionsWithState.size - 1))
    }
    
    /**
     * 更新当前题目状态
     */
    fun updateCurrentQuestion(updater: (QuestionWithState) -> QuestionWithState): PracticeSessionState {
        val currentQ = currentQuestion ?: return this
        val updatedQuestions = questionsWithState.toMutableList()
        updatedQuestions[currentIndex] = updater(currentQ)
        return copy(questionsWithState = updatedQuestions)
    }
    
    /**
     * 更新指定题目状态
     */
    fun updateQuestion(index: Int, updater: (QuestionWithState) -> QuestionWithState): PracticeSessionState {
        if (index !in questionsWithState.indices) return this
        val updatedQuestions = questionsWithState.toMutableList()
        updatedQuestions[index] = updater(updatedQuestions[index])
        return copy(questionsWithState = updatedQuestions)
    }
    
    /**
     * 批量更新题目状态
     */
    fun updateQuestions(newQuestions: List<QuestionWithState>): PracticeSessionState {
        return copy(questionsWithState = newQuestions)
    }
    
    /**
     * 回答当前题目
     */
    fun answerCurrentQuestion(selectedOptions: List<Int>): PracticeSessionState {
        return updateCurrentQuestion { question ->
            question.updateSelectedOptions(selectedOptions)
        }
    }
    
    /**
     * 显示当前题目结果
     */
    fun showCurrentResult(): PracticeSessionState {
        return updateCurrentQuestion { question ->
            question.showResult()
        }
    }
    
    /**
     * 单选题/判断题答题（自动显示结果）
     */
    fun answerSingleChoice(selectedOption: Int): PracticeSessionState {
        return updateCurrentQuestion { question ->
            question.updateSelectedOptions(listOf(selectedOption)).showResult()
        }
    }
    
    /**
     * 多选题答题（不自动显示结果）
     */
    fun toggleMultiChoice(selectedOption: Int): PracticeSessionState {
        return updateCurrentQuestion { question ->
            val currentOptions = question.selectedOptions.toMutableList()
            if (currentOptions.contains(selectedOption)) {
                currentOptions.remove(selectedOption)
            } else {
                currentOptions.add(selectedOption)
            }
            question.updateSelectedOptions(currentOptions)
        }
    }
    
    /**
     * 提交多选题答案
     */
    fun submitMultiChoice(): PracticeSessionState {
        return showCurrentResult()
    }
    
    /**
     * 标记进度已加载
     */
    fun markProgressLoaded(): PracticeSessionState {
        return copy(progressLoaded = true)
    }
}
