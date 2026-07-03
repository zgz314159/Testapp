package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.core.util.retainCorrectFillAnswerParts
import com.example.testapp.core.util.resolveFillCorrectAnswer
import kotlinx.coroutines.flow.MutableStateFlow

/** Reopen / retry question state for full-answer and pending flows. */
internal object PracticeQuestionReopenPipeline {

    fun reopenForPendingRetry(
        sessionState: MutableStateFlow<PracticeSessionState>,
        index: Int,
        onSnapshot: (Int) -> Unit,
        onSaved: () -> Unit
    ) {
        val currentState = sessionState.value
        if (index !in currentState.questionsWithState.indices) return
        onSnapshot(index)
        sessionState.value = currentState.copy(
            currentIndex = index,
            questionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
                if (idx == index) {
                    questionWithState.copy(showResult = false, sessionAnswerTime = 0L)
                } else {
                    questionWithState
                }
            }
        )
        onSaved()
    }

    fun reopenForFullAnswerRetry(
        sessionState: MutableStateFlow<PracticeSessionState>,
        index: Int,
        onSnapshot: (Int) -> Unit,
        onSaved: () -> Unit
    ) {
        val currentState = sessionState.value
        if (index !in currentState.questionsWithState.indices) return
        onSnapshot(index)
        val targetQuestion = currentState.questionsWithState[index]
        sessionState.value = currentState.copy(
            currentIndex = index,
            questionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
                if (idx != index) {
                    questionWithState
                } else if (QuestionTypes.isFill(questionWithState.question.type)) {
                    val retainedAnswer = retainCorrectFillAnswerParts(
                        userAnswer = targetQuestion.textAnswer,
                        correctAnswer = resolveFillCorrectAnswer(targetQuestion.question)
                    )
                    questionWithState.copy(
                        textAnswer = retainedAnswer,
                        selectedOptions = if (retainedAnswer.isNotBlank()) listOf(-1) else emptyList(),
                        showResult = false,
                        sessionAnswerTime = 0L
                    )
                } else {
                    questionWithState.copy(
                        selectedOptions = emptyList(),
                        textAnswer = "",
                        showResult = false,
                        sessionAnswerTime = 0L
                    )
                }
            }
        )
        onSaved()
    }
}
