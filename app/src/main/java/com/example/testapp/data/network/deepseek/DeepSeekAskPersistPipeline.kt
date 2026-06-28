package com.example.testapp.data.network.deepseek

/** DeepSeek 提问结果：与解析共用 question_analysis 存储，兼容旧 question_ask。 */
object DeepSeekAskPersistPipeline {

    private const val LEGACY_ASK_NOTE_PREFIX = "【DeepSeek问答】"

    fun resolveLoadText(analysis: String?, askLegacy: String?): String? {
        val fromAnalysis = analysis?.trim().orEmpty()
        if (fromAnalysis.isNotBlank()) return fromAnalysis
        val fromAsk = askLegacy?.trim().orEmpty()
        if (fromAsk.isNotBlank()) return fromAsk
        return null
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
