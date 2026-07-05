package com.example.testapp.presentation.session.browse

import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowseSessionLoadPipelineTest {
    private fun question(id: Int) =
        Question(
            id = id,
            content = "Q$id",
            type = "单选",
            options = listOf("A", "B"),
            answer = "A",
            explanation = "",
        )

    @Test
    fun load_pinsTargetAndSetsStartIndex() {
        val catalog = (1..10).map { question(it) }
        val result =
            BrowseSessionLoadPipeline.load(
                catalog = catalog,
                targetQuestionId = 7,
                questionCount = 5,
                random = false,
                sessionStartTime = 1L,
            )
        assertTrue(result.questionsWithState.any { it.question.id == 7 })
        assertEquals(7, result.questionsWithState[result.startIndex].question.id)
    }

    @Test
    fun load_emptyCatalog_returnsEmpty() {
        val result =
            BrowseSessionLoadPipeline.load(
                catalog = emptyList(),
                targetQuestionId = 1,
                questionCount = 5,
                random = false,
                sessionStartTime = 1L,
            )
        assertTrue(result.questionsWithState.isEmpty())
        assertEquals(0, result.startIndex)
    }
}
