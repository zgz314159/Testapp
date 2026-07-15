package com.example.testapp.data.network.deepseek

/**
 * DeepSeek Chat Completions 参数。
 *
 * - 日常解析：thinking disabled、低温度
 * - 标答校对：可临时开启 thinking（由 [DeepSeekApiService.chat] 传入）
 */
object DeepSeekChatConfig {
    const val API_URL = "https://api.deepseek.com/v1/chat/completions"
    const val MODEL = "deepseek-v4-flash"
    const val MAX_TOKENS = 4096

    /** 考试解析：降低随机性，减少答案漂移。 */
    const val TEMPERATURE = 0.35
    const val PRESENCE_PENALTY = 0.0
    const val THINKING_DISABLED = "disabled"
    const val THINKING_ENABLED = "enabled"

    /** 为 true 时在标答校对请求中附带 search_web tool 声明（需后续 tool 回环才真正检索）。 */
    const val ATTACH_WEB_SEARCH_TOOL_ON_REVIEW = true
}
