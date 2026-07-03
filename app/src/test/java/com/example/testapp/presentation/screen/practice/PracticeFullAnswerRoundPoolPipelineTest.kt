package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.buildDerivedFillQuestionId
import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeFullAnswerRoundPoolPipelineTest {

    private fun question(id: Int) = Question(
        id = id,
        content = "c",
        type = "fill",
        options = emptyList(),
        answer = "a",
        explanation = ""
    )

    @Test
    fun indicesInRoundPool_sameRoundAcrossSources() {
        val q1r1 = buildDerivedFillQuestionId(1, 0)
        val q1r2 = buildDerivedFillQuestionId(1, 1)
        val q2r1 = buildDerivedFillQuestionId(2, 0)
        val questions = listOf(
            question(q1r1),
            question(q1r2),
            question(q2r1)
        )
        assertEquals(listOf(0, 2), PracticeFullAnswerRoundPoolPipeline.indicesInRoundPool(questions, 0))
        assertEquals(listOf(1), PracticeFullAnswerRoundPoolPipeline.indicesInRoundPool(questions, 1))
    }
}
