package com.example.testapp.uicommon.design

/** 联网回答的单条参考来源。 */
data class AiChatSourceRef(
    val index: Int,
    val title: String,
    val url: String,
    val publishedDate: String = "",
    val snippet: String = "",
) {
    /** 站点域名（去 www 前缀），用于来源列表展示与 favicon 加载。 */
    val host: String
        get() = url.substringAfter("://").substringBefore('/').removePrefix("www.")

    val faviconUrl: String
        get() {
            val rawHost = url.substringAfter("://").substringBefore('/')
            return if (rawHost.isBlank()) "" else "https://$rawHost/favicon.ico"
        }
}

/** 正文与来源拆分结果。 */
data class AiChatSourcesSplit(
    val body: String,
    val sources: List<AiChatSourceRef>,
)

/**
 * 解析联网回答尾部的「参考来源」块（由 data 层 AiWebSearchPromptPipeline.appendCitations 生成，
 * 格式：`---\n参考来源` 后跟若干 `[n] 标题\nURL[\n时间: …][\n摘要: …]`）。
 * UI 只展示正文，来源改为胶囊 + 底部列表。
 */
object AiChatSourcesPipeline {

    private const val MARKER = "---\n参考来源"
    private val entryRegex = Regex(
        """\[(\d+)]\s*([^\n]*)\n(https?://\S+)(?:\n时间: ([^\n]*))?(?:\n摘要: ([^\n]*))?""",
    )

    fun split(content: String): AiChatSourcesSplit {
        val markerIndex = content.lastIndexOf(MARKER)
        if (markerIndex < 0) return AiChatSourcesSplit(content, emptyList())
        val block = content.substring(markerIndex)
        val sources = entryRegex.findAll(block).map { match ->
            AiChatSourceRef(
                index = match.groupValues[1].toIntOrNull() ?: 0,
                title = match.groupValues[2].trim(),
                url = match.groupValues[3].trim(),
                publishedDate = match.groupValues[4].trim(),
                snippet = match.groupValues[5].trim(),
            )
        }.toList()
        if (sources.isEmpty()) return AiChatSourcesSplit(content, emptyList())
        val body = content.substring(0, markerIndex).trimEnd()
        return AiChatSourcesSplit(body, sources)
    }

    /** 编辑正文后重挂来源块，保证保存/恢复的文本格式不变。 */
    fun reattach(body: String, sources: List<AiChatSourceRef>): String {
        if (sources.isEmpty()) return body
        return buildString {
            append(body.trimEnd())
            append("\n\n")
            append(MARKER)
            sources.forEach { source ->
                append("\n\n[${source.index}] ${source.title}\n${source.url}")
                if (source.publishedDate.isNotBlank()) {
                    append("\n时间: ${source.publishedDate}")
                }
                if (source.snippet.isNotBlank()) {
                    append("\n摘要: ${source.snippet}")
                }
            }
        }
    }
}
