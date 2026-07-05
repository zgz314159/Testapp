package com.example.testapp.core.session.strategy.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionAnsweredHistoryTargetPipelineTest {
    private val ordered = listOf(2, 5, 8)

    @Test
    fun routesStandardOlder() {
        assertEquals(
            8,
            SessionAnsweredHistoryTargetPipeline.resolveOlderTargetIndex(
                fullAnswerModeActive = false,
                orderedIndices = ordered,
                anchorPoolIndices = emptySet(),
                currentIndex = 5,
                activeHistoryPosition = null,
            ),
        )
    }

    @Test
    fun routesFullAnswerOlder() {
        val pool = setOf(2, 5)
        assertEquals(
            8,
            SessionAnsweredHistoryTargetPipeline.resolveOlderTargetIndex(
                fullAnswerModeActive = true,
                orderedIndices = ordered,
                anchorPoolIndices = pool,
                currentIndex = 5,
                activeHistoryPosition = null,
            ),
        )
    }
}
