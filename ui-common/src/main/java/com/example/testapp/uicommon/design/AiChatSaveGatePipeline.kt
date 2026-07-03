package com.example.testapp.uicommon.design

/** 无状态：返回键是否应弹出保存确认。 */
object AiChatSaveGatePipeline {

    fun shouldConfirmSave(
        content: String,
        isParsing: Boolean,
        parsingKeyword: String,
        parseFailedKeyword: String
    ): Boolean {
        val trimmed = content.trim()
        if (trimmed.isEmpty() || isParsing) return false
        if (trimmed.contains(parsingKeyword)) return false
        if (trimmed.contains(parseFailedKeyword)) return false
        return true
    }
}
