package com.example.testapp.core.session.policy

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitAction
import com.example.testapp.domain.session.exit.SessionExitRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExitPolicyFactoryTest {
    @Test
    fun browse_neverShowsSubmitDialog() {
        assertEquals(
            SessionExitAction.ExitWithoutAnswer,
            ExitPolicyFactory.browse.resolve(
                SessionExitRequest(
                    answeredThisSession = true,
                    hasSessionInput = true,
                    sessionAnsweredCount = 5,
                    totalCount = 10,
                ),
            ),
        )
    }

    @Test
    fun review_alwaysReviewBack() {
        assertEquals(
            SessionExitAction.ReviewBack,
            ExitPolicyFactory.review.resolve(SessionExitRequest()),
        )
    }

    @Test
    fun practice_reviewMode_viaForKind() {
        assertEquals(
            SessionExitAction.ReviewBack,
            ExitPolicyFactory.forKind(
                QuestionSessionKind.Review(progressId = "p1"),
            ).resolve(SessionExitRequest()),
        )
    }

    @Test
    fun practice_noInput_exitWithoutAnswer() {
        assertEquals(
            SessionExitAction.ExitWithoutAnswer,
            ExitPolicyFactory.practice.resolve(
                SessionExitRequest(
                    answeredThisSession = false,
                    hasSessionInput = false,
                    totalCount = 10,
                    realUnanswered = 10,
                ),
            ),
        )
    }

    @Test
    fun practice_allAnswered_finishWithStats() {
        val action =
            ExitPolicyFactory.practice.resolve(
                SessionExitRequest(
                    answeredThisSession = true,
                    hasSessionInput = true,
                    sessionAnsweredCount = 10,
                    totalCount = 10,
                    sessionScore = 8,
                    realUnanswered = 2,
                ),
            )
        assertTrue(action is SessionExitAction.FinishWithStats)
        action as SessionExitAction.FinishWithStats
        assertEquals(8, action.sessionScore)
        assertEquals(10, action.sessionAnsweredCount)
        assertEquals(2, action.realUnanswered)
    }

    @Test
    fun practice_partialProgress_showSubmitDialog() {
        assertEquals(
            SessionExitAction.ShowSubmitDialog,
            ExitPolicyFactory.practice.resolve(
                SessionExitRequest(
                    answeredThisSession = false,
                    hasSessionInput = true,
                    sessionAnsweredCount = 3,
                    totalCount = 10,
                ),
            ),
        )
    }

    @Test
    fun exam_notAnswered_exitWithoutAnswer() {
        assertEquals(
            SessionExitAction.ExitWithoutAnswer,
            ExitPolicyFactory.exam.resolve(
                SessionExitRequest(answeredThisSession = false, hasPendingQuestions = true),
            ),
        )
    }

    @Test
    fun exam_pending_showSubmitDialog() {
        assertEquals(
            SessionExitAction.ShowSubmitDialog,
            ExitPolicyFactory.exam.resolve(
                SessionExitRequest(answeredThisSession = true, hasPendingQuestions = true),
            ),
        )
    }

    @Test
    fun exam_complete_finishDirect() {
        assertEquals(
            SessionExitAction.FinishDirect,
            ExitPolicyFactory.exam.resolve(
                SessionExitRequest(answeredThisSession = true, hasPendingQuestions = false),
            ),
        )
    }

    @Test
    fun examReview_usesReviewPolicy() {
        assertEquals(
            SessionExitAction.ReviewBack,
            ExitPolicyFactory.forKind(
                QuestionSessionKind.Exam(quizId = "q", reviewProgressId = "r1"),
            ).resolve(SessionExitRequest(answeredThisSession = true)),
        )
    }
}
