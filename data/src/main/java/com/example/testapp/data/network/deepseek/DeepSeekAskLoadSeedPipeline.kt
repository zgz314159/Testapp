package com.example.testapp.data.network.deepseek

/** 打开问答页时合并 DB 持久化与会话内文本：优先结构化多轮，其次更长文本。 */
object DeepSeekAskLoadSeedPipeline {

    fun resolveRaw(
        dbRaw: String?,
        seedDisplay: String?,
    ): String? {
        val candidates = listOf(dbRaw, seedDisplay)
            .mapNotNull { it?.trim()?.takeIf { s -> s.isNotEmpty() } }
        if (candidates.isEmpty()) return null
        return candidates.maxWithOrNull(compareBy({ richness(it) }, { it.length }))
    }

    /** 会话写回/同步写库：禁止用扁平正文覆盖结构化多轮。 */
    fun resolvePreferStructured(
        existing: String?,
        incoming: String?,
    ): String {
        return resolveRaw(existing, incoming).orEmpty()
    }

    fun richness(text: String): Int {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return -1
        return when {
            DeepSeekAskPersistFormatPipeline.USER_MARKER in trimmed &&
                DeepSeekAskPersistFormatPipeline.ASSISTANT_MARKER in trimmed -> 2
            DeepSeekAskPersistFormatPipeline.ASSISTANT_SEPARATOR in trimmed -> 1
            else -> 0
        }
    }
}
