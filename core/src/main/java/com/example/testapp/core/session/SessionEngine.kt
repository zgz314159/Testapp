package com.example.testapp.core.session

import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 共享会话引擎 — 消除 Practice ↔ Exam 60-70% 重复的核心抽象。
 *
 * 职责：
 * 1. 统一状态管理（MutableStateFlow<UnifiedSessionState>）
 * 2. 答题交互（answerQuestion / toggleOption / updateTextAnswer）
 * 3. 导航（next / prev / goTo）
 * 4. 持久化委托（SessionProgressManager、SessionAnalysisLoader）
 * 5. 记忆模式（SessionMemoryMode）
 *
 * PracticeViewModel 和 ExamViewModel 各自持有此引擎实例，
 * 模式差异通过 SessionMode 参数区分，考试特有逻辑（评分）保留在 ExamViewModel。
 */
class SessionEngine(
    val progressManager: SessionProgressManager,
    val analysisLoader: SessionAnalysisLoader,
    val memoryMode: SessionMemoryMode
) {
    private val mutex = Mutex()

    private val _state = MutableStateFlow(UnifiedSessionState())
    val state: StateFlow<UnifiedSessionState> = _state.asStateFlow()

    // ========================================================================
    // Question loading
    // ========================================================================

    suspend fun setQuestions(questionsWithState: List<QuestionWithState>, startIndex: Int = 0) {
        mutex.withLock {
            _state.value = _state.value.copy(
                questionsWithState = questionsWithState,
                currentIndex = startIndex
            )
        }
    }

    fun updateState(updater: (UnifiedSessionState) -> UnifiedSessionState) {
        _state.value = updater(_state.value)
    }

    // ========================================================================
    // Answer interaction
    // ========================================================================

    fun answerQuestion(option: Int) {
        val current = _state.value
        val idx = current.currentIndex
        val updatedQuestions = current.questionsWithState.mapIndexed { index, qws ->
            if (index == idx) {
                qws.copy(
                    selectedOptions = listOf(option),
                    showResult = true,
                    sessionAnswerTime = System.currentTimeMillis()
                )
            } else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    fun toggleOption(option: Int) {
        val current = _state.value
        val idx = current.currentIndex
        val updatedQuestions = current.questionsWithState.mapIndexed { index, qws ->
            if (index == idx) {
                val cur = qws.selectedOptions.toMutableList()
                if (cur.contains(option)) cur.remove(option) else cur.add(option)
                qws.copy(selectedOptions = cur)
            } else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    fun updateTextAnswer(answer: String) {
        val current = _state.value
        val idx = current.currentIndex
        val updatedQuestions = current.questionsWithState.mapIndexed { index, qws ->
            if (index == idx) qws.copy(textAnswer = answer)
            else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    // ========================================================================
    // Navigation
    // ========================================================================

    fun nextQuestion(): Boolean {
        val current = _state.value
        return if (current.currentIndex < current.questionsWithState.size - 1) {
            _state.value = current.copy(currentIndex = current.currentIndex + 1)
            true
        } else false
    }

    fun prevQuestion(): Boolean {
        val current = _state.value
        return if (current.currentIndex > 0) {
            _state.value = current.copy(currentIndex = current.currentIndex - 1)
            true
        } else false
    }

    fun goToQuestion(index: Int): Boolean {
        val current = _state.value
        return if (index in current.questionsWithState.indices) {
            _state.value = current.copy(currentIndex = index)
            true
        } else false
    }

    // ========================================================================
    // Show result
    // ========================================================================

    fun showResult(index: Int) {
        val current = _state.value
        val updatedQuestions = current.questionsWithState.mapIndexed { i, qws ->
            if (i == index) qws.showResult()
            else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    fun updateShowResult(index: Int, value: Boolean) {
        val current = _state.value
        val updatedQuestions = current.questionsWithState.mapIndexed { i, qws ->
            if (i == index) qws.copy(
                showResult = value,
                sessionAnswerTime = if (value && qws.sessionAnswerTime == 0L) System.currentTimeMillis() else qws.sessionAnswerTime
            ) else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    // ========================================================================
    // Analysis / Note updates
    // ========================================================================

    fun updateAnalysis(index: Int, text: String) {
        val current = _state.value
        val updatedQuestions = current.questionsWithState.mapIndexed { i, qws ->
            if (i == index) qws.copy(analysis = text) else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    fun updateSparkAnalysis(index: Int, text: String) {
        val current = _state.value
        val updatedQuestions = current.questionsWithState.mapIndexed { i, qws ->
            if (i == index) qws.copy(sparkAnalysis = text) else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    fun updateBaiduAnalysis(index: Int, text: String) {
        val current = _state.value
        val updatedQuestions = current.questionsWithState.mapIndexed { i, qws ->
            if (i == index) qws.copy(baiduAnalysis = text) else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    fun updateNote(index: Int, text: String) {
        val current = _state.value
        val updatedQuestions = current.questionsWithState.mapIndexed { i, qws ->
            if (i == index) qws.copy(note = text) else qws
        }
        _state.value = current.copy(questionsWithState = updatedQuestions)
    }

    // ========================================================================
    // Reset
    // ========================================================================

    fun reset() {
        _state.value = UnifiedSessionState()
    }

    fun resetAnswers() {
        val current = _state.value
        val reset = current.questionsWithState.map { qws ->
            qws.copy(
                selectedOptions = emptyList(),
                textAnswer = "",
                showResult = false,
                sessionAnswerTime = 0L
            )
        }
        _state.value = current.copy(
            questionsWithState = reset,
            currentIndex = 0,
            progressLoaded = false
        )
    }

    /**
     * 更新进度标记（当进度从 DB 恢复后调用）
     */
    fun markProgressLoaded() {
        _state.value = _state.value.copy(progressLoaded = true)
    }

    // ========================================================================
    // Stateless helpers for ViewModels
    // ========================================================================

    companion object {
        /**
         * 在 QuestionWithState 列表中按 index 更新单个元素
         */
        fun modifyQuestionsWithState(
            list: List<QuestionWithState>,
            index: Int,
            transform: (QuestionWithState) -> QuestionWithState
        ): List<QuestionWithState> = list.mapIndexed { i, qws ->
            if (i == index) transform(qws) else qws
        }
    }
}
