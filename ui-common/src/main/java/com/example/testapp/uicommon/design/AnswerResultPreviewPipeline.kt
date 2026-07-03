package com.example.testapp.uicommon.design

fun resolveAnswerResultPreviewLine(text: String, maxChars: Int = 96): String {
    val normalized = text.replace('\n', ' ').replace(Regex("\\s+"), " ").trim()
    if (normalized.length <= maxChars) return normalized
    return normalized.take(maxChars - 1) + "…"
}
