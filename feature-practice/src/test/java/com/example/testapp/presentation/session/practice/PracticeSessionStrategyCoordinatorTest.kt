package com.example.testapp.presentation.session.practice

import com.example.testapp.core.session.strategy.edit.QuestionEditSessionStrategyBootstrap
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitMode
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSessionStrategyCoordinatorTest {
    @Test
    fun bindStrategy_appliesContextToListener() {
        var applied = 0
        val coordinator =
            PracticeSessionStrategyCoordinator(
                progressId = { "practice_quiz" },
                onStrategyApplied = { applied++ },
            )

        coordinator.bindStrategy(QuestionSessionKind.Practice("quiz"))

        assertEquals(1, applied)
        assertEquals(SessionExitMode.PRACTICE, coordinator.exitConfig().mode)
    }

    @Test
    fun reviewSnapshot_restoreRoundTrip() {
        val coordinator =
            PracticeSessionStrategyCoordinator(
                progressId = { "practice_quiz" },
                onStrategyApplied = {},
            )
        coordinator.bindStrategy(
            QuestionEditSessionStrategyBootstrap.kind("quiz", questionId = 1),
        )
        coordinator.capturePreReviewIfNeeded()
        coordinator.bindReviewStrategy("practice_quiz|10|0|0|0")

        assertEquals(SessionExitMode.REVIEW, coordinator.exitConfig().mode)

        val snapshot = coordinator.restorePreReviewOrNull()
        assertTrue(snapshot != null)
        coordinator.bindStrategy(snapshot!!.kind)

        assertEquals(SessionExitMode.BROWSE, coordinator.exitConfig().mode)
        assertEquals(SessionNavigationMode.BROWSE_LINEAR, coordinator.navigationConfig().mode)
        assertNull(coordinator.restorePreReviewOrNull())
    }
}
