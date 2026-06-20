package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.UnifiedQuestionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExamNavigationCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val navHelper: ExamNavigationHelper,
    private val fullAnswerRequireCorrect: () -> Boolean,
    private val randomExamEnabled: () -> Boolean,
    private val memoryModeActive: () -> Boolean,
    private val effectiveCurrentMemoryRoundQuestionIds: () -> Set<Int>,
    private val buildExamQuestionState: (Int) -> UnifiedQuestionState,
    private val advanceMemoryRoundIfNeeded: suspend () -> Boolean,
    private val saveProgress: () -> Unit,
    private val saveProgressInternal: suspend () -> Unit
) {

    fun currentFullAnswerCandidateIndices(candidates: List<Int>): List<Int> {
        val state = sessionState.value
        return navHelper.currentFullAnswerCandidateIndices(
            questions = state.questions,
            currentIndex = state.currentIndex,
            eligible = candidates,
            fullAnswerRequireCorrect = fullAnswerRequireCorrect(),
            buildState = buildExamQuestionState
        )
    }

    fun navigateCandidateIndices(): List<Int> {
        val state = sessionState.value
        return navHelper.navigateCandidateIndices(
            questions = state.questions,
            currentIndex = state.currentIndex,
            fullAnswerRequireCorrect = fullAnswerRequireCorrect(),
            memoryActive = memoryModeActive(),
            roundIds = effectiveCurrentMemoryRoundQuestionIds(),
            buildState = buildExamQuestionState
        )
    }

    suspend fun navigateToRandomUnansweredOrAdvanceRound() {
        val candidates = navigateCandidateIndices()
        if (candidates.isNotEmpty()) {
            sessionState.update { it.copy(currentIndex = candidates.random()) }
            return
        }
        advanceMemoryRoundIfNeeded()
    }

    fun nextQuestion() {
        val state = sessionState.value
        if (randomExamEnabled()) {
            navigateRandomOrAdvanceRound()
        } else if (state.currentIndex < state.questionsWithState.size - 1) {
            sessionState.update { it.copy(currentIndex = state.currentIndex + 1) }
            saveProgress()
        }
    }

    fun prevQuestion() {
        val state = sessionState.value
        if (randomExamEnabled()) {
            navigateRandomOrAdvanceRound()
        } else if (state.currentIndex > 0) {
            sessionState.update { it.copy(currentIndex = state.currentIndex - 1) }
            saveProgress()
        }
    }

    fun goToQuestion(index: Int) {
        if (index in sessionState.value.questionsWithState.indices) {
            sessionState.update { it.copy(currentIndex = index) }
            saveProgress()
        }
    }

    private fun navigateRandomOrAdvanceRound() {
        val candidates = navigateCandidateIndices()
        if (candidates.isNotEmpty()) {
            sessionState.update { it.copy(currentIndex = candidates.random()) }
            saveProgress()
        } else if (memoryModeActive()) {
            scope.launch {
                if (advanceMemoryRoundIfNeeded()) saveProgressInternal()
            }
        }
    }
}
