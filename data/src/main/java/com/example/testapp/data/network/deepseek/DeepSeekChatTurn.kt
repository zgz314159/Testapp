package com.example.testapp.data.network.deepseek

/** 单轮 user → assistant，供多轮 messages 拼接。 */
data class DeepSeekChatTurn(
    val user: String,
    val assistant: String
)
