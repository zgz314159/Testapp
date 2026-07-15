package com.example.testapp.data.network.deepseek

/** DeepSeek 提问结果：与解析共用 question_analysis 存储，兼容旧 question_ask。 */
object DeepSeekAskPersistPipeline {

    private const val LEGACY_ASK_NOTE_PREFIX = "【DeepSeek问答】"

    fun resolveLoadText(analysis: String?, askLegacy: String?): String? {
        // 优先结构化/更长文本，避免 analysis 短文遮蔽 ask 表里的完整多轮
        return DeepSeekAskLoadSeedPipeline.resolveRaw(analysis, askLegacy)
    }

    fun extractFromAskNote(note: String?): String? {
        val trimmed = note?.trim().orEmpty()
        if (trimmed.isBlank()) return null
        return if (trimmed.startsWith(LEGACY_ASK_NOTE_PREFIX)) {
            trimmed.removePrefix(LEGACY_ASK_NOTE_PREFIX).trimStart()
        } else {
            null
        }
    }
}
