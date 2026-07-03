package com.example.testapp.uicommon.model

/** 单轮 user → assistant，供展示管道扁平化。 */
data class AiChatTurn(
    val user: String,
    val assistant: String
)
