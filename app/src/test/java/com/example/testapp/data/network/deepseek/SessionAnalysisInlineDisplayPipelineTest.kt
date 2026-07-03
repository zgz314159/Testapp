package com.example.testapp.data.network.deepseek

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionAnalysisInlineDisplayPipelineTest {

    @Test
    fun toDisplayText_returnsPlainTextAsIs() {
        assertEquals("plain \$x^2\$", SessionAnalysisInlineDisplayPipeline.toDisplayText("plain \$x^2\$"))
    }

    @Test
    fun toDisplayText_decodesStructuredPersist() {
        val persisted = DeepSeekAskPersistFormatPipeline.encode(
            listOf(DeepSeekChatTurn(user = "题干", assistant = "答案 \$F=ma\$"))
        )
        assertEquals("答案 \$F=ma\$", SessionAnalysisInlineDisplayPipeline.toDisplayText(persisted, "题干"))
    }
}
