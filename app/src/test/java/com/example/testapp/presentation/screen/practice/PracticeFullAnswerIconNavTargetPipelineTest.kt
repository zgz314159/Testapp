package com.example.testapp.presentation.screen.practice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeFullAnswerIconNavTargetPipelineTest {

    @Test
    fun resolveNext_sequential_wrapsToFirst() {
        assertEquals(0, PracticeFullAnswerIconNavTargetPipeline.resolveNext(2, listOf(0, 1, 2), randomOrder = false))
    }

    @Test
    fun resolvePrev_sequential_wrapsToLast() {
        assertEquals(2, PracticeFullAnswerIconNavTargetPipeline.resolvePrev(0, listOf(0, 1, 2), randomOrder = false))
    }

    @Test
    fun hasNext_falseWhenOnlyCurrentInPool() {
        assertFalse(PracticeFullAnswerIconNavTargetPipeline.hasNext(1, listOf(1), randomOrder = false))
    }

    @Test
    fun hasNext_trueWhenOtherNavigableExists() {
        assertTrue(PracticeFullAnswerIconNavTargetPipeline.hasNext(0, listOf(0, 3), randomOrder = false))
    }
}
