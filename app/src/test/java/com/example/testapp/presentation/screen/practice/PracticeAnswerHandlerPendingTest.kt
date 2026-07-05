package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeAnswerHandlerPendingTest {
    private val handler = PracticeAnswerHandler()

    private fun fillQws(
        textAnswer: String,
        showResult: Boolean,
    ) = QuestionWithState(
        question =
            Question(
                id = 1,
                content = "q",
                type = "填空",
                options = emptyList(),
                answer = "right",
                explanation = "",
            ),
        textAnswer = textAnswer,
        showResult = showResult,
    )

    @Test
    fun requireCorrect_doesNotGatePending_whenFullAnswerModeInactive() {
        val revealedWrong = fillQws(textAnswer = "wrong", showResult = true)
        assertFalse(
            handler.isQuestionPendingForCurrentMode(
                questionWithState = revealedWrong,
                fullAnswerModeActive = false,
                fullAnswerRequireCorrect = true,
            ),
        )
    }

    @Test
    fun requireCorrect_gatesPending_whenFullAnswerModeActive() {
        val revealedWrong = fillQws(textAnswer = "wrong", showResult = true)
        assertTrue(
            handler.isQuestionPendingForCurrentMode(
                questionWithState = revealedWrong,
                fullAnswerModeActive = true,
                fullAnswerRequireCorrect = true,
            ),
        )
    }
}
