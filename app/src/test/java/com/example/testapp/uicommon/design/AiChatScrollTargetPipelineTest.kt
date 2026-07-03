package com.example.testapp.uicommon.design

import org.junit.Assert.assertEquals
import org.junit.Test

class AiChatScrollTargetPipelineTest {

    @Test
    fun lastIndex_countsTypingAndError() {
        assertEquals(0, AiChatScrollTargetPipeline.lastIndex(1, false, false))
        assertEquals(1, AiChatScrollTargetPipeline.lastIndex(1, true, false))
        assertEquals(2, AiChatScrollTargetPipeline.lastIndex(1, true, true))
    }

    @Test
    fun lastIndex_emptyMessages() {
        assertEquals(0, AiChatScrollTargetPipeline.lastIndex(0, true, false))
    }
}
