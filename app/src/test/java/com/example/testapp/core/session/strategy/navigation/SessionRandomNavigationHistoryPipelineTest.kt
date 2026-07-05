package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRandomNavigationHistoryPipelineTest {
    @Test
    fun appendOrigin_skipsDuplicateTail() {
        assertFalse(SessionRandomNavigationHistoryPipeline.shouldAppendOrigin(listOf(1, 2), currentIndex = 2))
        assertTrue(SessionRandomNavigationHistoryPipeline.shouldAppendOrigin(listOf(1, 2), currentIndex = 3))
        assertEquals(listOf(1, 2, 3), SessionRandomNavigationHistoryPipeline.appendedHistory(listOf(1, 2), 3))
    }

    @Test
    fun seedHistoryIndices_excludesCurrentAndUnanswered() {
        val qws =
            listOf(
                questionAt(0, answered = true, time = 300L),
                questionAt(1, answered = false, time = 0L),
                questionAt(2, answered = true, time = 100L),
            )
        assertEquals(
            listOf(0, 2),
            SessionRandomNavigationHistoryPipeline.seedHistoryIndices(
                questionsWithState = qws,
                currentIndex = 1,
                isQuestionAnswered = { it.showResult },
            ),
        )
    }

    private fun questionAt(
        index: Int,
        answered: Boolean,
        time: Long,
    ): QuestionWithState =
        QuestionWithState(
            question =
                Question(
                    id = index,
                    content = "q$index",
                    type = "单选题",
                    options = emptyList(),
                    answer = "A",
                    explanation = "",
                ),
            showResult = answered,
            sessionAnswerTime = time,
        )
}
