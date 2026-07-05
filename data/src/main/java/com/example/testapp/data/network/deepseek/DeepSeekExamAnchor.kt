package com.example.testapp.data.network.deepseek

/** 单题锚点：多轮对话中保持稳定，避免随聊天漂移。 */
data class DeepSeekExamAnchor(
    val questionType: String,
    val content: String,
    val options: List<String>,
    val standardAnswer: String,
    val officialExplanation: String
)
