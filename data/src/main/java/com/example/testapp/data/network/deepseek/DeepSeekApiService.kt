package com.example.testapp.data.network.deepseek

import com.example.testapp.data.BuildConfig
import com.example.testapp.domain.model.Question
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ThinkingConfig(val type: String)

@Serializable
private data class RequestBody(
    val model: String = DeepSeekChatConfig.MODEL,
    val messages: List<DeepSeekChatMessage>,
    @SerialName("max_tokens") val maxTokens: Int = DeepSeekChatConfig.MAX_TOKENS,
    val temperature: Double = DeepSeekChatConfig.TEMPERATURE,
    @SerialName("presence_penalty") val presencePenalty: Double = DeepSeekChatConfig.PRESENCE_PENALTY,
    val thinking: ThinkingConfig = ThinkingConfig(DeepSeekChatConfig.THINKING_DISABLED)
)

@Serializable
private data class ResponseChoice(
    val message: DeepSeekChatMessage
)

@Serializable
private data class ResponseData(
    val choices: List<ResponseChoice> = emptyList()
)

class DeepSeekApiService(private val client: HttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun stripMarkdown(text: String): String =
        text.replace("*", "").replace("_", "")

    private fun requireApiKey() {
        if (BuildConfig.DEEPSEEK_API_KEY.isBlank()) {
            throw IllegalStateException("未配置 DEEPSEEK_API_KEY，请在项目根目录 local.properties 中添加")
        }
    }

    private suspend fun complete(messages: List<DeepSeekChatMessage>): String {
        requireApiKey()
        val requestBody = RequestBody(messages = messages)
        val httpResponse: HttpResponse = client.post {
            url(DeepSeekChatConfig.API_URL)
            header("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(requestBody)
        }
        val raw = httpResponse.bodyAsText()
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }
        val response = json.decodeFromString<ResponseData>(raw)
        val result = response.choices.firstOrNull()?.message?.content.orEmpty()
        return stripMarkdown(result)
    }

    private suspend fun complete(userContent: String): String =
        complete(DeepSeekChatMessages.build(userContent))

    suspend fun analyze(question: Question): String {
        val prompt = DeepSeekExamPromptPipeline.buildAnalyzeUserContent(question)
        return complete(prompt)
    }

    suspend fun ask(text: String): String = complete(text)

    suspend fun chat(messages: List<DeepSeekChatMessage>): String = complete(messages)
}
