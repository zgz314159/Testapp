package com.example.testapp.uicommon.design

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatSendEnabledPipelineTest {

    @Test
    fun isEnabled_requiresNonBlankInput() {
        assertFalse(AiChatSendEnabledPipeline.isEnabled(sendEnabled = true, input = "  "))
        assertTrue(AiChatSendEnabledPipeline.isEnabled(sendEnabled = true, input = "hi"))
    }

    @Test
    fun isEnabled_respectsSendEnabledFlag() {
        assertFalse(AiChatSendEnabledPipeline.isEnabled(sendEnabled = false, input = "hi"))
    }
}
