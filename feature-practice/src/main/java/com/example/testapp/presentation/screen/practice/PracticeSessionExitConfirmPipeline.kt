package com.example.testapp.presentation.screen.practice

/** 交卷确认后的结果页参数（无 side effect）。 */
object PracticeSessionExitConfirmPipeline {

    data class QuizEndParams(
        val sessionScore: Int,
        val sessionAnsweredForDisplay: Int,
        val realUnanswered: Int,
        val shouldRecordHistory: Boolean
    )

    fun buildQuizEndParams(
        graded: PracticeSessionGradeSnapshot,
        sessionInputCount: Int,
        totalCount: Int
    ): QuizEndParams {
        val realUnanswered = totalCount - graded.answeredCount
        val answeredForDisplay = graded.sessionAnsweredCount.coerceAtLeast(sessionInputCount)
        return QuizEndParams(
            sessionScore = graded.sessionCorrectCount,
            sessionAnsweredForDisplay = answeredForDisplay,
            realUnanswered = realUnanswered,
            shouldRecordHistory = answeredForDisplay > 0
        )
    }
}
