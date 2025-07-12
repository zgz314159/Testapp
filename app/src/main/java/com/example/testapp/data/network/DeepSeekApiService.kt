package com.example.testapp.data.network

import com.example.testapp.BuildConfig
import com.example.testapp.domain.model.Question
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class Message(
    val role: String,
    val content: String
)

@Serializable
private data class RequestBody(
    val model: String = "deepseek-chat",
    val messages: List<Message>
)

@Serializable
private data class ResponseChoice(
    val message: Message
)

@Serializable
private data class ResponseData(
    val choices: List<ResponseChoice> = emptyList()
)

class DeepSeekApiService(private val client: HttpClient) {
    suspend fun analyze(question: Question): String {
        val prompt = buildString {
            appendLine(question.content)
            question.options.forEachIndexed { index, option ->
                val letter = ('A' + index)
                appendLine("$letter. $option")
            }
            append("请给出正确答案和解析。")
        }
        val response: ResponseData = client.post {
            url("https://api.deepseek.com/v1/chat/completions")
            header("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(RequestBody(messages = listOf(Message("user", prompt))))
        }.body()
        return response.choices.firstOrNull()?.message?.content ?: ""
    }
}