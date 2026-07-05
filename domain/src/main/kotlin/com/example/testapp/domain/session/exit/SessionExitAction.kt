package com.example.testapp.domain.session.exit

sealed class SessionExitAction {
    data object ReviewBack : SessionExitAction()
    data object ExitWithoutAnswer : SessionExitAction()
    data object ShowSubmitDialog : SessionExitAction()
    /** Exam：由 UI 调 gradeExam 后结束 */
    data object FinishDirect : SessionExitAction()
    /** Practice：携带当次统计直接结束 */
    data class FinishWithStats(
        val sessionScore: Int,
        val sessionAnsweredCount: Int,
        val realUnanswered: Int
    ) : SessionExitAction()
}
