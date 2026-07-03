package com.example.testapp.uicommon.design

import com.example.testapp.uicommon.model.AiChatMessageRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiChatBubbleLayoutPipelineTest {

    @Test
    fun assistantUsesBubble_geminiStyleIsFlat() {
        assertFalse(
            AiChatBubbleLayoutPipeline.assistantUsesBubble(
                AiChatMessageRole.Assistant,
                geminiStyle = true
            )
        )
    }

    @Test
    fun assistantUsesBubble_classicStyleKeepsBubble() {
        assertTrue(
            AiChatBubbleLayoutPipeline.assistantUsesBubble(
                AiChatMessageRole.Assistant,
                geminiStyle = false
            )
        )
    }

    @Test
    fun userMaxWidthFraction_geminiIsNarrower() {
        assertEquals(0.82f, AiChatBubbleLayoutPipeline.userMaxWidthFraction(true))
        assertEquals(0.88f, AiChatBubbleLayoutPipeline.userMaxWidthFraction(false))
    }
}
