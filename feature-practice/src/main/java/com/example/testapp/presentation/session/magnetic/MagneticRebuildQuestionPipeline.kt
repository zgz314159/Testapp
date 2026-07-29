package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel
import com.example.testapp.core.util.FillAnswerPartDescriptor
import com.example.testapp.core.util.splitFillAnswerDescriptors
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import kotlin.random.Random

object MagneticRebuildQuestionPipeline {
    private val displayBlankRegex = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")
    private val punctuationOnlyRegex = Regex("^[\\s，。、；：！？,.!?;:（）()《》“”'\"—-]+$")
    private val leadingOwnedPunctuationRegex =
        Regex("^([）)\\]】》〉」』”’〕］，。、；：！？,.!?;:]+)\\s*(.*)$")
    private val numberedItemPrefixRegex =
        Regex("^(?:[（(][一二三四五六七八九十百0-9]+[）)]|[一二三四五六七八九十百0-9]+[、.．])")
    private val numberedItemBoundaryRegex =
        Regex(
            "(?<=[，。、；：！？,.!?;:])\\s*(?=(?:[（(][一二三四五六七八九十百0-9]+[）)]|" +
                "[一二三四五六七八九十百0-9]+[、.．]))",
        )
    private val numberRegex = Regex(".*\\d.*")
    private const val MIN_CHUNKS = 3
    private const val DEFAULT_SESSION_SIZE = 20
    private const val MAX_SESSION_SIZE = 50

    fun prepare(
        sourceQuestions: List<Question>,
        requestedCount: Int,
        randomOrder: Boolean,
        seed: Long,
        fixedQuestionOrder: List<Int> = emptyList(),
        fragmentationLevel: MagneticFragmentationLevel = MagneticFragmentationLevel.STANDARD,
    ): List<MagneticClause> {
        val clauses = sourceQuestions.mapNotNull { buildClause(it, fragmentationLevel) }
        if (fixedQuestionOrder.isNotEmpty()) {
            val clausesById = clauses.associateBy(MagneticClause::sourceQuestionId)
            return fixedQuestionOrder.mapNotNull(clausesById::get)
        }
        val limit =
            (if (requestedCount <= 0) DEFAULT_SESSION_SIZE else requestedCount)
                .coerceAtMost(MAX_SESSION_SIZE)
        val ordered = if (randomOrder) clauses.shuffled(Random(seed)) else clauses
        return ordered.take(limit)
    }

    fun buildClause(
        question: Question,
        fragmentationLevel: MagneticFragmentationLevel = MagneticFragmentationLevel.STANDARD,
    ): MagneticClause? {
        if (!QuestionTypes.isInlineBlank(question.type)) return null
        val matches = displayBlankRegex.findAll(question.content).toList()
        if (matches.isEmpty()) return null
        val descriptors = splitFillAnswerDescriptors(question.answer).take(matches.size)
        if (descriptors.size != matches.size || descriptors.any { it.answerText.isBlank() }) return null

        val rawSegments = buildRawSegments(question.content, matches, descriptors)
        val merged = mergePunctuation(rawSegments)
        val balanced = balanceChunkCount(merged, fragmentationLevel.maxChunkCount)
        if (balanced.size < MIN_CHUNKS) return null

        val originalText = balanced.joinToString(separator = "")
        if (originalText.isBlank()) return null
        val tokens =
            balanced.mapIndexed { index, text ->
                MagneticToken(
                    id = question.id * 100 + index,
                    text = text,
                    order = index,
                    role = detectRole(text),
                )
            }
        return MagneticClause(
            sourceQuestionId = question.id,
            originalText = originalText,
            tokens = tokens,
        )
    }

    fun shuffledTokens(
        clause: MagneticClause,
        seed: Long,
    ): List<MagneticToken> {
        if (clause.tokens.size <= 1) return clause.tokens
        val shuffled = clause.tokens.shuffled(Random(seed))
        return if (shuffled.map { it.id } == clause.tokens.map { it.id }) {
            shuffled.drop(1) + shuffled.first()
        } else {
            shuffled
        }
    }

    private fun buildRawSegments(
        content: String,
        matches: List<MatchResult>,
        descriptors: List<FillAnswerPartDescriptor>,
    ): List<String> =
        buildList {
            var cursor = 0
            matches.forEachIndexed { index, match ->
                addIfMeaningful(content.substring(cursor, match.range.first))
                addIfMeaningful(descriptors[index].answerText)
                cursor = match.range.last + 1
            }
            addIfMeaningful(content.substring(cursor))
        }

    private fun MutableList<String>.addIfMeaningful(value: String) {
        val normalized = value.replace("\n", "").replace("\r", "").trim()
        if (normalized.isNotBlank()) add(normalized)
    }

    private fun mergePunctuation(segments: List<String>): List<String> {
        val result = mutableListOf<String>()
        var pendingAtStart = ""
        segments
            .flatMap { segment -> segment.split(numberedItemBoundaryRegex).filter(String::isNotBlank) }
            .forEach { rawSegment ->
                var segment = rawSegment
                val leadingMatch = leadingOwnedPunctuationRegex.matchEntire(segment)
                if (leadingMatch != null) {
                    val punctuation = leadingMatch.groupValues[1]
                    segment = leadingMatch.groupValues[2].trimStart()
                    if (result.isEmpty()) {
                        pendingAtStart += punctuation
                    } else {
                        result[result.lastIndex] += punctuation
                    }
                }
                if (segment.isBlank()) return@forEach
                if (punctuationOnlyRegex.matches(segment)) {
                    if (result.isEmpty()) {
                        pendingAtStart += segment
                    } else {
                        result[result.lastIndex] += segment
                    }
                } else {
                    result += pendingAtStart + segment
                    pendingAtStart = ""
                }
            }
        if (pendingAtStart.isNotEmpty() && result.isNotEmpty()) {
            result[result.lastIndex] += pendingAtStart
        }
        return result.filter(String::isNotBlank)
    }

    private fun balanceChunkCount(
        initial: List<String>,
        maxChunkCount: Int,
    ): List<String> {
        if (initial.size <= maxChunkCount) return initial
        val chunks = initial.toMutableList()
        while (chunks.size > maxChunkCount) {
            val mergeIndex =
                (0 until chunks.lastIndex)
                    .filterNot { index -> isHardBoundary(chunks[index], chunks[index + 1]) }
                    .minByOrNull { index -> mergeCost(chunks[index], chunks[index + 1]) }
                    ?: break
            chunks[mergeIndex] = chunks[mergeIndex] + chunks[mergeIndex + 1]
            chunks.removeAt(mergeIndex + 1)
        }
        return chunks
    }

    private fun isHardBoundary(
        left: String,
        right: String,
    ): Boolean =
        left.trimEnd().endsWithAny("。", "！", "？", ".", "!", "?") ||
            numberedItemPrefixRegex.containsMatchIn(right.trimStart())

    private fun mergeCost(
        left: String,
        right: String,
    ): Int {
        var cost = left.length + right.length
        cost +=
            when {
                left.endsWithAny("。", "！", "？", ".", "!", "?") -> SENTENCE_BOUNDARY_PENALTY
                left.endsWithAny("；", ";", "，", ",", "、", "：", ":") -> CLAUSE_BOUNDARY_PENALTY
                else -> 0
            }
        if (isConnectorChunk(left) || isConnectorChunk(right)) cost -= CONNECTOR_MERGE_BONUS
        if (left.length <= 2 || right.length <= 2) cost -= SHORT_CHUNK_MERGE_BONUS
        val leftRole = detectRole(left)
        val rightRole = detectRole(right)
        if (leftRole != MagneticSemanticRole.OTHER && rightRole != MagneticSemanticRole.OTHER) {
            cost += SEMANTIC_PAIR_PENALTY
        }
        return cost
    }

    private fun String.endsWithAny(vararg suffixes: String): Boolean = suffixes.any(::endsWith)

    private fun isConnectorChunk(text: String): Boolean =
        text.trim().trim('，', ',', '、', '；', ';', '：', ':') in CONNECTOR_CHUNKS

    private val CONNECTOR_CHUNKS =
        setOf("的", "及", "以及", "及其", "与", "和", "或", "在", "自", "即", "中", "为", "等")
    private const val SENTENCE_BOUNDARY_PENALTY = 1_000
    private const val CLAUSE_BOUNDARY_PENALTY = 80
    private const val SEMANTIC_PAIR_PENALTY = 24
    private const val CONNECTOR_MERGE_BONUS = 28
    private const val SHORT_CHUNK_MERGE_BONUS = 10

    private fun detectRole(text: String): MagneticSemanticRole {
        val trimmed = text.trim()
        return when {
            punctuationOnlyRegex.matches(trimmed) -> MagneticSemanticRole.PUNCTUATION
            numberRegex.matches(trimmed) -> MagneticSemanticRole.NUMBER
            trimmed in setOf("应", "必须", "严禁", "禁止", "不得", "不准", "可以", "可", "宜") ->
                MagneticSemanticRole.MODAL
            trimmed.contains("时") || trimmed.contains("前后") || trimmed.contains("以后") ||
                trimmed.contains("之前") || trimmed.contains("超过") -> MagneticSemanticRole.CONDITION
            trimmed.endsWith("人") || trimmed.endsWith("人员") || trimmed.endsWith("设备") ||
                trimmed.endsWith("电容器") -> MagneticSemanticRole.SUBJECT
            trimmed.startsWith("进行") || trimmed.startsWith("重新") || trimmed.startsWith("充分") ||
                trimmed.endsWith("放电") || trimmed.endsWith("接地") -> MagneticSemanticRole.ACTION
            trimmed.endsWith("票") || trimmed.endsWith("措施") || trimmed.endsWith("工作") -> MagneticSemanticRole.OBJECT
            else -> MagneticSemanticRole.OTHER
        }
    }
}
