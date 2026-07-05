package com.example.testapp.core.session.strategy.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionFullAnswerHistoryBrowsePipelineTest {
    @Test
    fun resolveOlder_withinPool() {
        val ordered = listOf(1, 3, 5, 7)
        val pool = setOf(1, 3, 5)
        assertEquals(5, SessionFullAnswerHistoryBrowsePipeline.resolveOlderTargetIndex(ordered, pool, currentIndex = 3))
    }

    @Test
    fun resolveOlder_crossPoolAtPoolEnd() {
        val ordered = listOf(1, 3, 7)
        val pool = setOf(1, 3)
        assertEquals(7, SessionFullAnswerHistoryBrowsePipeline.resolveOlderTargetIndex(ordered, pool, currentIndex = 3))
    }

    @Test
    fun resolveOlder_nextGlobalWhenPoolHasMoreEntries() {
        val ordered = listOf(1, 3, 5, 7)
        val pool = setOf(1, 3)
        assertEquals(5, SessionFullAnswerHistoryBrowsePipeline.resolveOlderTargetIndex(ordered, pool, currentIndex = 3))
    }

    @Test
    fun resolveOlder_liveQuestionNotInHistory() {
        val ordered = listOf(1, 3, 5)
        val pool = setOf(1, 3, 5)
        assertEquals(1, SessionFullAnswerHistoryBrowsePipeline.resolveOlderTargetIndex(ordered, pool, currentIndex = 9))
    }

    @Test
    fun resolveNewer_withinPool() {
        val ordered = listOf(1, 3, 5, 7)
        val pool = setOf(1, 3, 5)
        assertEquals(3, SessionFullAnswerHistoryBrowsePipeline.resolveNewerTargetIndex(ordered, pool, currentIndex = 5))
    }

    @Test
    fun resolveNewer_crossPoolAtPoolHead() {
        val ordered = listOf(1, 3, 5, 7)
        val pool = setOf(3, 5)
        assertEquals(1, SessionFullAnswerHistoryBrowsePipeline.resolveNewerTargetIndex(ordered, pool, currentIndex = 3))
    }

    @Test
    fun resolveNewer_atNewest_returnsNull() {
        val ordered = listOf(1, 3, 5)
        val pool = setOf(1, 3, 5)
        assertNull(SessionFullAnswerHistoryBrowsePipeline.resolveNewerTargetIndex(ordered, pool, currentIndex = 1))
    }
}
