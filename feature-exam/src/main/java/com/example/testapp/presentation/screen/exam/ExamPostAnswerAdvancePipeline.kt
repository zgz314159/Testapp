package com.example.testapp.presentation.screen.exam

/** 单题作答后的跳转判定：仍有未答题 → 继续；否则 → 交卷确认 */
object ExamPostAnswerAdvancePipeline {

    sealed class Action {
        data object Advance : Action()
        data object PromptSubmit : Action()
    }

    fun resolve(hasPendingQuestions: Boolean): Action =
        if (hasPendingQuestions) Action.Advance else Action.PromptSubmit
}
