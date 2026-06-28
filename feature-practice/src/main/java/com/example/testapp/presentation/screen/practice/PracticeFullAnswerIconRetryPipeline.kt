package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState

/** 全答+须全对：单槽词条答错后，底栏图标应留在当前题重做，不跨词条。 */
object PracticeFullAnswerIconRetryPipeline {

    fun shouldReopenCurrentForWrongRetry(
        questionWithState: QuestionWithState?,
        sourceComplete: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): Boolean {
        if (!fullAnswerRequireCorrect || sourceComplete) return false
        val qws = questionWithState ?: return false
        return qws.showResult && qws.isCorrect != true
    }

    fun resolveStayIndexForWrongRetry(
        state: PracticeSessionState,
        sourceComplete: Boolean,
        fullAnswerRequireCorrect: Boolean
    ): Int? {
        if (!shouldReopenCurrentForWrongRetry(
                state.questionsWithState.getOrNull(state.currentIndex),
                sourceComplete,
                fullAnswerRequireCorrect
            )
        ) {
            return null
        }
        return state.currentIndex
    }
}
