package com.example.testapp.data.network.spark

import android.util.Log
import com.example.testapp.BuildConfig
import com.example.testapp.domain.model.Question
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
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
    val model: String = "x1",
    val messages: List<Message>,
    @SerialName("max_tokens") val maxTokens: Int = 4096
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
    private val json = Json { ignoreUnknownKeys = true }

    private fun stripMarkdown(text: String): String {
        return text.replace("*", "").replace("_", "")
    }

    // 重试机制的辅助函数
    private suspend fun <T> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        backoffFactor: Double = 2.0,
        operation: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                Log.d("SparkApiService", "Attempting request (${attempt + 1}/$maxRetries)")
                return operation()
            } catch (e: HttpRequestTimeoutException) {
                Log.w("SparkApiService", "Attempt ${attempt + 1} failed with timeout: ${e.message}")
                lastException = e
                if (attempt < maxRetries - 1) {
                    Log.i("SparkApiService", "Retrying in ${currentDelay}ms...")
                    delay(currentDelay)
                    currentDelay = (currentDelay * backoffFactor).toLong().coerceAtMost(maxDelayMs)
                }
            } catch (e: Exception) {
                // 对于非超时错误，记录并不重试
                Log.e("SparkApiService", "Request failed with non-timeout error: ${e.javaClass.simpleName}: ${e.message}")
                throw e
            }
        }
        Log.e("SparkApiService", "All $maxRetries retry attempts failed")
        throw lastException ?: Exception("All retry attempts failed")
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

        Log.d("SparkApiService", "Prompt=\n$prompt")

        val requestBody = RequestBody(
            messages = listOf(Message("user", prompt)),
            maxTokens = 4096
        )

        Log.d("SparkApiService", "RequestBodyJson=${json.encodeToString(requestBody)}")

        val requestStart = System.currentTimeMillis()
        Log.d("SparkApiService", "Sending request...")

        val httpResponse: HttpResponse = retryWithBackoff(maxRetries = 3) {
            client.post {
                url("https://spark-api-open.xf-yun.com/v2/chat/completions")
                header("Authorization", "Bearer ${BuildConfig.SPARK_API_KEY}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(requestBody)
            }
        }
        
        val networkDuration = System.currentTimeMillis() - requestStart
        Log.d("SparkApiService", "Network duration=${networkDuration} ms")
        Log.d("SparkApiService", "Status=${httpResponse.status.value}, Headers=${httpResponse.headers}")

        val raw: String = httpResponse.bodyAsText()
        Log.d("SparkApiService", "RawResponse=$raw")

        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }

        val parseStart = System.currentTimeMillis()
        val response = try {
            json.decodeFromString<ResponseData>(raw)
        } catch (e: Exception) {
            Log.e("SparkApiService", "Parse response failed", e)
            throw e
        }
        val parseDuration = System.currentTimeMillis() - parseStart
        Log.d("SparkApiService", "Parse duration=${parseDuration} ms")
        Log.d("SparkApiService", "Response=$response")
        val totalDuration = System.currentTimeMillis() - totalStart
        Log.d("SparkApiService", "Total analyze duration=${totalDuration} ms")

        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
    
    suspend fun ask(text: String): String {
        val totalStart = System.currentTimeMillis()
        
        Log.d("SparkApiService", "Ask prompt=\n$text")
        
        val requestBody = RequestBody(
            messages = listOf(Message("user", text)),
            maxTokens = 4096
        )
        Log.d("SparkApiService", "Ask RequestBodyJson=${json.encodeToString(requestBody)}")
        
        val requestStart = System.currentTimeMillis()
        Log.d("SparkApiService", "Ask sending request...")
        
        val httpResponse: HttpResponse = retryWithBackoff(maxRetries = 3) {
            client.post {
                url("https://spark-api-open.xf-yun.com/v2/chat/completions")
                header("Authorization", "Bearer ${BuildConfig.SPARK_API_KEY}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(requestBody)
            }
        }
        
        val networkDuration = System.currentTimeMillis() - requestStart
        Log.d("SparkApiService", "Ask network duration=${networkDuration} ms")
        Log.d("SparkApiService", "Ask status=${httpResponse.status.value}, Headers=${httpResponse.headers}")
        
        val raw = httpResponse.bodyAsText()
        Log.d("SparkApiService", "Ask RawResponse=$raw")
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }
        
        val parseStart = System.currentTimeMillis()
        val response = try {
            json.decodeFromString<ResponseData>(raw)
        } catch (e: Exception) {
            Log.e("SparkApiService", "Ask parse response failed", e)
            throw e
        }
        val parseDuration = System.currentTimeMillis() - parseStart
        Log.d("SparkApiService", "Ask parse duration=${parseDuration} ms")
        Log.d("SparkApiService", "Ask response=$response")
        val totalDuration = System.currentTimeMillis() - totalStart
        Log.d("SparkApiService", "Total ask duration=${totalDuration} ms")
        
        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
}