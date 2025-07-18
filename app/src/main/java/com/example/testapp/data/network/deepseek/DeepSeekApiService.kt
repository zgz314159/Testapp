package com.example.testapp.data.network.deepseek

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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class Message(
    val role: String,
    val content: String
)

@Serializable
private data class RequestBody(
    val model: String = "deepseek-chat",
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

class DeepSeekApiService(private val client: HttpClient) {

    private fun stripMarkdown(text: String): String {
        return text.replace("*", "").replace("_", "")
    }

    suspend fun analyze(question: Question): String {

        val totalStart = System.currentTimeMillis()

        val prompt = buildString {
            appendLine(question.content)
            question.options.forEachIndexed { index, option ->
                val letter = ('A' + index)
                appendLine("$letter. $option")
            }
            append("请给出正确答案和解析。")
        }

        android.util.Log.d("DeepSeekApiService", "Prompt=\n$prompt")

        val requestBody = RequestBody(
            messages = listOf(Message("user", prompt)),
            maxTokens = 512
        )
        android.util.Log.d(
            "DeepSeekApiService",
            "RequestBodyJson=${Json.encodeToString(requestBody)}"
        )

        val requestStart = System.currentTimeMillis()
        android.util.Log.d("DeepSeekApiService", "Sending request...")

        val httpResponse: HttpResponse = try {
            client.post {
                url("https://api.deepseek.com/v1/chat/completions")
                header("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(requestBody)
            }
        } catch (e: Exception) {
            android.util.Log.e("DeepSeekApiService", "Request failed", e)
            throw e
        }
        val networkDuration = System.currentTimeMillis() - requestStart
        android.util.Log.d(
            "DeepSeekApiService",
            "Network duration=${networkDuration} ms"
        )
        android.util.Log.d(
            "DeepSeekApiService",
            "Status=${httpResponse.status.value}, Headers=${httpResponse.headers}"
        )
        val raw: String = httpResponse.body()
        android.util.Log.d("DeepSeekApiService", "RawResponse=$raw")
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }
        val parseStart = System.currentTimeMillis()
        val response = try {
            httpResponse.body<ResponseData>()
        } catch (e: Exception) {
            android.util.Log.e("DeepSeekApiService", "Parse response failed", e)
            throw e
        }
        val parseDuration = System.currentTimeMillis() - parseStart
        android.util.Log.d("DeepSeekApiService", "Parse duration=${parseDuration} ms")
        android.util.Log.d("DeepSeekApiService", "Response=$response")
        val totalDuration = System.currentTimeMillis() - totalStart
        android.util.Log.d(
            "DeepSeekApiService",
            "Total analyze duration=${totalDuration} ms"
        )
        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
    suspend fun ask(text: String): String {
        val requestBody = RequestBody(
            messages = listOf(Message("user", text)),
            maxTokens = 512
        )
        val httpResponse = client.post {
            url("https://api.deepseek.com/v1/chat/completions")
            header("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(requestBody)
        }
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${'$'}{httpResponse.status.value}: ${'$'}{httpResponse.body<String>()}")
        }
        val response = httpResponse.body<ResponseData>()
        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
}