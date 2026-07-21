package com.example.testapp.data.network.deepseek

/** 答题页 AI 区：持久化/展示文本 → 可渲染的正文。 */
object SessionAnalysisInlineDisplayPipeline {

    fun toDisplayText(persistedOrDisplay: String, questionStem: String = ""): String {
        val trimmed = persistedOrDisplay.trim()
        if (trimmed.isBlank()) return ""
        if (DeepSeekAskPersistFormatPipeline.isStructured(trimmed) ||
            DeepSeekAskPersistFormatPipeline.ASSISTANT_SEPARATOR in trimmed
        ) {
            val display = DeepSeekAskDisplayPipeline.fromTurns(
                DeepSeekAskPersistFormatPipeline.decode(questionStem, trimmed)
            ).ifBlank { trimmed }
            return display
        }
        return trimmed
    }
}
