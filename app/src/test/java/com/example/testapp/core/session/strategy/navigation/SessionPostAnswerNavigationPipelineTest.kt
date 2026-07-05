package com.example.testapp.core.session.strategy.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPostAnswerNavigationPipelineTest {
    @Test
    fun routeAfterRoundPool_fullAnswerSequential() {
        assertEquals(
            SessionPostAnswerAdvanceRoute.FULL_ANSWER_SEQUENTIAL,
            SessionPostAnswerNavigationPipeline.routeAfterRoundPoolChecks(
                fullAnswerModeActive = true,
                randomEnabled = false,
            ),
        )
    }

    @Test
    fun routeAfterRoundPool_randomExam() {
        assertEquals(
            SessionPostAnswerAdvanceRoute.RANDOM,
            SessionPostAnswerNavigationPipeline.routeAfterRoundPoolChecks(
                fullAnswerModeActive = true,
                randomEnabled = true,
            ),
        )
    }

    @Test
    fun routeAfterRoundPool_backward_skipsFullAnswerSequential() {
        assertEquals(
            SessionPostAnswerAdvanceRoute.UNANSWERED_SCAN,
            SessionPostAnswerNavigationPipeline.routeAfterRoundPoolChecks(
                fullAnswerModeActive = true,
                randomEnabled = false,
                direction = SessionPostAnswerAdvanceDirection.BACKWARD,
            ),
        )
    }

    @Test
    fun roundPoolGuards() {
        assertTrue(SessionPostAnswerNavigationPipeline.stopsAfterRoundPoolNavigation(navigatedInRoundPool = true))
        assertFalse(SessionPostAnswerNavigationPipeline.stopsAfterRoundPoolNavigation(navigatedInRoundPool = false))
        assertTrue(SessionPostAnswerNavigationPipeline.blocksWhenMustStayInRoundPool(mustStayInRoundPool = true))
    }
}
