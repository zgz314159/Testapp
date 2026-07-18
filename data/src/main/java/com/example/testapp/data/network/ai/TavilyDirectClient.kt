package com.example.testapp.data.network.ai

import com.example.testapp.domain.model.QuestionCorrectionSource
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TavilyDirectClient @Inject constructor(
    private val client: HttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun search(
        apiKey: String,
        query: String,
        maxResults: Int = 5,
    ): List<QuestionCorrectionSource> {
        val payload = buildJsonObject {
            put("query", query.take(400))
            put("max_results", maxResults)
            put("search_depth", "basic")
            put("include_answer", false)
        }
        val httpResponse = client.post {
            url(TAVILY_URL)
            header("Authorization", "Bearer ${apiKey.trim()}")
            header("Content-Type", "application/json")
            setBody(TextContent(payload.toString(), ContentType.Application.Json))
        }
        val raw = httpResponse.bodyAsText()
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("Tavily 检索失败（HTTP ${httpResponse.status.value}）")
        }
        val body = json.decodeFromString(TavilyResponse.serializer(), raw)
        return body.results
            .filter { it.url.isNotBlank() && (it.title.isNotBlank() || it.content.isNotBlank()) }
            .take(maxResults)
            .map {
                QuestionCorrectionSource(
                    title = it.title.take(200),
                    url = it.url,
                    snippet = it.content.take(500),
                    publishedDate = it.publishedDate.trim().take(10)
                        .takeIf { d -> d.matches(Regex("""\d{4}-\d{2}-\d{2}""")) }
                        .orEmpty(),
                )
            }
    }

    @Serializable
    private data class TavilyResponse(
        val results: List<TavilyResult> = emptyList(),
    )

    @Serializable
    private data class TavilyResult(
        val title: String = "",
        val url: String = "",
        val content: String = "",
        @SerialName("published_date")
        val publishedDate: String = "",
    )

    private companion object {
        const val TAVILY_URL = "https://api.tavily.com/search"
    }
}
