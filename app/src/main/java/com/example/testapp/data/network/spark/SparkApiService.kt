package com.example.testapp.data.network.spark

import com.example.testapp.BuildConfig
import com.example.testapp.domain.model.Question
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class Message(
    val role: String,
    val content: String
)

@Serializable
private data class RequestBody(
    val model: String = "spark",
    val messages: List<Message>,
    @SerialName("max_tokens") val maxTokens: Int = 512
)

@Serializable
private data class ResponseChoice(
    val message: Message
)

@Serializable
private data class ResponseData(
    val choices: List<ResponseChoice> = emptyList()
)

class SparkApiService(private val client: HttpClient) {

    private fun stripMarkdown(text: String): String {
        return text.replace("*", "").replace("_", "")
    }

    suspend fun analyze(question: Question): String {
        val prompt = buildString {
            appendLine(question.content)
            question.options.forEachIndexed { index, option ->
                val letter = ('A' + index)
                appendLine("$letter. $option")
            }
            append("请给出正确答案和解析。")
        }

        val requestBody = RequestBody(
            messages = listOf(Message("user", prompt)),
            maxTokens = 512
        )

        val httpResponse: HttpResponse = client.post {
            url("https://spark-api-open.xf-yun.com/v1/chat/completions")
            header("Authorization", "Bearer ${BuildConfig.SPARK_API_KEY}")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(requestBody)
        }
        val raw: String = httpResponse.body()
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${'$'}{httpResponse.status.value}: ${'$'}raw")
        }
        val response = httpResponse.body<ResponseData>()
        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
}