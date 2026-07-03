package com.example.testapp.presentation.screen.practice

/** 练习页顶栏/返回键退出判定（无 Compose 依赖）。 */
object PracticeSessionExitPipeline {

    sealed class Action {
        data object ReviewBack : Action()
        data object ExitWithoutAnswer : Action()
        data object ShowSubmitDialog : Action()
        data class FinishDirect(
            val sessionScore: Int,
            val sessionAnsweredCount: Int,
            val realUnanswered: Int
        ) : Action()
    }

    fun resolve(
        isReviewMode: Boolean,
        answeredThisSession: Boolean,
        hasSessionInput: Boolean,
        sessionAnsweredCount: Int,
        totalCount: Int,
        sessionScore: Int,
        realUnanswered: Int
    ): Action = when {
        isReviewMode -> Action.ReviewBack
        !answeredThisSession && !hasSessionInput -> Action.ExitWithoutAnswer
        sessionAnsweredCount >= totalCount -> Action.FinishDirect(
            sessionScore = sessionScore,
            sessionAnsweredCount = sessionAnsweredCount,
            realUnanswered = realUnanswered
        )
        else -> Action.ShowSubmitDialog
    }
}
