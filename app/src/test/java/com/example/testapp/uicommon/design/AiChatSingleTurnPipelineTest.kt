package com.example.testapp.uicommon.design

import com.example.testapp.uicommon.model.AiChatMessageRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatSingleTurnPipelineTest {

    @Test
    fun build_returnsUserThenAssistant() {
        val messages = AiChatSingleTurnPipeline.build("问题", "回答")
        assertEquals(2, messages.size)
        assertEquals(AiChatMessageRole.User, messages[0].role)
        assertEquals(AiChatMessageRole.Assistant, messages[1].role)
    }

    @Test
    fun build_emptyReturnsEmptyList() {
        assertTrue(AiChatSingleTurnPipeline.build("", "").isEmpty())
    }
}
