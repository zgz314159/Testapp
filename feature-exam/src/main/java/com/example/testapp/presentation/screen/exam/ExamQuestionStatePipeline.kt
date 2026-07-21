package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.updateAt

internal object ExamQuestionStatePipeline {
    fun toUnified(state: PracticeSessionState, index: Int): UnifiedQuestionState {
        val questionState = state.questionsWithState.getOrNull(index)
            ?: return UnifiedQuestionState(questionId = -1)
        return UnifiedQuestionState(
            questionId = questionState.question.id,
            selectedOptions = questionState.selectedOptions,
            textAnswer = questionState.textAnswer,
            showResult = questionState.showResult,
            analysis = questionState.analysis,
            sparkAnalysis = questionState.sparkAnalysis,
            baiduAnalysis = questionState.baiduAnalysis,
            note = questionState.note,
            answerTime = questionState.sessionAnswerTime
        )
    }

    fun retryCurrent(state: PracticeSessionState, index: Int): PracticeSessionState =
        retry(state, index, ExamQuestionRetryPipeline::reopenCurrent)

    fun retryWrongFillBlanks(state: PracticeSessionState, index: Int): PracticeSessionState =
        retry(state, index, ExamQuestionRetryPipeline::reopenWrongBlanks)

    fun updateShowResult(
        state: PracticeSessionState,
        index: Int,
        showResult: Boolean,
        now: Long = System.currentTimeMillis()
    ): PracticeSessionState =
        state.updateAt(index) { questionState ->
            if (showResult && questionState.sessionAnswerTime == 0L) {
                questionState.copy(showResult = true, sessionAnswerTime = now)
            } else {
                questionState.copy(showResult = showResult)
            }
        }

    private inline fun retry(
        state: PracticeSessionState,
        index: Int,
        reopen: (com.example.testapp.domain.model.QuestionWithState) ->
            com.example.testapp.domain.model.QuestionWithState
    ): PracticeSessionState {
        if (index !in state.questionsWithState.indices) return state
        return state.copy(
            questionsWithState = state.questionsWithState.mapIndexed { currentIndex, questionState ->
                if (currentIndex == index) reopen(questionState) else questionState
            },
            currentIndex = index,
            finished = false
        )
    }
}
