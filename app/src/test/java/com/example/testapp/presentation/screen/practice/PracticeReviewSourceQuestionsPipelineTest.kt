package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PracticeReviewSourceQuestionsPipelineTest {
    private val q1 = Question(id = 1, content = "a", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f1")
    private val q2 = Question(id = 2, content = "b", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f2")

    @Test
    fun filterBySource_returnsNull_whenNotReviewMode() {
        assertNull(
            PracticeReviewSourceQuestionsPipeline.filterBySource(
                sourceId = "f1",
                wrongBook = false,
                favorite = false,
                wrongBookQuestions = listOf(q1),
                favoriteQuestions = listOf(q2),
            ),
        )
    }

    @Test
    fun filterBySource_wrongBook_matchesFileName() {
        val result =
            PracticeReviewSourceQuestionsPipeline.filterBySource(
                sourceId = "f1",
                wrongBook = true,
                favorite = false,
                wrongBookQuestions = listOf(q1, q2),
                favoriteQuestions = emptyList(),
            )
        assertEquals(listOf(q1), result)
    }

    @Test
    fun filterBySource_favorite_matchesFileName() {
        val result =
            PracticeReviewSourceQuestionsPipeline.filterBySource(
                sourceId = "f2",
                wrongBook = false,
                favorite = true,
                wrongBookQuestions = emptyList(),
                favoriteQuestions = listOf(q1, q2),
            )
        assertEquals(listOf(q2), result)
    }
}
