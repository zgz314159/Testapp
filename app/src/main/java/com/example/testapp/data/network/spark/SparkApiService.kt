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
                
                return operation()
            } catch (e: HttpRequestTimeoutException) {
                
                lastException = e
                if (attempt < maxRetries - 1) {
                    
                    delay(currentDelay)
                    currentDelay = (currentDelay * backoffFactor).toLong().coerceAtMost(maxDelayMs)
                }
            } catch (e: Exception) {
                // 对于非超时错误，记录并不重试
                
                throw e
            }
        }
        
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

        val requestBody = RequestBody(
            messages = listOf(Message("user", prompt)),
            maxTokens = 4096
        )

        val requestStart = System.currentTimeMillis()

        val httpResponse: HttpResponse = retryWithBackoff(maxRetries = 3) {
            client.post {
                url("https://spark-api-open.xf-yun.com/v2/chat/completions")
                header("Authorization", "Bearer ${BuildConfig.SPARK_API_KEY}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(requestBody)
            }
        }
        
        val networkDuration = System.currentTimeMillis() - requestStart

        val raw: String = httpResponse.bodyAsText()

        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }

        val parseStart = System.currentTimeMillis()
        val response = try {
            json.decodeFromString<ResponseData>(raw)
        } catch (e: Exception) {
            
            throw e
        }
        val parseDuration = System.currentTimeMillis() - parseStart

        val totalDuration = System.currentTimeMillis() - totalStart

        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
    
    suspend fun ask(text: String): String {
        val totalStart = System.currentTimeMillis()

        val requestBody = RequestBody(
            messages = listOf(Message("user", text)),
            maxTokens = 4096
        )

        val requestStart = System.currentTimeMillis()

        val httpResponse: HttpResponse = retryWithBackoff(maxRetries = 3) {
            client.post {
                url("https://spark-api-open.xf-yun.com/v2/chat/completions")
                header("Authorization", "Bearer ${BuildConfig.SPARK_API_KEY}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(requestBody)
            }
        }
        
        val networkDuration = System.currentTimeMillis() - requestStart

        val raw = httpResponse.bodyAsText()
        
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }
        
        val parseStart = System.currentTimeMillis()
        val response = try {
            json.decodeFromString<ResponseData>(raw)
        } catch (e: Exception) {
            
            throw e
        }
        val parseDuration = System.currentTimeMillis() - parseStart

        val totalDuration = System.currentTimeMillis() - totalStart

        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
}