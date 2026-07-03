package com.example.testapp.uicommon.design

import com.example.testapp.uicommon.model.AiChatMessageRole
import com.example.testapp.uicommon.model.AiChatTurn
import org.junit.Assert.assertEquals
import org.junit.Test

class AiChatTurnFlattenPipelineTest {

    @Test
    fun flatten_interleavesUserAndAssistant() {
        val turns = listOf(
            AiChatTurn(user = "Q1", assistant = "A1"),
            AiChatTurn(user = "Q2", assistant = "A2")
        )
        val messages = AiChatTurnFlattenPipeline.flatten(turns)
        assertEquals(4, messages.size)
        assertEquals(AiChatMessageRole.User, messages[0].role)
        assertEquals("Q1", messages[0].content)
        assertEquals(AiChatMessageRole.Assistant, messages[1].role)
        assertEquals("A2", messages[3].content)
    }

    @Test
    fun flatten_skipsBlankParts() {
        val turns = listOf(AiChatTurn(user = "  ", assistant = "A1"))
        assertEquals(1, AiChatTurnFlattenPipeline.flatten(turns).size)
    }
}
