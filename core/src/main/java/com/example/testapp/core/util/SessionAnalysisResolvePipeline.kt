package com.example.testapp.core.util

/** 流式 AI 结果 vs 会话快照：优先非空会话/列表，避免占位流式结果遮蔽已保存解析。 */
object SessionAnalysisResolvePipeline {

    fun resolve(
        currentIndex: Int,
        streamingPair: Pair<Int, String?>?,
        sessionValue: String?,
        listValue: String?,
        parsingKeyword: String
    ): String? {
        val fromSession = sessionValue?.trim()?.takeIf { it.isNotBlank() }
        if (fromSession != null) return fromSession
        val fromList = listValue?.trim()?.takeIf { it.isNotBlank() }
        if (fromList != null) return fromList
        return streamingPair
            ?.takeIf { it.first == currentIndex }
            ?.second
            ?.trim()
            ?.takeIf { it.isNotBlank() && !it.startsWith(parsingKeyword) }
    }
}
