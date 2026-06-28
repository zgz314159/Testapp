package com.example.testapp.data.network.deepseek

/** DeepSeek Chat Completions 参数：对齐 Web/App 端自然对话风格。 */
object DeepSeekChatConfig {
    const val API_URL = "https://api.deepseek.com/v1/chat/completions"
    const val MODEL = "deepseek-v4-flash"
    const val MAX_TOKENS = 4096
    const val TEMPERATURE = 0.7
    const val PRESENCE_PENALTY = 0.3
    const val SYSTEM_PROMPT =
        "你是一个热情、自然、有人情味的AI助手。回答问题时要像朋友聊天一样，可以使用适当的语气词和口语表达，语气友好但保持专业。" +
            "回答结构要清晰，善用分点、加粗等格式增强可读性。结尾主动提供延伸帮助。" +
            "若用户指出你先前回答有误或与题目答案不符，请结合完整对话历史重新审视并修正。"
    const val THINKING_DISABLED = "disabled"
}
