package com.example.testapp.data.network.ai

import com.example.testapp.data.network.deepseek.DeepSeekChatMessage
import com.example.testapp.data.repository.QuestionCorrectionParsePipeline
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.domain.model.QuestionCorrectionSource
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
        val stem = QuestionCorrectionSearchQueryPipeline.normalizeStem(request.content)
        val specs = QuestionCorrectionSearchQueryPipeline.buildSpecs(request)
        val batches = specs.map { spec ->
            runCatching {
                webSearchOrchestrator.searchSpec(spec, maxResults = SEARCH_PER_QUERY)
            }.getOrDefault(emptyList())
        }
        val sources = QuestionCorrectionSimilarityPipeline.mergeBatches(
            stem = stem,
            batches = batches,
            topN = QuestionCorrectionSimilarityPipeline.TOP_N,
        )
        val maxSim = QuestionCorrectionSimilarityPipeline.maxSimilarity(sources)
        val sameQuestionFound = maxSim >= QuestionCorrectionSimilarityPipeline.SAME_QUESTION_THRESHOLD
        val optionsBlock = request.options
            .mapIndexed { i, o -> "${'A' + i}. $o" }
            .joinToString("\n")
        val compareHint = QuestionCorrectionSimilarityPipeline.consensusHint(sources)
        val evidenceGate = if (sameQuestionFound) {
            "【证据充分】最高题干匹配度 ${(maxSim * 100).toInt()}%，请按高匹配来源中的选项/答案纠正。"
        } else {
            "【证据不足·禁止编造】最高题干匹配度仅 ${(maxSim * 100).toInt()}%（阈值 " +
                "${(QuestionCorrectionSimilarityPipeline.SAME_QUESTION_THRESHOLD * 100).toInt()}%）。" +
                "当前检索未命中同题题库页（博查/Tavily 对部分中文搜题站索引弱于百度）。" +
                "严禁根据弱相关规范/文档编造 ±1mm 等数值选项；" +
                "若原选项为占位符，options 仍输出占位原样，answer 保持原答案，confidence≤0.35，" +
                "reason 明确写「未检索到同题，请人工核对或换检索源」。"
        }
        val user = listOf(
            "题型：${request.questionType}",
            "题干：$stem",
            if (optionsBlock.isNotBlank()) "选项：\n$optionsBlock" else "选项：（无）",
            "当前答案：${request.answer.ifBlank { "（空）" }}",
            request.explanation.takeIf { it.isNotBlank() }?.let { "当前解析：$it" }.orEmpty(),
            AiWebSearchPromptPipeline.formatSources(sources),
            "客户端题干匹配摘要：$compareHint",
            evidenceGate,
            "请输出纠正后的 JSON。",
        ).filter { it.isNotBlank() }.joinToString("\n\n")

        val rawText = deepSeekDirectClient.chat(
            apiKey = deepSeekKey,
            messages = listOf(
                DeepSeekChatMessage(role = "system", content = CORRECTION_SYSTEM),
                DeepSeekChatMessage(role = "user", content = user),
            ),
            enableThinking = false,
            temperature = 0.1,
            maxTokens = 2500,
        )
        var suggestion = parseSuggestion(rawText, request)
        if (!sameQuestionFound) {
            suggestion = clampWeakEvidence(suggestion, request)
        }
        val calibrated = calibrateConfidence(suggestion.confidence, sources, sameQuestionFound)
        return suggestion.copy(
            confidence = calibrated,
            sources = sources,
            verifiedOnline = sources.isNotEmpty(),
        )
    }

    /** 弱证据时强制不「修好」占位选项，避免 ±1/±2 幻觉。 */
    private fun clampWeakEvidence(
        suggestion: QuestionCorrectionSuggestion,
        request: QuestionCorrectionRequest,
    ): QuestionCorrectionSuggestion {
        val keepOptions = if (request.options.isNotEmpty()) request.options else suggestion.options
        return suggestion.copy(
            options = keepOptions,
            answer = request.answer.ifBlank { suggestion.answer },
            reason = buildString {
                append(suggestion.reason.trim())
                if (!contains(WEAK_EVIDENCE_MARKER)) {
                    if (isNotEmpty()) append(' ')
                    append(WEAK_EVIDENCE_MARKER)
                }
            },
            confidence = minOf(suggestion.confidence, 0.35),
        )
    }

    private fun calibrateConfidence(
        modelConfidence: Double,
        sources: List<QuestionCorrectionSource>,
        sameQuestionFound: Boolean,
    ): Double {
        if (sources.isEmpty()) return minOf(modelConfidence, 0.35)
        if (!sameQuestionFound) return minOf(modelConfidence, 0.35)
        val top = sources.take(QuestionCorrectionSimilarityPipeline.COMPARE_COUNT)
        val avg = top.map { it.similarity }.average()
        val floor = when {
            avg >= 0.55 -> 0.7
            avg >= 0.45 -> 0.55
            else -> 0.4
        }
        val ceiling = when {
            avg >= 0.6 -> 0.95
            avg >= 0.45 -> 0.88
            else -> 0.75
        }
        return modelConfidence.coerceIn(floor, ceiling)
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

    private companion object {
        const val SEARCH_PER_QUERY = 8
        const val WEAK_EVIDENCE_MARKER =
            "未检索到足够相似的同题页面（博查/Tavily 索引弱于百度时常见），请人工核对。"

        const val CORRECTION_SYSTEM = """你是题库校对助手。根据题干、选项与联网检索证据，纠正错误的题干文字、选项内容或答案。
必须只输出一个 JSON 对象，不要 markdown 代码块，字段如下：
{
  "content": "纠正后的题干",
  "options": ["选项A文本","选项B文本",...],
  "answer": "正确答案（选择/判断题用字母如 B，或与选项文本一致；填空/简答写文本）",
  "explanation": "简要解析",
  "reason": "修改原因（须简述比照了哪些来源、匹配度与共识/分歧）",
  "confidence": 0.0
}
规则：
1. 不得更改题型语义以外的业务归属；不要输出题型字段。
2. 选择题/判断题：options 数量与输入一致或合理（至少 2 个），answer 必须能对应某一选项。
3. 只有摘要/标题中出现与题干高度重合的同题页时，才可采用其选项与答案；优先多源共识。
4. 若用户消息标注「证据不足·禁止编造」，严禁输出检索摘要中未出现的数值选项（尤其禁止用 ±1/±2 mm 等弱相关规范值凑数）。
5. 多源选项不一致时，在 reason 写明差异并降低 confidence。
6. 优先把 Excel 日期序列号还原为可读日期文本。
7. confidence 为 0~1；同题高匹配可 >0.8，未找到同题必须 ≤0.35。"""
    }
}
