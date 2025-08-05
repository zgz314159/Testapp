package com.example.testapp.data.network.baidu

import com.example.testapp.BuildConfig
import com.example.testapp.domain.model.Question
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
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
    val model: String = "ernie-3.5-8k",
    val messages: List<Message>,
    @SerialName("max_completion_tokens") val maxCompletionTokens: Int = 2048
)

@Serializable
private data class ResponseChoice(
    val message: Message
)

@Serializable
private data class ResponseData(
    val choices: List<ResponseChoice> = emptyList()
)

class BaiduApiService(private val client: HttpClient) {
    private val json = Json { 
        ignoreUnknownKeys = true
    }

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

        val requestBody = RequestBody(
            model = "ernie-3.5-8k",
            messages = listOf(Message("user", prompt)),
            maxCompletionTokens = 2048
        )

        val requestStart = System.currentTimeMillis()

        val httpResponse: HttpResponse = try {
            client.post {
                url("https://qianfan.baidubce.com/v2/chat/completions")
                header("Authorization", "Bearer ${BuildConfig.BAIDU_API_KEY}")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(requestBody)
            }
        } catch (e: Exception) {
            
            throw e
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

        // 添加详细耗时分析日志

        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
    
    suspend fun ask(text: String): String {
        val totalStart = System.currentTimeMillis()
        
        val requestBody = RequestBody(
            model = "ernie-3.5-8k",
            messages = listOf(Message("user", text)),
            maxCompletionTokens = 2048
        )
        
        val requestStart = System.currentTimeMillis()
        val httpResponse = client.post {
            url("https://qianfan.baidubce.com/v2/chat/completions")
            header("Authorization", "Bearer ${BuildConfig.BAIDU_API_KEY}")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(requestBody)
        }
        val networkDuration = System.currentTimeMillis() - requestStart
        
        val raw = httpResponse.bodyAsText()
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }
        
        val parseStart = System.currentTimeMillis()
        val response = json.decodeFromString<ResponseData>(raw)
        val parseDuration = System.currentTimeMillis() - parseStart
        
        val totalDuration = System.currentTimeMillis() - totalStart
        
        // 添加详细耗时分析日志

        val result = response.choices.firstOrNull()?.message?.content ?: ""
        return stripMarkdown(result)
    }
}
