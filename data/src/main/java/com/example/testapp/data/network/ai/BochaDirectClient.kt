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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/** 博查 Web Search：大陆直连、中文检索质量优；响应为 Bing 兼容结构。 */
@Singleton
class BochaDirectClient @Inject constructor(
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
            put("freshness", "noLimit")
            put("summary", true)
            put("count", maxResults)
        }
        val httpResponse = client.post {
            url(BOCHA_URL)
            header("Authorization", "Bearer ${apiKey.trim()}")
            header("Content-Type", "application/json")
            setBody(TextContent(payload.toString(), ContentType.Application.Json))
        }
        val raw = httpResponse.bodyAsText()
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("博查检索失败（HTTP ${httpResponse.status.value}）")
        }
        val body = json.decodeFromString(BochaResponse.serializer(), raw)
        return body.data.webPages.value
            .filter { it.url.isNotBlank() && (it.name.isNotBlank() || it.snippet.isNotBlank() || it.summary.isNotBlank()) }
            .take(maxResults)
            .map {
                QuestionCorrectionSource(
                    title = it.name.take(200),
                    url = it.url,
                    snippet = it.summary.ifBlank { it.snippet }.take(500),
                    publishedDate = normalizeDate(it.datePublished.ifBlank { it.dateLastCrawled }),
                )
            }
    }

    /** ISO 时间戳只保留日期部分。 */
    private fun normalizeDate(raw: String): String =
        raw.trim().take(10).takeIf { it.matches(Regex("""\d{4}-\d{2}-\d{2}""")) }.orEmpty()

    @Serializable
    private data class BochaResponse(
        val data: BochaData = BochaData(),
    )

    @Serializable
    private data class BochaData(
        val webPages: BochaWebPages = BochaWebPages(),
    )

    @Serializable
    private data class BochaWebPages(
        val value: List<BochaWebPage> = emptyList(),
    )

    @Serializable
    private data class BochaWebPage(
        val name: String = "",
        val url: String = "",
        val snippet: String = "",
        val summary: String = "",
        val datePublished: String = "",
        val dateLastCrawled: String = "",
    )

    private companion object {
        const val BOCHA_URL = "https://api.bochaai.com/v1/web-search"
    }
}
