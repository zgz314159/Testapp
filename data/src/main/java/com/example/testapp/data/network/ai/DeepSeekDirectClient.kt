package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekDirectClient @Inject constructor(
    private val client: HttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun chat(
        apiKey: String,
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean = false,
        temperature: Double = 0.35,
        maxTokens: Int = 4096,
    ): String {
        val payload = buildJsonObject {
            put("model", MODEL)
            put(
                "messages",
                buildJsonArray {
                    messages.forEach { msg ->
                        add(
                            buildJsonObject {
                                put("role", msg.role)
                                put("content", msg.content)
                            },
                        )
                    }
                },
            )
            put("max_tokens", maxTokens)
            put("temperature", temperature)
            put("presence_penalty", 0)
            put(
                "thinking",
                buildJsonObject {
                    put("type", if (enableThinking) "enabled" else "disabled")
                },
            )
        }
        val httpResponse = client.post {
            url(DEEPSEEK_URL)
            header("Authorization", "Bearer ${apiKey.trim()}")
            header("Content-Type", "application/json")
            setBody(TextContent(payload.toString(), ContentType.Application.Json))
        }
        val raw = httpResponse.bodyAsText()
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("DeepSeek 调用失败（HTTP ${httpResponse.status.value}）")
        }
        val body = json.decodeFromString(DeepSeekResponse.serializer(), raw)
        val content = body.choices.firstOrNull()?.message?.content.orEmpty()
        return content.replace("*", "").replace("_", "")
    }

    @Serializable
    private data class DeepSeekResponse(
        val choices: List<DeepSeekChoice> = emptyList(),
    )

    @Serializable
    private data class DeepSeekChoice(
        val message: DeepSeekMsg? = null,
    )

    @Serializable
    private data class DeepSeekMsg(
        val content: String? = null,
    )

    private companion object {
        const val DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions"
        const val MODEL = "deepseek-v4-flash"
    }
}
