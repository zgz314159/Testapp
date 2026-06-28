package com.example.testapp.presentation.screen.exam

/** 答题区边缘/末题左滑判定 — 无状态管道 */
object ExamEdgeSwipePipeline {

    sealed class ForwardAction {
        data object NextQuestion : ForwardAction()
        data object PromptSubmit : ForwardAction()
        data object ExitWithoutAnswer : ForwardAction()
    }

    fun resolveForwardSwipe(
        answeredThisSession: Boolean,
        canNavigateNext: Boolean
    ): ForwardAction = when {
        canNavigateNext -> ForwardAction.NextQuestion
        !answeredThisSession -> ForwardAction.ExitWithoutAnswer
        else -> ForwardAction.PromptSubmit
    }
}
