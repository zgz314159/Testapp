package com.example.testapp.core.util

/** 将 LaTeX 常见定界符转为 RichText 可识别的 `$…$` / `$$…$$`。 */
object RichTextLatexDelimiterPipeline {

    private val BlockDelimited = Regex("""\\\[([\s\S]+?)\\]""")
    private val InlineDelimited = Regex("""\\\((.+?)\\\)""")

    fun normalize(text: String): String {
        if (!text.contains('\\')) return text
        return text.replace(BlockDelimited) { match ->
            val body = match.groupValues[1].trim()
            if (body.isBlank()) match.value else "$$$body$$"
        }.replace(InlineDelimited) { match ->
            val body = match.groupValues[1].trim()
            if (body.isBlank()) match.value else "$$body$"
        }
    }
}
