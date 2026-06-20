package com.example.testapp.core.util

private val MARKDOWN_CODE_FENCE = Regex("""(?s)^```[A-Za-z0-9_-]*\s*\n([\s\S]*?)\n?```\s*$""")
private val LINE_BULLET_PREFIX = Regex("""^[\u2022\u00B7\u25CF\u25AA\u25AB\u2219?？]\s+""")

fun unwrapMarkdownCodeFence(text: String): String {
    val trimmed = text.trim()
    MARKDOWN_CODE_FENCE.matchEntire(trimmed)?.let { return it.groupValues[1].trim() }
    if (!trimmed.startsWith("```")) return trimmed

    var body = trimmed.removePrefix("```")
    val languageBreak = body.indexOf('\n')
    body = if (languageBreak >= 0) {
        body.substring(languageBreak + 1)
    } else {
        body.replaceFirst(Regex("""^[A-Za-z0-9_-]*"""), "")
    }
    body = body.replace(Regex("""\n?```\s*$"""), "")
    return body.trim()
}

fun normalizeRichDisplayBullets(text: String): String {
    return text.lineSequence().joinToString("\n") { line ->
        val trimmed = line.trimStart()
        val match = LINE_BULLET_PREFIX.matchEntire(trimmed) ?: return@joinToString line
        val indent = line.substring(0, line.length - trimmed.length)
        val content = trimmed.substring(match.range.last + 1).trimStart()
        "$indent- $content"
    }
}

fun prepareRichDisplayText(raw: String): String {
    return unwrapMarkdownCodeFence(raw)
        .let(::normalizeRichDisplayBullets)
        .let(::normalizeRichMarkdownStructure)
}
