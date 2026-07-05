package com.example.testapp.data.network.deepseek

/** 答题页 AI 区：持久化/展示文本 → 可渲染的正文。 */
object SessionAnalysisInlineDisplayPipeline {

    private const val USER_MARKER = "【DS·问】"

    fun toDisplayText(persistedOrDisplay: String, questionStem: String = ""): String {
        val trimmed = persistedOrDisplay.trim()
        if (trimmed.isBlank()) return ""
        if (USER_MARKER in trimmed || DeepSeekAskPersistFormatPipeline.ASSISTANT_SEPARATOR in trimmed) {
            return DeepSeekAskDisplayPipeline.fromTurns(
                DeepSeekAskPersistFormatPipeline.decode(questionStem, trimmed)
            ).ifBlank { trimmed }
        }
        return trimmed
    }
}
