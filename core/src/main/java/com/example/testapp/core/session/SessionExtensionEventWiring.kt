package com.example.testapp.core.session

import com.example.testapp.domain.model.PracticeSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Practice / Exam Session 共用：从 sessionState 推导 Extension 事件 */
object SessionExtensionEventWiring {
    fun launchQuestionChangedEvents(
        scope: CoroutineScope,
        sessionState: StateFlow<PracticeSessionState>,
        onQuestionChanged: (index: Int, questionId: Int) -> Unit,
    ): Job {
        return scope.launch {
            var prevIndex = -1
            var prevQuestionId: Int? = null
            sessionState.collect { state ->
                val index = state.currentIndex
                val questionId = state.currentQuestion?.question?.id
                if (questionId != null && (index != prevIndex || questionId != prevQuestionId)) {
                    prevIndex = index
                    prevQuestionId = questionId
                    onQuestionChanged(index, questionId)
                }
            }
        }
    }

    fun launchAnswerSubmittedEvents(
        scope: CoroutineScope,
        sessionState: StateFlow<PracticeSessionState>,
        onAnswerSubmitted: (index: Int, questionId: Int) -> Unit,
    ): Job {
        return scope.launch {
            var trackedIndex = -1
            var wasShowingResult = false
            sessionState.collect { state ->
                val current = state.currentQuestion
                if (current == null) {
                    trackedIndex = -1
                    wasShowingResult = false
                    return@collect
                }
                val index = state.currentIndex
                val showing = current.showResult
                if (index != trackedIndex) {
                    trackedIndex = index
                    wasShowingResult = showing
                    return@collect
                }
                if (showing && !wasShowingResult) {
                    onAnswerSubmitted(index, current.question.id)
                }
                wasShowingResult = showing
            }
        }
    }
}
