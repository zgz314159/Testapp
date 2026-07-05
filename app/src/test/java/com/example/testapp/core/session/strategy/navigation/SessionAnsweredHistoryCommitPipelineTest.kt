package com.example.testapp.core.session.strategy.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionAnsweredHistoryCommitPipelineTest {
    @Test
    fun navigationUpdate_positionFromOrdered() {
        val update =
            SessionAnsweredHistoryCommitPipeline.navigationUpdate(
                originIndex = 2,
                orderedIndices = listOf(5, 3, 1),
                targetIndex = 3,
                anchorPoolIndices = setOf(3, 5),
            )
        assertEquals(1, update.historyPosition)
        assertEquals(2, update.originIndex)
    }

    @Test
    fun shouldResumeLiveOnForwardMiss_onlyWhenActive() {
        assertTrue(SessionAnsweredHistoryCommitPipeline.shouldResumeLiveOnForwardMiss(true))
        assertFalse(SessionAnsweredHistoryCommitPipeline.shouldResumeLiveOnForwardMiss(false))
    }
}
