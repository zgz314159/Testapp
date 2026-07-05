package com.example.testapp.core.session.strategy.exit

import com.example.testapp.core.session.policy.ExitPolicyFactory
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitAction
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.exit.SessionExitMode
import com.example.testapp.domain.session.exit.SessionExitRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionExitGateTest {
    @Test
    fun browse_neverShowsSubmitDialog() {
        val config = ExitPolicyFactory.configForKind(QuestionSessionKind.Browse("f", 1))
        assertFalse(SessionExitGate.allowsSubmitDialogOnExit(config))
        assertEquals(
            SessionExitAction.ExitWithoutAnswer,
            SessionExitGate.resolveAction(
                config,
                SessionExitRequest(answeredThisSession = true, hasSessionInput = true),
            ),
        )
    }

    @Test
    fun review_alwaysReviewBack() {
        val config = SessionExitConfig(SessionExitMode.REVIEW)
        assertTrue(SessionExitGate.isReviewBackExit(config))
        assertEquals(
            SessionExitAction.ReviewBack,
            SessionExitGate.resolveAction(config, SessionExitRequest()),
        )
    }

    @Test
    fun questionEdit_usesBrowseMode() {
        val config =
            ExitPolicyFactory.configForKind(
                QuestionSessionKind.QuestionEdit("f", questionId = 1),
            )
        assertEquals(SessionExitMode.BROWSE, config.mode)
        assertFalse(SessionExitGate.allowsSubmitDialogOnExit(config))
    }

    @Test
    fun practice_allAnswered_finishesWithStats() {
        val config = SessionExitConfig(SessionExitMode.PRACTICE)
        val action =
            SessionExitGate.resolveAction(
                config,
                SessionExitRequest(
                    answeredThisSession = true,
                    hasSessionInput = true,
                    sessionAnsweredCount = 10,
                    totalCount = 10,
                    sessionScore = 8,
                    realUnanswered = 0,
                ),
            )
        assertTrue(action is SessionExitAction.FinishWithStats)
    }

    @Test
    fun exam_pendingQuestions_showsSubmitDialog() {
        val config = SessionExitConfig(SessionExitMode.EXAM)
        assertTrue(SessionExitGate.allowsSubmitDialogOnExit(config))
        assertEquals(
            SessionExitAction.ShowSubmitDialog,
            SessionExitGate.resolveAction(
                config,
                SessionExitRequest(answeredThisSession = true, hasPendingQuestions = true),
            ),
        )
    }
}
