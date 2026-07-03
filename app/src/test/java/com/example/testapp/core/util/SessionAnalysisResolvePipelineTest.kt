package com.example.testapp.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionAnalysisResolvePipelineTest {

    @Test
    fun resolve_prefersSessionWhenStreamingIsParsing() {
        assertEquals(
            "saved",
            SessionAnalysisResolvePipeline.resolve(
                currentIndex = 0,
                streamingPair = 0 to "解析中...",
                sessionValue = "saved",
                listValue = "list",
                parsingKeyword = "解析中"
            )
        )
    }

    @Test
    fun resolve_prefersSessionOverStreamingWhenBothReady() {
        assertEquals(
            "saved",
            SessionAnalysisResolvePipeline.resolve(
                currentIndex = 1,
                streamingPair = 1 to "stream",
                sessionValue = "saved",
                listValue = null,
                parsingKeyword = "解析中"
            )
        )
    }

    @Test
    fun resolve_usesStreamingWhenSessionBlank() {
        assertEquals(
            "stream",
            SessionAnalysisResolvePipeline.resolve(
                currentIndex = 1,
                streamingPair = 1 to "stream",
                sessionValue = "",
                listValue = null,
                parsingKeyword = "解析中"
            )
        )
    }

    @Test
    fun resolve_returnsNullWhenAllBlank() {
        assertNull(
            SessionAnalysisResolvePipeline.resolve(
                currentIndex = 0,
                streamingPair = null,
                sessionValue = "",
                listValue = "",
                parsingKeyword = "解析中"
            )
        )
    }
}
