package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import kotlinx.coroutines.flow.MutableStateFlow

class PracticeSimpleNavigationCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val randomPracticeEnabled: () -> Boolean,
    private val saveProgress: () -> Unit
) {
    fun nextQuestion() {
        val currentState = sessionState.value
        if (randomPracticeEnabled()) {
            val unansweredIndices = currentState.questionsWithState.mapIndexedNotNull { index, qws ->
                if (qws.selectedOptions.isEmpty()) index else null
            }
            if (unansweredIndices.isNotEmpty()) {
                sessionState.value = currentState.copy(
                    currentIndex = unansweredIndices.random(kotlin.random.Random(currentState.sessionStartTime))
                )
            }
        } else if (currentState.currentIndex < currentState.questionsWithState.size - 1) {
            sessionState.value = currentState.copy(currentIndex = currentState.currentIndex + 1)
        }
        saveProgress()
    }

    fun prevQuestion() {
        val currentState = sessionState.value
        if (currentState.currentIndex > 0) {
            sessionState.value = currentState.copy(currentIndex = currentState.currentIndex - 1)
            saveProgress()
        }
    }

    fun goToQuestion(index: Int) {
        val currentState = sessionState.value
        if (index in 0 until currentState.questionsWithState.size) {
            sessionState.value = currentState.copy(currentIndex = index)
            saveProgress()
        }
    }
}
