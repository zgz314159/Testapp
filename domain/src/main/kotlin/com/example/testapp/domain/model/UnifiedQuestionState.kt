package com.example.testapp.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 统一题目状态模型 — 消除 Practice(QuestionAnswerState) 与 Exam(ExamQuestionState) 的差异。
 *
 * 它是 QuestionAnswerState 的超集：
 *   - `answerTime` 替代 `sessionAnswerTime`（Long? 可空，兼容 old 0L 默认值）
 *   - `displayedQuestionContent/Answer` 是 Practice 新增字段，Exam 可忽略
 *   - analysis/sparkAnalysis/baiduAnalysis/note 保留，保证 DB 读写不丢失数据
 */
@Serializable
data class UnifiedQuestionState(
    val questionId: Int,
    val selectedOptions: List<Int> = emptyList(),
    val textAnswer: String = "",
    val showResult: Boolean = false,
    val analysis: String = "",
    val sparkAnalysis: String = "",
    val baiduAnalysis: String = "",
    val note: String = "",
    @SerialName("sessionAnswerTime")
    val answerTime: Long = 0L,

    // Practice 专用（Exam 可忽略）
    val displayedQuestionContent: String? = null,
    val displayedQuestionAnswer: String? = null
)

/**
 * 统一会话运行时状态 — PracticeSessionState 与 Exam 16 个分散 StateFlow 的共同上游。
 * 后续 Phase 6 将 ExamViewModel 从此状态容器接管。
 */
@Serializable
data class UnifiedSessionState(
    val questionsWithState: List<QuestionWithState> = emptyList(),
    val currentIndex: Int = 0,
    val sessionStartTime: Long = System.currentTimeMillis(),
    val mode: SessionMode = SessionMode.PRACTICE,
    val progressId: String = "",
    val questionsSource: String = "",
    val isRandomMode: Boolean = false,
    val questionCount: Int = 0,
    val progressLoaded: Boolean = false,

    // 持久化必需字段
    val finished: Boolean = false,
    val sessionId: String = ""
) {
    val totalCount: Int get() = questionsWithState.size

    val currentQuestion: QuestionWithState?
        get() = questionsWithState.getOrNull(currentIndex)

    val answeredCount: Int
        get() = questionsWithState.count { it.isAnswered }

    val correctCount: Int
        get() = questionsWithState.count { it.isCorrect == true }

    val wrongCount: Int
        get() = questionsWithState.count { it.isCorrect == false }

    val unansweredCount: Int
        get() = totalCount - answeredCount

    val questions: List<Question>
        get() = questionsWithState.map { it.question }

    val canGoNext: Boolean
        get() = currentIndex < questionsWithState.size - 1

    val canGoPrevious: Boolean
        get() = currentIndex > 0

    val sessionAnsweredCount: Int
        get() = questionsWithState.count { it.showResult && it.sessionAnswerTime >= sessionStartTime }

    val sessionCorrectCount: Int
        get() = questionsWithState.count {
            it.showResult && it.isCorrect == true && it.sessionAnswerTime >= sessionStartTime
        }

    val sessionWrongCount: Int
        get() = questionsWithState.count {
            it.showResult && it.isCorrect == false && it.sessionAnswerTime >= sessionStartTime
        }

    val answeredIndices: List<Int>
        get() = questionsWithState.mapIndexedNotNull { index, q ->
            if (q.isAnswered) index else null
        }

    fun updateCurrentIndex(index: Int): UnifiedSessionState =
        copy(currentIndex = index.coerceIn(0, questionsWithState.size - 1))

    fun nextQuestion(): UnifiedSessionState =
        if (canGoNext) copy(currentIndex = currentIndex + 1) else this

    fun previousQuestion(): UnifiedSessionState =
        if (canGoPrevious) copy(currentIndex = currentIndex - 1) else this

    fun goToQuestion(index: Int): UnifiedSessionState =
        copy(currentIndex = index.coerceIn(0, questionsWithState.size - 1))

    fun updateCurrentQuestion(updater: (QuestionWithState) -> QuestionWithState): UnifiedSessionState {
        val currentQ = currentQuestion ?: return this
        val updated = questionsWithState.toMutableList()
        updated[currentIndex] = updater(currentQ)
        return copy(questionsWithState = updated)
    }

    fun updateQuestion(index: Int, updater: (QuestionWithState) -> QuestionWithState): UnifiedSessionState {
        if (index !in questionsWithState.indices) return this
        val updated = questionsWithState.toMutableList()
        updated[index] = updater(updated[index])
        return copy(questionsWithState = updated)
    }

    fun updateQuestions(newQuestions: List<QuestionWithState>): UnifiedSessionState =
        copy(questionsWithState = newQuestions)

    fun answerCurrentQuestion(selectedOptions: List<Int>): UnifiedSessionState =
        updateCurrentQuestion { it.updateSelectedOptions(selectedOptions) }

    fun showCurrentResult(): UnifiedSessionState =
        updateCurrentQuestion { it.showResult() }

    fun answerSingleChoice(selectedOption: Int): UnifiedSessionState =
        updateCurrentQuestion { it.updateSelectedOptions(listOf(selectedOption)).showResult() }

    fun toggleMultiChoice(selectedOption: Int): UnifiedSessionState =
        updateCurrentQuestion { q ->
            val cur = q.selectedOptions.toMutableList()
            if (cur.contains(selectedOption)) cur.remove(selectedOption) else cur.add(selectedOption)
            q.updateSelectedOptions(cur)
        }

    fun submitMultiChoice(): UnifiedSessionState = showCurrentResult()

    fun markProgressLoaded(): UnifiedSessionState = copy(progressLoaded = true)
}

enum class SessionMode { PRACTICE, EXAM }
