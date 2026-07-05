package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SessionAnsweredHistoryOverlayPipelineTest {
    @Test
    fun restoreOverlays_emptyOriginals() {
        val qws = listOf(sample(1))
        val result = SessionAnsweredHistoryOverlayPipeline.restoreOverlays(emptyMap(), qws)
        assertFalse(result.changed)
        assertEquals(qws, result.questionsWithState)
    }

    @Test
    fun applySnapshotOverlay_replacesIndex() {
        val live = sample(1, content = "live")
        val snap = sample(1, content = "snap")
        val updated = SessionAnsweredHistoryOverlayPipeline.applySnapshotOverlay(listOf(live), 0, snap)
        assertEquals("snap", updated[0].question.content)
    }

    private fun sample(
        id: Int,
        content: String = "q",
    ): QuestionWithState =
        QuestionWithState(
            question =
                Question(
                    id = id,
                    content = content,
                    type = "单选题",
                    options = emptyList(),
                    answer = "A",
                    explanation = "",
                ),
        )
}
