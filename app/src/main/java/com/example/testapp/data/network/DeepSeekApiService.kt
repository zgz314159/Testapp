package com.example.testapp.data.network

import com.example.testapp.BuildConfig
import com.example.testapp.domain.model.Question
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    @SerialName("max_tokens") val maxTokens: Int = 512,
    val stream: Boolean = true
)

@Serializable
private data class ResponseChoice(
    val message: Message
)

@Serializable
private data class ResponseData(
    val choices: List<ResponseChoice> = emptyList()
)

@Serializable
private data class Delta(
    val content: String? = null
)

@Serializable
private data class StreamChoice(
    val delta: Delta? = null
)

@Serializable
private data class StreamResponse(
    val choices: List<StreamChoice> = emptyList()
)

class DeepSeekApiService(private val client: HttpClient) {

    private fun stripMarkdown(text: String): String {
        return text.replace("*", "").replace("_", "")
    }

    fun analyze(question: Question): Flow<String> = flow {

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
            maxTokens = 512,
            stream = true
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
                header(HttpHeaders.Accept, "text/event-stream")
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

        if (!httpResponse.status.isSuccess()) {
            val raw: String = httpResponse.body()
            android.util.Log.d("DeepSeekApiService", "RawResponse=$raw")
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }
        val channel = httpResponse.bodyAsChannel()
        val reader = channel.toInputStream().bufferedReader()
        val buffer = StringBuilder()
        android.util.Log.d("DeepSeekApiService", "== 开始读取deepseek流 ==");
        while (true) {
            val line = reader.readLine() ?: break
            android.util.Log.d("DeepSeekApiService", "SSE Line: $line")   // <-- 新加
            if (!line.startsWith("data:")) continue
            val data = line.removePrefix("data:").trim()
            if (data == "[DONE]") break
            try {
                val resp = Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<StreamResponse>(data)

                val delta = resp.choices.firstOrNull()?.delta?.content
                if (!delta.isNullOrBlank()) {
                    buffer.append(delta)
                    emit(buffer.toString())  // 关键：每次 emit 当前累积内容
                }
            } catch (e: Exception) {
                android.util.Log.e("DeepSeekApiService", "Parse stream chunk failed", e)
            }
        }
        val totalDuration = System.currentTimeMillis() - totalStart
        android.util.Log.d(
            "DeepSeekApiService",
            "Total analyze duration=${totalDuration} ms"
        )

    }
}