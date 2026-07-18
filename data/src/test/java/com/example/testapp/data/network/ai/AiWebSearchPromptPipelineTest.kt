package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.domain.model.QuestionCorrectionSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiWebSearchPromptPipelineTest {

    @Test
    fun latestUserQuery_usesLastUserMessage() {
        val query = AiWebSearchPromptPipeline.latestUserQuery(
            listOf(
                DeepSeekChatMessage("user", "first"),
                DeepSeekChatMessage("assistant", "answer"),
                DeepSeekChatMessage("user", " latest question "),
            ),
        )

        assertEquals("latest question", query)
    }

    @Test
    fun attachSources_enrichesOnlyLastUserMessage() {
        val messages = listOf(
            DeepSeekChatMessage("system", "system"),
            DeepSeekChatMessage("user", "question"),
        )
        val result = AiWebSearchPromptPipeline.attachSources(
            messages = messages,
            sources = listOf(
                QuestionCorrectionSource(
                    title = "source",
                    url = "https://example.com",
                    snippet = "evidence",
                ),
            ),
        )

        assertEquals("system", result.first().content)
        assertTrue(result.last().content.startsWith("question"))
        assertTrue(result.last().content.contains("https://example.com"))
        assertTrue(result.last().content.contains("evidence"))
        assertTrue(result.last().content.contains("[1]"))
        assertEquals("question", messages.last().content)
    }

    @Test
    fun appendCitations_addsDeterministicSourceListAfterResponse() {
        val result = AiWebSearchPromptPipeline.appendCitations(
            response = "Answer with citation [1].",
            sources = listOf(
                QuestionCorrectionSource(
                    title = "Example source",
                    url = "https://example.com/article",
                    snippet = "line one\nline two",
                    publishedDate = "2026-01-29",
                ),
            ),
        )

        assertTrue(result.startsWith("Answer with citation [1]."))
        assertTrue(result.contains("---\n参考来源"))
        assertTrue(result.contains("[1] Example source\nhttps://example.com/article"))
        assertTrue(result.contains("时间: 2026-01-29"))
        assertTrue(result.contains("摘要: line one line two"))
    }

    @Test
    fun appendCitations_omitsDateAndSnippetLinesWhenBlank() {
        val result = AiWebSearchPromptPipeline.appendCitations(
            response = "Answer.",
            sources = listOf(
                QuestionCorrectionSource(title = "T", url = "https://example.com"),
            ),
        )

        assertTrue(result.endsWith("[1] T\nhttps://example.com"))
    }

    @Test
    fun appendCitations_keepsResponseUnchangedWhenNoSources() {
        assertEquals(
            "plain answer",
            AiWebSearchPromptPipeline.appendCitations("plain answer", emptyList()),
        )
    }
}
