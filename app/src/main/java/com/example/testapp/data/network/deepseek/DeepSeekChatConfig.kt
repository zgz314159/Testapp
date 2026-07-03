package com.example.testapp.data.network.deepseek

/** DeepSeek Chat Completions 参数：考试场景低温度、稳定 system prompt。 */
object DeepSeekChatConfig {
    const val API_URL = "https://api.deepseek.com/v1/chat/completions"
    const val MODEL = "deepseek-v4-flash"
    const val MAX_TOKENS = 4096
    /** 考试解析：降低随机性，减少答案漂移。 */
    const val TEMPERATURE = 0.35
    const val PRESENCE_PENALTY = 0.0
    const val THINKING_DISABLED = "disabled"
}
