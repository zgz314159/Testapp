package com.example.testapp.data.repository

import com.example.testapp.domain.model.QuestionCorrectionApplySelection
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class QuestionCorrectionParsePipelineTest {

    @Test
    fun validate_choiceRequiresOptions() {
        val request = QuestionCorrectionRequest(
            questionType = "单选题",
            content = "题干",
            options = listOf("a", "b"),
            answer = "A",
        )
        assertThrows(IllegalArgumentException::class.java) {
            QuestionCorrectionParsePipeline.validate(
                QuestionCorrectionSuggestion(
                    content = "新题干",
                    options = emptyList(),
                    answer = "A",
                    confidence = 0.8,
                ),
                request,
            )
        }
    }

    @Test
    fun applyToDraft_respectsSelection() {
        val applied = QuestionCorrectionParsePipeline.applyToDraft(
            currentContent = "旧题干",
            currentOptions = listOf("1", "2"),
            currentAnswer = "A",
            currentExplanation = "旧解析",
            suggestion = QuestionCorrectionSuggestion(
                content = "新题干",
                options = listOf("x", "y"),
                answer = "B",
                explanation = "新解析",
                confidence = 0.9,
            ),
            selection = QuestionCorrectionApplySelection(
                applyContent = true,
                applyOptions = false,
                applyAnswer = true,
                applyExplanation = false,
            ),
        )
        assertEquals("新题干", applied.content)
        assertEquals(listOf("1", "2"), applied.options)
        assertEquals("B", applied.answer)
        assertEquals("旧解析", applied.explanation)
    }
}
