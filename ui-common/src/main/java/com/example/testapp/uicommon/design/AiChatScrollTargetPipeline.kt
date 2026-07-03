package com.example.testapp.uicommon.design

/** 无状态：LazyColumn 应滚动到的最后一项索引。 */
object AiChatScrollTargetPipeline {

    fun lastIndex(
        messageCount: Int,
        showTyping: Boolean,
        showError: Boolean
    ): Int {
        var count = messageCount
        if (showTyping) count++
        if (showError) count++
        return (count - 1).coerceAtLeast(0)
    }
}
