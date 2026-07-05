package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionNavigationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPracticePostAnswerNavigationPipelineTest {
    private val practiceOrch =
        SessionNavigationOrchestrationResolver.from(
            com.example.testapp.domain.session.navigation.SessionNavigationConfig(
                mode = SessionNavigationMode.PRACTICE_INTERACTIVE,
                swipeAnsweredHistory = true,
            ),
        )

    private val browseOrch =
        SessionNavigationOrchestrationResolver.from(
            com.example.testapp.domain.session.navigation.SessionNavigationConfig(
                mode = SessionNavigationMode.BROWSE_LINEAR,
                swipeAnsweredHistory = false,
            ),
        )

    @Test
    fun practice_resumeAfterHistoryExit_whenNotRandom() {
        assertTrue(
            SessionPracticePostAnswerNavigationPipeline.shouldResumePendingAfterHistoryExit(
                orchestration = practiceOrch,
                randomPracticeEnabled = false,
                exitedAnsweredHistory = true,
            ),
        )
        assertFalse(
            SessionPracticePostAnswerNavigationPipeline.shouldResumePendingAfterHistoryExit(
                orchestration = practiceOrch,
                randomPracticeEnabled = true,
                exitedAnsweredHistory = true,
            ),
        )
    }

    @Test
    fun browse_skipsFullAnswerSourceStay() {
        assertFalse(
            SessionPracticePostAnswerNavigationPipeline.shouldTryFullAnswerSourceStay(
                orchestration = browseOrch,
                fullAnswerModeActive = true,
            ),
        )
        assertTrue(
            SessionPracticePostAnswerNavigationPipeline.shouldTryFullAnswerSourceStay(
                orchestration = practiceOrch,
                fullAnswerModeActive = true,
            ),
        )
    }

    @Test
    fun resolveFinalAdvanceRoute_delegatesToPostAnswerPipeline() {
        assertEquals(
            SessionPostAnswerAdvanceRoute.RANDOM,
            SessionPracticePostAnswerNavigationPipeline.resolveFinalAdvanceRoute(
                orchestration = practiceOrch,
                randomPracticeEnabled = true,
                fullAnswerModeActive = false,
            ),
        )
    }

    @Test
    fun browse_skipsMultiRoundPostAnswerPrev() {
        assertFalse(
            SessionPracticePostAnswerNavigationPipeline.shouldTryMultiRoundPostAnswerPrev(
                orchestration = browseOrch,
                fullAnswerModeActive = true,
                multiRoundSession = true,
            ),
        )
        assertTrue(
            SessionPracticePostAnswerNavigationPipeline.shouldTryMultiRoundPostAnswerPrev(
                orchestration = practiceOrch,
                fullAnswerModeActive = true,
                multiRoundSession = true,
            ),
        )
    }

    @Test
    fun resolveBackwardAdvanceRoute_skipsFullAnswerSequential() {
        assertEquals(
            SessionPostAnswerAdvanceRoute.UNANSWERED_SCAN,
            SessionPracticePostAnswerNavigationPipeline.resolveBackwardAdvanceRoute(
                orchestration = practiceOrch,
                randomPracticeEnabled = false,
                fullAnswerModeActive = true,
            ),
        )
    }
}
