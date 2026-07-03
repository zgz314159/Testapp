package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.buildDerivedFillQuestionId
import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeFullAnswerSourceRoundPoolPipelineTest {

    private fun question(id: Int) = Question(id, "c", "fill", emptyList(), "a", "")

    @Test
    fun indicesInPool_sameSourceSameRoundOnly() {
        val q1r1 = buildDerivedFillQuestionId(1, 0)
        val q1r2 = buildDerivedFillQuestionId(1, 1)
        val q2r1 = buildDerivedFillQuestionId(2, 0)
        val questions = listOf(question(q1r1), question(q1r2), question(q2r1))
        assertEquals(listOf(0), PracticeFullAnswerSourceRoundPoolPipeline.indicesInPool(questions, 0))
        assertEquals(listOf(1), PracticeFullAnswerSourceRoundPoolPipeline.indicesInPool(questions, 1))
        assertEquals(listOf(2), PracticeFullAnswerSourceRoundPoolPipeline.indicesInPool(questions, 2))
    }
}
