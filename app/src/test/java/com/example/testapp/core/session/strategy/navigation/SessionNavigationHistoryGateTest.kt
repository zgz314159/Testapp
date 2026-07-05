package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionNavigationHistoryGateTest {
    @Test
    fun practice_tracksSnapshots_and_preparesForward() {
        val orch =
            SessionNavigationOrchestrationResolver.from(
                SessionNavigationConfig(
                    mode = SessionNavigationMode.PRACTICE_INTERACTIVE,
                    swipeAnsweredHistory = true,
                ),
            )
        assertTrue(SessionNavigationHistoryGate.allowsAnsweredHistoryBrowse(orch))
        assertTrue(SessionNavigationHistoryGate.shouldTrackAnsweredSnapshots(orch))
        assertTrue(SessionNavigationHistoryGate.shouldPrepareForwardFromAnsweredHistory(orch))
        assertTrue(SessionNavigationHistoryGate.shouldClearHistoryOnManualJump(orch))
        assertFalse(SessionNavigationHistoryGate.isReviewPostAnswerNavOnly(orch))
    }

    @Test
    fun review_postAnswerNavOnly_noForwardPrepare() {
        val orch =
            SessionNavigationOrchestrationResolver.from(
                SessionNavigationConfig(
                    mode = SessionNavigationMode.REVIEW_HISTORY,
                    swipeAnsweredHistory = true,
                ),
            )
        assertTrue(SessionNavigationHistoryGate.allowsAnsweredHistoryBrowse(orch))
        assertTrue(SessionNavigationHistoryGate.isReviewPostAnswerNavOnly(orch))
        assertFalse(SessionNavigationHistoryGate.shouldPrepareForwardFromAnsweredHistory(orch))
    }

    @Test
    fun exam_noHistoryBrowse() {
        val orch = SessionNavigationOrchestrationResolver.examDefault()
        assertFalse(SessionNavigationHistoryGate.allowsAnsweredHistoryBrowse(orch))
        assertFalse(SessionNavigationHistoryGate.shouldTrackAnsweredSnapshots(orch))
    }
}
