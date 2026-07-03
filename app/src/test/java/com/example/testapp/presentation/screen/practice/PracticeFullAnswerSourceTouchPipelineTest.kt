package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.buildDerivedFillQuestionId
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeFullAnswerSourceTouchPipelineTest {

    private fun question(id: Int) = Question(id, "c", "fill", emptyList(), "a", "")

    private fun qws(id: Int, text: String) = QuestionWithState(
        question = question(id),
        textAnswer = text,
        selectedOptions = if (text.isNotBlank()) listOf(-1) else emptyList()
    )

    @Test
    fun untouched_whenNoInputOnAnyRound() {
        val q1r1 = buildDerivedFillQuestionId(133, 0)
        val q1r2 = buildDerivedFillQuestionId(133, 1)
        val questions = listOf(question(q1r1), question(q1r2))
        val states = listOf(qws(q1r1, ""), qws(q1r2, ""))
        assertTrue(
            PracticeFullAnswerSourceTouchPipeline.isSourceCompletelyUntouched(
                questions, states, currentIndex = 0
            )
        )
    }

    @Test
    fun touched_whenAnyRoundHasInput() {
        val q1r1 = buildDerivedFillQuestionId(133, 0)
        val q1r2 = buildDerivedFillQuestionId(133, 1)
        val questions = listOf(question(q1r1), question(q1r2))
        val states = listOf(qws(q1r1, ""), qws(q1r2, "x"))
        assertFalse(
            PracticeFullAnswerSourceTouchPipeline.isSourceCompletelyUntouched(
                questions, states, currentIndex = 0
            )
        )
    }
}
