package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PracticePinnedQuestionPipelineTest {
    private fun q(id: Int) =
        Question(
            id = id,
            content = "q$id",
            type = "单选题",
            options = emptyList(),
            answer = "a",
            explanation = "",
            fileName = "f",
        )

    @Test
    fun ensurePinned_insertsMissingQuestionAtFront() {
        val ordered = listOf(q(1), q(2), q(3))
        val catalog = ordered + q(99)
        val result = PracticePinnedQuestionPipeline.ensurePinned(ordered, catalog, 99, questionCount = 3)
        assertEquals(listOf(99, 1, 2), result.map { it.id })
    }

    @Test
    fun ensurePinned_keepsExistingOrderWhenAlreadyPresent() {
        val ordered = listOf(q(1), q(2))
        val result = PracticePinnedQuestionPipeline.ensurePinned(ordered, ordered, 2, questionCount = 0)
        assertEquals(listOf(1, 2), result.map { it.id })
    }

    @Test
    fun indexInSession_returnsNullWhenMissing() {
        assertNull(PracticePinnedQuestionPipeline.indexInSession(listOf(q(1)), 2))
    }
}
