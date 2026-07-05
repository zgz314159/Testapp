package com.example.testapp.data.network.deepseek

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeepSeekAskLoadSeedPipelineTest {
    @Test
    fun resolveRaw_prefersDbWhenBothPresent() {
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
}
