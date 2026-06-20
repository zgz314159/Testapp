package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.updateAt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExamAnswerCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val memoryModeActive: () -> Boolean,
    private val randomExamEnabled: () -> Boolean,
    private val currentFullAnswerCandidateIndices: (List<Int>) -> List<Int>,
    private val refreshMemoryRoundPoolIfNeeded: suspend (Int) -> Boolean,
    private val navigateToRandomUnansweredOrAdvanceRound: suspend () -> Unit,
    private val calculateCumulativeStats: () -> Unit,
    private val saveProgress: () -> Unit,
    private val saveProgressInternal: suspend () -> Unit
) {

    fun selectOption(option: Int, skipAfterChanged: Boolean = false) {
        val idx = sessionState.value.currentIndex
        val question = sessionState.value.questions.getOrNull(idx) ?: return
        if (QuestionTypes.isFill(question.type)) return

        sessionState.update { state ->
            state.updateAt(idx) { qws ->
                val selected = qws.selectedOptions.toMutableList()
                if (QuestionTypes.isMulti(question.type)) {
                    if (selected.contains(option)) selected.remove(option) else selected.add(option)
                } else {
                    selected.clear()
                    selected.add(option)
                }
                qws.copy(selectedOptions = selected)
            }
        }

        if (!skipAfterChanged) afterAnswerChanged(idx)
    }

    fun updateTextAnswer(answer: String) {
        val idx = sessionState.value.currentIndex
        sessionState.update { state ->
            state.updateAt(idx) { qws ->
                qws.copy(
                    textAnswer = answer,
                    selectedOptions = if (answer.isNotBlank()) listOf(-1) else emptyList()
                )
            }
        }
    }

    private fun afterAnswerChanged(index: Int) {
        if (memoryModeActive()) {
            scope.launch {
                refreshMemoryRoundPoolIfNeeded(index)
                if (randomExamEnabled()) navigateToRandomUnansweredOrAdvanceRound()
                calculateCumulativeStats()
                saveProgressInternal()
            }
            return
        }

        if (randomExamEnabled()) {
            val state = sessionState.value
            val unanswered = state.questionsWithState.indices.filter { i ->
                state.questionsWithState[i].selectedOptions.isEmpty()
            }
            if (unanswered.isNotEmpty()) {
                sessionState.update { it.copy(currentIndex = currentFullAnswerCandidateIndices(unanswered).random()) }
            }
        }

        calculateCumulativeStats()
        saveProgress()
    }
}
