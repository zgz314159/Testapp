package com.example.testapp.core.session.strategy.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionAnsweredHistoryBrowsePipelineTest {
    private val ordered = listOf(2, 5, 8) // newest -> oldest

    @Test
    fun resolveOlder_fromIdle_currentInList() {
        assertEquals(8, SessionAnsweredHistoryBrowsePipeline.resolveOlderTargetIndex(ordered, 5, null))
    }

    @Test
    fun resolveOlder_fromActivePosition() {
        assertEquals(
            8,
            SessionAnsweredHistoryBrowsePipeline.resolveOlderTargetIndex(ordered, 5, activeHistoryPosition = 1),
        )
    }

    @Test
    fun resolveNewer_fromActivePosition() {
        assertEquals(
            2,
            SessionAnsweredHistoryBrowsePipeline.resolveNewerTargetIndex(ordered, 5, activeHistoryPosition = 1),
        )
        assertNull(SessionAnsweredHistoryBrowsePipeline.resolveNewerTargetIndex(ordered, 2, activeHistoryPosition = 0))
    }

    @Test
    fun backwardStop_atGlobalOldest() {
        assertEquals(
            SessionAnsweredHistoryBrowsePipeline.BackwardStop.AtOldestAnswered,
            SessionAnsweredHistoryBrowsePipeline.resolveBackwardStopWhenNoTarget(
                orderedIndices = ordered,
                currentIndex = 8,
                inActiveHistoryMode = false,
            ),
        )
    }

    @Test
    fun historyPositionForTarget() {
        assertEquals(1, SessionAnsweredHistoryBrowsePipeline.historyPositionForTarget(ordered, 5))
        assertEquals(0, SessionAnsweredHistoryBrowsePipeline.historyPositionForTarget(ordered, 99))
    }
}
