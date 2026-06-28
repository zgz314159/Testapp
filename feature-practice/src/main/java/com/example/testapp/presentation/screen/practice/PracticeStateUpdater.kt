package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.updateAt
import kotlinx.coroutines.flow.MutableStateFlow

class PracticeStateUpdater(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val saveProgress: () -> Unit
) {
    fun answerQuestion(option: Int) {
        val currentState = sessionState.value
        val idx = currentState.currentIndex
        sessionState.value = currentState.updateAt(idx) {
            it.copy(
                selectedOptions = listOf(option),
                sessionAnswerTime = System.currentTimeMillis()
            )
        }
        saveProgress()
    }

    fun toggleOption(option: Int) {
        sessionState.value = sessionState.value.updateAt(sessionState.value.currentIndex) { qws ->
            val current = qws.selectedOptions.toMutableList()
            if (current.contains(option)) current.remove(option) else current.add(option)
            qws.copy(selectedOptions = current)
        }
        saveProgress()
    }

    fun updateShowResult(index: Int, value: Boolean) {
        sessionState.value = sessionState.value.updateAt(index) { qws ->
            if (value && qws.sessionAnswerTime == 0L) {
                qws.copy(showResult = value, sessionAnswerTime = System.currentTimeMillis())
            } else {
                qws.copy(showResult = value)
            }
        }
        saveProgress()
    }

    /** 立即展示批改区，不触发持久化（由 finalize 统一落盘）。 */
    fun revealShowResult(index: Int) {
        sessionState.value = sessionState.value.updateAt(index) { qws ->
            if (qws.sessionAnswerTime == 0L) {
                qws.copy(showResult = true, sessionAnswerTime = System.currentTimeMillis())
            } else {
                qws.copy(showResult = true)
            }
        }
    }

    fun updateAnalysis(index: Int, text: String) {
        val current = sessionState.value.questionsWithState.getOrNull(index)?.analysis
        if (current == text) return
        sessionState.value = sessionState.value.updateAt(index) { it.copy(analysis = text) }
        saveProgress()
    }

    fun updateSparkAnalysis(index: Int, text: String) {
        val current = sessionState.value.questionsWithState.getOrNull(index)?.sparkAnalysis
        if (current == text) return
        sessionState.value = sessionState.value.updateAt(index) { it.copy(sparkAnalysis = text) }
        saveProgress()
    }

    fun updateBaiduAnalysis(index: Int, text: String) {
        val current = sessionState.value.questionsWithState.getOrNull(index)?.baiduAnalysis
        if (current == text) return
        sessionState.value = sessionState.value.updateAt(index) { it.copy(baiduAnalysis = text) }
        saveProgress()
    }

    fun updateTextAnswer(answer: String) {
        val idx = sessionState.value.currentIndex
        sessionState.value = sessionState.value.updateAt(idx) {
            it.copy(textAnswer = answer, selectedOptions = if (answer.isNotBlank()) listOf(-1) else emptyList())
        }
        saveProgress()
    }
}
