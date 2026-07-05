package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeFullAnswerModeActivePipelineTest {
    private fun choiceOnly() =
        listOf(
            Question(
                id = 1,
                content = "q",
                type = QuestionTypes.SINGLE,
                options = listOf("A"),
                answer = "A",
                explanation = "",
            ),
        )

    private fun withInlineBlank() =
        listOf(
            Question(id = 1, content = "___", type = "填空", options = emptyList(), answer = "x", explanation = ""),
        )

    @Test
    fun inactive_whenFullAnswerButNoInlineBlank() {
        val config = PracticeFillConfig.default.copy(generationMode = FillQuestionGenerationMode.FULL_ANSWER)
        assertFalse(PracticeFullAnswerModeActivePipeline.isActive(config, choiceOnly()))
    }

    @Test
    fun active_whenFullAnswerAndInlineBlankPresent() {
        val config = PracticeFillConfig.default.copy(generationMode = FillQuestionGenerationMode.FULL_ANSWER)
        assertTrue(PracticeFullAnswerModeActivePipeline.isActive(config, withInlineBlank()))
    }
}
