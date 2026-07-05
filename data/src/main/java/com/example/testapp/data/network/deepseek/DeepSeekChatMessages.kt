package com.example.testapp.data.network.deepseek

import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekChatMessage(
    val role: String,
    val content: String
)

/** 组装 DeepSeek messages：system（风格）+ user（任务）。 */
object DeepSeekChatMessages {

    fun build(userContent: String): List<DeepSeekChatMessage> =
        DeepSeekMultiTurnMessagesPipeline.build(emptyList(), userContent)
}
