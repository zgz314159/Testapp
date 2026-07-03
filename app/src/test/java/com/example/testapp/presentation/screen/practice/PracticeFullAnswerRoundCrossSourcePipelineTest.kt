package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.buildDerivedFillQuestionId
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeFullAnswerRoundCrossSourcePipelineTest {

    private fun question(id: Int) = Question(
        id = id,
        content = "c",
        type = "fill",
        options = emptyList(),
        answer = "a",
        explanation = ""
    )

    private fun qws(id: Int, showResult: Boolean, textAnswer: String = "") = QuestionWithState(
        question = question(id),
        showResult = showResult,
        textAnswer = textAnswer,
        selectedOptions = if (textAnswer.isNotBlank()) listOf(-1) else emptyList()
    )

    @Test
    fun maySingleTapExit_falseWhenRoundHasUnanswered() {
        val id = buildDerivedFillQuestionId(sourceQuestionId = 1, variantIndex = 0)
        val questions = listOf(question(id))
        val states = listOf(qws(id, showResult = false))
        assertFalse(
            PracticeFullAnswerRoundCrossSourcePipeline.maySingleTapExitRoundPool(
                questions, states, 0, fullAnswerRequireCorrect = false
            )
        )
    }

    @Test
    fun maySingleTapExit_trueWhenAllHaveInputWithoutShowResult() {
        val id = buildDerivedFillQuestionId(sourceQuestionId = 1, variantIndex = 0)
        val questions = listOf(question(id))
        val states = listOf(qws(id, showResult = false, textAnswer = "a"))
        assertTrue(
            PracticeFullAnswerRoundCrossSourcePipeline.maySingleTapExitRoundPool(
                questions, states, 0, fullAnswerRequireCorrect = false
            )
        )
    }
}
