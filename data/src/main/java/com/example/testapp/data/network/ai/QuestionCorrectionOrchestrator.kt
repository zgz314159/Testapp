package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.data.repository.QuestionCorrectionParsePipeline
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSuggestion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionCorrectionOrchestrator @Inject constructor(
    private val deepSeekDirectClient: DeepSeekDirectClient,
    private val webSearchOrchestrator: AiWebSearchOrchestrator,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun correct(
        deepSeekKey: String,
        request: QuestionCorrectionRequest,
    ): QuestionCorrectionSuggestion {
        val query = buildSearchQuery(request)
        val sources = webSearchOrchestrator.search(query, maxResults = 5)
        val optionsBlock = request.options
            .mapIndexed { i, o -> "${'A' + i}. $o" }
            .joinToString("\n")
        val user = listOf(
            "题型：${request.questionType}",
            "题干：${request.content}",
            if (optionsBlock.isNotBlank()) "选项：\n$optionsBlock" else "选项：（无）",
            "当前答案：${request.answer.ifBlank { "（空）" }}",
            request.explanation.takeIf { it.isNotBlank() }?.let { "当前解析：$it" }.orEmpty(),
            AiWebSearchPromptPipeline.formatSources(sources),
            "请输出纠正后的 JSON。",
        ).filter { it.isNotBlank() }.joinToString("\n\n")

        val rawText = deepSeekDirectClient.chat(
            apiKey = deepSeekKey,
            messages = listOf(
                DeepSeekChatMessage(role = "system", content = CORRECTION_SYSTEM),
                DeepSeekChatMessage(role = "user", content = user),
            ),
            enableThinking = false,
            temperature = 0.2,
            maxTokens = 2048,
        )
        val suggestion = parseSuggestion(rawText, request)
        return suggestion.copy(
            sources = sources,
            verifiedOnline = sources.isNotEmpty(),
        )
    }

    private fun parseSuggestion(
        rawText: String,
        request: QuestionCorrectionRequest,
    ): QuestionCorrectionSuggestion {
        val obj = extractJsonObject(rawText)
        val options = when (val opts = obj["options"]) {
            is JsonArray -> opts.map { it.jsonPrimitive.contentOrNull.orEmpty().trim() }
            else -> emptyList()
        }
        val suggestion = QuestionCorrectionSuggestion(
            content = obj.string("content"),
            options = options,
            answer = obj.string("answer"),
            explanation = obj.string("explanation"),
            reason = obj.string("reason").ifBlank { "AI 纠题建议" },
            confidence = obj["confidence"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
            sources = emptyList(),
            verifiedOnline = false,
        )
        return QuestionCorrectionParsePipeline.validate(suggestion, request)
    }

    private fun extractJsonObject(text: String): JsonObject {
        val trimmed = text.trim()
        val fenced = Regex("```(?:json)?\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
            .find(trimmed)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
        val candidate = fenced ?: trimmed
        val start = candidate.indexOf('{')
        val end = candidate.lastIndexOf('}')
        require(start >= 0 && end > start) { "模型输出无法解析为 JSON" }
        return json.parseToJsonElement(candidate.substring(start, end + 1)) as? JsonObject
            ?: error("模型输出不是 JSON 对象")
    }

    private fun JsonObject.string(key: String): String =
        (this[key] as? JsonPrimitive)?.contentOrNull?.trim().orEmpty()

    private fun buildSearchQuery(request: QuestionCorrectionRequest): String {
        val options = request.options
            .mapIndexed { i, o -> "${'A' + i}. $o" }
            .joinToString(" ")
        return "${request.content} $options ${request.answer}".trim().take(380)
    }

    private companion object {
        const val CORRECTION_SYSTEM = """你是题库校对助手。根据题干、选项与联网检索证据，纠正错误的题干文字、选项内容或答案。
必须只输出一个 JSON 对象，不要 markdown 代码块，字段如下：
{
  "content": "纠正后的题干",
  "options": ["选项A文本","选项B文本",...],
  "answer": "正确答案（选择/判断题用字母如 B，或与选项文本一致；填空/简答写文本）",
  "explanation": "简要解析",
  "reason": "修改原因",
  "confidence": 0.0
}
规则：
1. 不得更改题型语义以外的业务归属；不要输出题型字段。
2. 选择题/判断题：options 数量与输入一致或合理（至少 2 个），answer 必须能对应某一选项。
3. 若检索证据不足，仍可基于常识纠正明显错误（如 Excel 序列号日期），但 confidence 应低于 0.7。
4. 优先把 Excel 日期序列号还原为可读日期文本。
5. confidence 为 0~1 的数字。"""
    }
}
