package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSubmitFlowTest {

    @Test
    fun resolve_showDialogWhenSessionHasInputOnly() {
        assertEquals(
            PracticeSubmitFlow.Action.ShowSubmitDialog,
            PracticeSubmitFlow.resolve(answeredThisSession = false, hasAnyInputInSession = true)
        )
    }

    @Test
    fun resolve_exitWhenNoInput() {
        assertEquals(
            PracticeSubmitFlow.Action.ExitWithoutAnswer,
            PracticeSubmitFlow.resolve(answeredThisSession = false, hasAnyInputInSession = false)
        )
    }
}

class PracticeSessionGradePipelineTest {

    private fun qws(text: String, showResult: Boolean = false) = QuestionWithState(
        question = Question(1, "c", "fill", emptyList(), "a", ""),
        textAnswer = text,
        selectedOptions = if (text.isNotBlank()) listOf(-1) else emptyList(),
        showResult = showResult
    )

    @Test
    fun indicesPendingReveal_onlyInputWithoutShowResult() {
        val states = listOf(qws("x"), qws("", showResult = false), qws("y", showResult = true))
        assertEquals(listOf(0), PracticeSessionGradePipeline.indicesPendingReveal(states))
    }
}
