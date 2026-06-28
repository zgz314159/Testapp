package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState

/** 全答+须全对：轮次池内已作答但答错，单击重开 */
object ExamFullAnswerIconRetryPipeline {

    fun shouldReopenForWrongRetry(
        questionWithState: QuestionWithState?,
        fullAnswerRequireCorrect: Boolean
    ): Boolean {
        if (!fullAnswerRequireCorrect) return false
        val qws = questionWithState ?: return false
        return qws.showResult && qws.isCorrect != true
    }

    fun resolveStayIndexForWrongRetry(
        state: PracticeSessionState,
        fullAnswerRequireCorrect: Boolean
    ): Int? {
        if (!shouldReopenForWrongRetry(
                state.questionsWithState.getOrNull(state.currentIndex),
                fullAnswerRequireCorrect
            )
        ) {
            return null
        }
        return state.currentIndex
    }
}
