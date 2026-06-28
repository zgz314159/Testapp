package com.example.testapp.presentation.screen.practice

/** 单题批改后的跳转判定：仍有未答题 → 继续；否则 → 结束/交卷确认。 */
object PracticePostAnswerAdvancePipeline {

    sealed class Action {
        data object Advance : Action()
        data object FinishOrPromptExit : Action()
    }

    fun resolve(hasPendingQuestions: Boolean): Action =
        if (hasPendingQuestions) Action.Advance else Action.FinishOrPromptExit
}
