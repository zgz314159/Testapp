package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExamReviewSourceQuestionsPipelineTest {
    private val q1 =
        Question(id = 1, content = "a", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f1")
    private val q2 =
        Question(id = 2, content = "b", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f2")

    @Test
    fun filterBySource_null_whenNotWrongOrFavorite() {
        assertNull(
            ExamReviewSourceQuestionsPipeline.filterBySource(
                sourceId = "f1",
                wrongBook = false,
                favorite = false,
                wrongBookQuestions = listOf(q1),
                favoriteQuestions = listOf(q2),
            ),
        )
    }

    @Test
    fun filterBySource_wrongBook() {
        assertEquals(
            listOf(q1),
            ExamReviewSourceQuestionsPipeline.filterBySource(
                sourceId = "f1",
                wrongBook = true,
                favorite = false,
                wrongBookQuestions = listOf(q1, q2),
                favoriteQuestions = emptyList(),
            ),
        )
    }
}
