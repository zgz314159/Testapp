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
import io.ktor.http.content.TextContent
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

@Serializable
private data class ThinkingConfig(val type: String)

@Serializable
private data class RequestBody(
    val model: String = DeepSeekChatConfig.MODEL,
    val messages: List<DeepSeekChatMessage>,
    @SerialName("max_tokens") val maxTokens: Int = DeepSeekChatConfig.MAX_TOKENS,
    val temperature: Double = DeepSeekChatConfig.TEMPERATURE,
    @SerialName("presence_penalty") val presencePenalty: Double = DeepSeekChatConfig.PRESENCE_PENALTY,
    val thinking: ThinkingConfig = ThinkingConfig(DeepSeekChatConfig.THINKING_DISABLED),
)

@Serializable
private data class ResponseMessage(
    val role: String? = null,
    val content: String? = null,
    @SerialName("tool_calls") val toolCalls: List<ToolCallDto>? = null,
)

@Serializable
private data class ToolCallDto(
    val id: String = "",
    val type: String = "function",
    val function: ToolFunctionDto = ToolFunctionDto(),
)

@Serializable
private data class ToolFunctionDto(
    val name: String = "",
    val arguments: String = "{}",
)

@Serializable
private data class ResponseChoice(
    val message: ResponseMessage = ResponseMessage(),
)

@Serializable
private data class ResponseData(
    val choices: List<ResponseChoice> = emptyList(),
)

class DeepSeekApiService(private val client: HttpClient) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true // 与 NetworkModule 一致；否则 model 等带默认值字段会被省略 → HTTP 400
    }

    private fun stripMarkdown(text: String): String =
        text.replace("*", "").replace("_", "")

    private fun requireApiKey() {
        if (BuildConfig.DEEPSEEK_API_KEY.isBlank()) {
            throw IllegalStateException("未配置 DEEPSEEK_API_KEY，请在项目根目录 local.properties 中添加")
        }
    }

    private fun encodeRequest(
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean,
        attachWebSearchTool: Boolean,
    ): String {
        val base = RequestBody(
            messages = messages,
            thinking = ThinkingConfig(
                if (enableThinking) DeepSeekChatConfig.THINKING_ENABLED
                else DeepSeekChatConfig.THINKING_DISABLED,
            ),
        )
        val element = json.encodeToJsonElement(base) as JsonObject
        if (!attachWebSearchTool) {
            return json.encodeToString(JsonElement.serializer(), element)
        }
        val tool = json.parseToJsonElement(DeepSeekWebSearchToolSkeleton.TOOL_DEFINITION_JSON)
        val withTools = buildJsonObject {
            element.forEach { (k, v) -> put(k, v) }
            put("tools", JsonArray(listOf(tool)))
        }
        return json.encodeToString(JsonElement.serializer(), withTools)
    }

    private suspend fun postCompletion(
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean,
        attachWebSearchTool: Boolean,
    ): ResponseData {
        requireApiKey()
        val body = encodeRequest(messages, enableThinking, attachWebSearchTool)
        val httpResponse: HttpResponse = client.post {
            url(DeepSeekChatConfig.API_URL)
            header("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
            setBody(TextContent(body, ContentType.Application.Json))
        }
        val raw = httpResponse.bodyAsText()
        if (!httpResponse.status.isSuccess()) {
            throw RuntimeException("HTTP ${httpResponse.status.value}: $raw")
        }
        return json.decodeFromString(ResponseData.serializer(), raw)
    }

    private fun extractQueryArg(argumentsJson: String): String {
        return runCatching {
            val obj = json.parseToJsonElement(argumentsJson) as? JsonObject
            (obj?.get("query") as? JsonPrimitive)?.content.orEmpty()
        }.getOrDefault("").ifBlank { argumentsJson }
    }

    /**
     * @param enableThinking 标答校对等争议轮次建议 true
     * @param attachWebSearchTool 声明 search_web；若模型发起 tool_call，用骨架 stub 回填再二次请求
     */
    suspend fun chat(
        messages: List<DeepSeekChatMessage>,
        enableThinking: Boolean = false,
        attachWebSearchTool: Boolean = false,
    ): String {
        val first = postCompletion(messages, enableThinking, attachWebSearchTool)
        val msg = first.choices.firstOrNull()?.message ?: return ""
        val toolCalls = msg.toolCalls.orEmpty()
        if (toolCalls.isEmpty() || !attachWebSearchTool) {
            return stripMarkdown(msg.content.orEmpty())
        }

        val followMessages = messages.toMutableList()
        followMessages.add(
            DeepSeekChatMessage(
                role = "assistant",
                content = msg.content.orEmpty(),
            ),
        )
        toolCalls.forEach { call ->
            val query = extractQueryArg(call.function.arguments)
            val result = if (call.function.name == DeepSeekWebSearchToolSkeleton.TOOL_NAME) {
                DeepSeekWebSearchToolSkeleton.stubToolResult(query)
            } else {
                "未知工具：${call.function.name}"
            }
            followMessages.add(
                DeepSeekChatMessage(
                    role = "user",
                    content = "【工具结果 ${call.function.name}】\n$result\n请结合工具结果继续按输出格式作答。",
                ),
            )
        }
        val second = postCompletion(
            messages = followMessages,
            enableThinking = enableThinking,
            attachWebSearchTool = false,
        )
        return stripMarkdown(second.choices.firstOrNull()?.message?.content.orEmpty())
    }

    private suspend fun complete(userContent: String): String =
        chat(DeepSeekChatMessages.build(userContent))

    suspend fun analyze(question: Question): String {
        val prompt = DeepSeekExamPromptPipeline.buildAnalyzeUserContent(question)
        return complete(prompt)
    }

    suspend fun ask(text: String): String = complete(text)
}
