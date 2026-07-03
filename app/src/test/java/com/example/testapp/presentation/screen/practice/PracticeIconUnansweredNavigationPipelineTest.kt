package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.FullAnswerIconTapStrategy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeIconUnansweredNavigationPipelineTest {

    @Test
    fun shouldFallbackToUnansweredSource_whenNonAtomicAtBoundary() {
        assertTrue(
            PracticeIconUnansweredNavigationPipeline.shouldFallbackToUnansweredSource(
                navResult = UnansweredNavResult.AtLastUnanswered,
                strategy = FullAnswerIconTapStrategy.GLOBAL_UNANSWERED_FIRST,
                forward = true
            )
        )
    }

    @Test
    fun shouldFallbackToUnansweredSource_notWhenAtomicRoundPool() {
        assertFalse(
            PracticeIconUnansweredNavigationPipeline.shouldFallbackToUnansweredSource(
                navResult = UnansweredNavResult.AtLastUnanswered,
                strategy = FullAnswerIconTapStrategy.MULTI_ROUND_POOL_FIRST,
                forward = true
            )
        )
    }
}
