package com.example.testapp.core.util

import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullAnswerIconNavigationStrategyPipelineTest {

    @Test
    fun resolve_multiRoundFullAnswer_usesRoundPoolFirst() {
        val questions = listOf(
            Question(
                id = buildDerivedFillQuestionId(1, 1),
                content = "a",
                type = "fill",
                options = emptyList(),
                answer = "b",
                explanation = ""
            )
        )
        assertTrue(FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(questions))
        assertEquals(
            FullAnswerIconTapStrategy.MULTI_ROUND_POOL_FIRST,
            FullAnswerIconNavigationStrategyPipeline.resolve(
                fullAnswerModeActive = true,
                multiRoundSession = true
            )
        )
        assertTrue(
            FullAnswerIconNavigationStrategyPipeline.singleTapUsesRoundPool(
                FullAnswerIconTapStrategy.MULTI_ROUND_POOL_FIRST
            )
        )
    }

    @Test
    fun resolve_singleRoundFullAnswer_usesGlobalUnanswered() {
        assertEquals(
            FullAnswerIconTapStrategy.GLOBAL_UNANSWERED_FIRST,
            FullAnswerIconNavigationStrategyPipeline.resolve(
                fullAnswerModeActive = true,
                multiRoundSession = false
            )
        )
        assertTrue(
            FullAnswerIconNavigationStrategyPipeline.shouldFallbackToCrossSourceOnSingleTap(
                FullAnswerIconTapStrategy.GLOBAL_UNANSWERED_FIRST,
                atBoundary = true
            )
        )
        assertFalse(
            FullAnswerIconNavigationStrategyPipeline.shouldFallbackToCrossSourceOnSingleTap(
                FullAnswerIconTapStrategy.MULTI_ROUND_POOL_FIRST,
                atBoundary = true
            )
        )
    }
}
