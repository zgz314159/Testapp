package com.example.testapp.presentation.screen.exam

/** Exam top-bar / back-key exit decision (no Compose). */
object ExamSessionExitPipeline {

    sealed class Action {
        data object ReviewBack : Action()
        data object ExitWithoutAnswer : Action()
        data object ShowSubmitDialog : Action()
        data object FinishDirect : Action()
    }

    fun resolve(
        isReviewMode: Boolean,
        answeredThisSession: Boolean,
        hasPendingQuestions: Boolean
    ): Action = when {
        isReviewMode -> Action.ReviewBack
        !answeredThisSession -> Action.ExitWithoutAnswer
        hasPendingQuestions -> Action.ShowSubmitDialog
        else -> Action.FinishDirect
    }
}
