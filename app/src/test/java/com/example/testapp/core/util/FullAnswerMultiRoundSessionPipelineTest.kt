package com.example.testapp.core.util

import com.example.testapp.domain.model.Question
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullAnswerMultiRoundSessionPipelineTest {

    private fun question(id: Int) = Question(id, "c", "fill", emptyList(), "a", "")

    @Test
    fun isMultiRound_trueWhenRound2Present() {
        val questions = listOf(
            question(buildDerivedFillQuestionId(1, 0)),
            question(buildDerivedFillQuestionId(1, 1))
        )
        assertTrue(FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(questions))
    }

    @Test
    fun isMultiRound_falseWhenOnlyRound1() {
        val questions = listOf(
            question(1),
            question(buildDerivedFillQuestionId(2, 0))
        )
        assertFalse(FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(questions))
    }
}
