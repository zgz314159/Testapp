package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeFullAnswerRoundSlotPendingPipelineTest {

    private fun qws(text: String, showResult: Boolean = false) =
        QuestionWithState(
            question = Question(1, "c", "fill", emptyList(), "correct", ""),
            textAnswer = text,
            selectedOptions = if (text.isNotBlank()) listOf(-1) else emptyList(),
            showResult = showResult
        )

    @Test
    fun fullAnswerMode_inputCountsAsComplete() {
        assertFalse(
            PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                qws("x"), fullAnswerRequireCorrect = false
            )
        )
    }

    @Test
    fun requireCorrect_inputWithoutGrade_notPending() {
        assertFalse(
            PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                qws("x", showResult = false), fullAnswerRequireCorrect = true
            )
        )
    }

    @Test
    fun requireCorrect_gradedWrong_pending() {
        assertTrue(
            PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                qws("wrong", showResult = true), fullAnswerRequireCorrect = true
            )
        )
    }

    @Test
    fun noInput_alwaysPending() {
        assertTrue(
            PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                qws(""), fullAnswerRequireCorrect = false
            )
        )
    }
}
