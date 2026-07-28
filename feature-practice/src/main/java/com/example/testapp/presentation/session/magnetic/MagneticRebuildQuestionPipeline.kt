package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.util.FillAnswerPartDescriptor
import com.example.testapp.core.util.splitFillAnswerDescriptors
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import kotlin.random.Random

object MagneticRebuildQuestionPipeline {
    private val displayBlankRegex = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")
    private val punctuationOnlyRegex = Regex("^[\\s，。、；：！？,.!?;:（）()《》“”'\"—-]+$")
    private val numberRegex = Regex(".*\\d.*")
    private const val MIN_CHUNKS = 3
    private const val MAX_CHUNKS = 12
    private const val DEFAULT_SESSION_SIZE = 20
    private const val MAX_SESSION_SIZE = 50

    fun prepare(
        sourceQuestions: List<Question>,
        requestedCount: Int,
        randomOrder: Boolean,
        seed: Long,
    ): List<MagneticClause> {
        val limit =
            (if (requestedCount <= 0) DEFAULT_SESSION_SIZE else requestedCount)
                .coerceAtMost(MAX_SESSION_SIZE)
        val clauses = sourceQuestions.mapNotNull(::buildClause)
        val ordered = if (randomOrder) clauses.shuffled(Random(seed)) else clauses
        return ordered.take(limit)
    }

    fun buildClause(question: Question): MagneticClause? {
        if (!QuestionTypes.isInlineBlank(question.type)) return null
        val matches = displayBlankRegex.findAll(question.content).toList()
        if (matches.isEmpty()) return null
        val descriptors = splitFillAnswerDescriptors(question.answer).take(matches.size)
        if (descriptors.size != matches.size || descriptors.any { it.answerText.isBlank() }) return null

        val rawSegments = buildRawSegments(question.content, matches, descriptors)
        val merged = mergePunctuation(rawSegments)
        val balanced = balanceChunkCount(merged)
        if (balanced.size !in MIN_CHUNKS..MAX_CHUNKS) return null

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
        var leadingPunctuation = ""
        segments.forEach { segment ->
            if (punctuationOnlyRegex.matches(segment)) {
                if (result.isEmpty()) {
                    leadingPunctuation += segment
                } else {
                    result[result.lastIndex] += segment
                }
            } else {
                result += leadingPunctuation + segment
                leadingPunctuation = ""
            }
        }
        if (leadingPunctuation.isNotEmpty() && result.isNotEmpty()) {
            result[result.lastIndex] += leadingPunctuation
        }
        return result.filter(String::isNotBlank)
    }

    private fun balanceChunkCount(initial: List<String>): List<String> {
        if (initial.size <= MAX_CHUNKS) return initial
        val chunks = initial.toMutableList()
        while (chunks.size > MAX_CHUNKS) {
            val mergeIndex =
                (0 until chunks.lastIndex).minByOrNull { index ->
                    chunks[index].length + chunks[index + 1].length
                } ?: break
            chunks[mergeIndex] = chunks[mergeIndex] + chunks[mergeIndex + 1]
            chunks.removeAt(mergeIndex + 1)
        }
        return chunks
    }

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
