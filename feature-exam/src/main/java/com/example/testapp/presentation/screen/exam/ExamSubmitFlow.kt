package com.example.testapp.presentation.screen.exam

/**
 * 考试交卷入口判定 — 无状态管道：是否已作答 → 退出 / 确认交卷
 */
object ExamSubmitFlow {
    sealed class Action {
        data object ExitWithoutAnswer : Action()
        data object ShowSubmitDialog : Action()
    }

    fun resolve(answeredThisSession: Boolean): Action =
        if (answeredThisSession) Action.ShowSubmitDialog else Action.ExitWithoutAnswer
}
