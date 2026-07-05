package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.core.session.SessionMemoryMode
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionAnsweredHistoryIndexPipelineTest {
    @Test
    fun applyMemoryRoundPriority_promotesCurrentRound() {
        val qws = listOf(sample(1, 100L), sample(2, 200L), sample(3, 300L))
        val ordered = listOf(2, 0, 1)
        assertEquals(
            listOf(2, 0, 1),
            SessionAnsweredHistoryIndexPipeline.applyMemoryRoundPriority(
                answeredByTimeDesc = ordered,
                questionsWithState = qws,
                roundIds = setOf(3),
                memoryModeActive = true,
                memoryPoolMode = SessionMemoryMode.MEMORY_POOL_MODE_ROUND,
            ),
        )
    }

    @Test
    fun buildSwipeHistoryIndices_sortsByAnswerTimeDesc() {
        val qws = listOf(sample(1, 100L), sample(2, 300L))
        assertEquals(
            listOf(1, 0),
            SessionAnsweredHistoryIndexPipeline.buildSwipeHistoryIndices(qws) { it },
        )
    }

    private fun sample(
        id: Int,
        time: Long,
    ): QuestionWithState =
        QuestionWithState(
            question =
                Question(
                    id = id,
                    content = "q",
                    type = "单选题",
                    options = emptyList(),
                    answer = "A",
                    explanation = "",
                ),
            showResult = true,
            sessionAnswerTime = time,
        )
}
