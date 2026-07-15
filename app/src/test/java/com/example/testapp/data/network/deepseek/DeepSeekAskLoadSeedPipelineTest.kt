package com.example.testapp.data.network.deepseek

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepSeekAskLoadSeedPipelineTest {
    @Test
    fun resolveRaw_prefersStructuredOverPlain() {
        val structured = DeepSeekAskPersistFormatPipeline.encode(
            listOf(
                DeepSeekChatTurn(user = "题干", assistant = "首答"),
                DeepSeekChatTurn(user = "【追问】扩展", assistant = "追问答"),
            )
        )
        val plain = "首答"
        assertEquals(structured, DeepSeekAskLoadSeedPipeline.resolveRaw(structured, plain))
        assertEquals(structured, DeepSeekAskLoadSeedPipeline.resolveRaw(plain, structured))
    }

    @Test
    fun resolveRaw_prefersLongerWhenSameRichness() {
        val db = "answer one\n\n---\n\nanswer two"
        assertEquals(db, DeepSeekAskLoadSeedPipeline.resolveRaw(db, "answer one"))
    }

    @Test
    fun resolveRaw_usesSeedWhenDbBlank() {
        assertEquals("in-memory only", DeepSeekAskLoadSeedPipeline.resolveRaw(null, "in-memory only"))
    }

    @Test
    fun resolveRaw_returnsNullWhenBothBlank() {
        assertNull(DeepSeekAskLoadSeedPipeline.resolveRaw("", "  "))
    }

    @Test
    fun resolvePreferStructured_keepsEncodeWhenIncomingIsFlat() {
        val structured = DeepSeekAskPersistFormatPipeline.encode(
            listOf(DeepSeekChatTurn(user = "q", assistant = "a1"))
        )
        val flat = "a1"
        assertEquals(structured, DeepSeekAskLoadSeedPipeline.resolvePreferStructured(structured, flat))
        assertTrue(DeepSeekAskLoadSeedPipeline.richness(structured) > DeepSeekAskLoadSeedPipeline.richness(flat))
    }
}
