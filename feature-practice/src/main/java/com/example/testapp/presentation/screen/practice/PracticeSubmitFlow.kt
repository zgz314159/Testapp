package com.example.testapp.presentation.screen.practice

/** 练习交卷入口判定 — 无状态管道：是否已作答 → 退出 / 确认交卷 */
object PracticeSubmitFlow {
    sealed class Action {
        data object ExitWithoutAnswer : Action()
        data object ShowSubmitDialog : Action()
    }

    fun resolve(answeredThisSession: Boolean, hasAnyInputInSession: Boolean): Action =
        if (answeredThisSession || hasAnyInputInSession) Action.ShowSubmitDialog else Action.ExitWithoutAnswer
}
