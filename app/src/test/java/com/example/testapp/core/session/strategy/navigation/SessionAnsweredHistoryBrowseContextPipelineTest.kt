package com.example.testapp.core.session.strategy.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionAnsweredHistoryBrowseContextPipelineTest {
    @Test
    fun resolve_idleContext() {
        val ctx =
            SessionAnsweredHistoryBrowseContextPipeline.resolve(
                liveCurrentIndex = 4,
                activeOriginIndex = null,
                activeHistoryPosition = null,
                activeOrderedIndices = null,
                activeAnchorPoolIndices = null,
                fullAnswerModeActive = true,
                idleSourcePoolIndices = setOf(2, 4),
                idleOrderedIndices = listOf(1, 3, 5),
            )
        assertEquals(4, ctx.originIndex)
        assertEquals(setOf(2, 4), ctx.anchorPoolIndices)
        assertEquals(listOf(1, 3, 5), ctx.orderedIndices)
        assertFalse(ctx.inActiveHistoryMode)
    }

    @Test
    fun resolve_activeContext() {
        val ctx =
            SessionAnsweredHistoryBrowseContextPipeline.resolve(
                liveCurrentIndex = 9,
                activeOriginIndex = 2,
                activeHistoryPosition = 1,
                activeOrderedIndices = listOf(5, 3, 1),
                activeAnchorPoolIndices = setOf(3, 5),
                fullAnswerModeActive = false,
                idleSourcePoolIndices = emptySet(),
                idleOrderedIndices = emptyList(),
            )
        assertTrue(ctx.inActiveHistoryMode)
        assertEquals(1, ctx.activeHistoryPosition)
        assertEquals(listOf(5, 3, 1), ctx.orderedIndices)
    }
}
