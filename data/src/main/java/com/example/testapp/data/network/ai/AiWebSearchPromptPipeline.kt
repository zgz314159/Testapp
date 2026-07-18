package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.domain.model.QuestionCorrectionSource

object AiWebSearchPromptPipeline {

    fun latestUserQuery(messages: List<DeepSeekChatMessage>): String =
        messages.lastOrNull { it.role == "user" }
            ?.content
            ?.trim()
            .orEmpty()
            .take(MAX_QUERY_LENGTH)

    fun attachSources(
        messages: List<DeepSeekChatMessage>,
        sources: List<QuestionCorrectionSource>,
    ): List<DeepSeekChatMessage> {
        val userIndex = messages.indexOfLast { it.role == "user" }
        if (userIndex < 0) return messages
        val original = messages[userIndex]
        val enriched = original.copy(
            content = buildString {
                append(original.content)
                append("\n\n")
                append(formatSources(sources))
                append(
                    "\n\n请结合检索结果回答，引用事实时在正文相应位置标注 [1]、[2] 等对应编号；" +
                        "上面每条与回答相关的来源都应至少被标注一次，仅与回答无关的来源可不标注；" +
                        "不要自行编造或另列来源，证据不足时请明确说明。",
                )
            },
        )
        return messages.toMutableList().apply { this[userIndex] = enriched }
    }

    fun appendCitations(
        response: String,
        sources: List<QuestionCorrectionSource>,
    ): String {
        if (sources.isEmpty()) return response
        return buildString {
            append(response.trimEnd())
            append("\n\n---\n参考来源")
            sources.forEachIndexed { index, source ->
                append("\n\n[${index + 1}] ")
                append(source.title.ifBlank { source.url })
                append("\n")
                append(source.url)
                if (source.publishedDate.isNotBlank()) {
                    append("\n时间: ${source.publishedDate}")
                }
                val snippetLine = source.snippet.replace('\n', ' ').trim().take(MAX_SNIPPET_LENGTH)
                if (snippetLine.isNotBlank()) {
                    append("\n摘要: $snippetLine")
                }
            }
        }
    }

    fun formatSources(sources: List<QuestionCorrectionSource>): String {
        if (sources.isEmpty()) return "【联网检索结果】未检索到可用来源。"
        return buildString {
            append("【联网检索结果】")
            sources.forEachIndexed { index, source ->
                append("\n\n${index + 1}. ${source.title}")
                append("\nURL: ${source.url}")
                append("\n摘要: ${source.snippet}")
            }
        }
    }

    private const val MAX_QUERY_LENGTH = 400
    private const val MAX_SNIPPET_LENGTH = 150
}
