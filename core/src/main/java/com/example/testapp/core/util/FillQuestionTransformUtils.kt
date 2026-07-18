package com.example.testapp.core.util

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import kotlin.math.min
import kotlin.random.Random

private val DISPLAY_BLANK_REGEX = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")
private const val DERIVED_FILL_QUESTION_ID_SCALE = 1000
private const val PARSED_FILL_QUESTION_CACHE_LIMIT = 512

private data class DisplayBlankMatchSnapshot(
    val range: IntRange,
    val value: String
)

private data class ParsedFillQuestionCacheKey(
    val questionId: Int,
    val content: String,
    val answer: String
)

private data class ParsedFillQuestionSnapshot(
    val matches: List<DisplayBlankMatchSnapshot>,
    val descriptors: List<FillAnswerPartDescriptor>,
    val correctParts: List<String>,
    val answerableBlankCount: Int
)

private val parsedFillQuestionCache = object : LinkedHashMap<ParsedFillQuestionCacheKey, ParsedFillQuestionSnapshot>(
    PARSED_FILL_QUESTION_CACHE_LIMIT,
    0.75f,
    true
) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<ParsedFillQuestionCacheKey, ParsedFillQuestionSnapshot>?): Boolean {
        return size > PARSED_FILL_QUESTION_CACHE_LIMIT
    }
}

enum class FillQuestionGenerationMode(val storageValue: String) {
    SCORE_DESC("score_desc"),
    SCORE_ASC("score_asc"),
    TAG_RANDOM("tag_random"),
    SCORE_RANGE_RANDOM("score_range_random"),
    FULL_ANSWER("full_answer");

    /**
     * 模式与筛选条件的互锁策略（唯一裁决点）：
     * 标签筛选只在「标签随机模式」参与出题，其余模式忽略，
     * 防止切换模式后残留标签造成串扰。
     */
    val usesTagFilter: Boolean
        get() = this == TAG_RANDOM

    /** 分值范围只在「分值范围随机 / 全答」两个模式参与出题。 */
    val usesScoreRange: Boolean
        get() = this == SCORE_RANGE_RANDOM || this == FULL_ANSWER

    companion object {
        fun fromStorageValue(value: String?): FillQuestionGenerationMode {
            return entries.firstOrNull { it.storageValue == value } ?: SCORE_RANGE_RANDOM
        }
    }
}

data class FillQuestionFilterSummary(
    val dynamicQuestionCount: Int = 0,
    val eligibleQuestionCount: Int = 0,
    val filteredQuestionCount: Int = 0
)

fun transformQuestionForFillSettings(
    question: Question,
    maxVisibleBlanks: Int,
    generationMode: FillQuestionGenerationMode,
    fullAnswerRandomOrder: Boolean,
    minAnswerScore: Int,
    maxAnswerScore: Int,
    answerTagFilter: String,
    seed: Long
): Question? {
    return transformQuestionVariantsForFillSettings(
        question = question,
        maxVisibleBlanks = maxVisibleBlanks,
        generationMode = generationMode,
        fullAnswerRandomOrder = fullAnswerRandomOrder,
        minAnswerScore = minAnswerScore,
        maxAnswerScore = maxAnswerScore,
        answerTagFilter = answerTagFilter,
        seed = seed
    ).firstOrNull()
}

fun transformQuestionVariantsForFillSettings(
    question: Question,
    maxVisibleBlanks: Int,
    generationMode: FillQuestionGenerationMode,
    fullAnswerRandomOrder: Boolean,
    minAnswerScore: Int,
    maxAnswerScore: Int,
    answerTagFilter: String,
    seed: Long
): List<Question> {
    if (!QuestionTypes.isInlineBlank(question.type)) return listOf(question)

    val parsedQuestion = getParsedFillQuestionSnapshot(question)
    val matches = parsedQuestion.matches
    if (matches.isEmpty()) return listOf(question)

    val descriptors = parsedQuestion.descriptors
    val correctParts = parsedQuestion.correctParts
    val answerableBlankCount = parsedQuestion.answerableBlankCount
    if (answerableBlankCount <= 0) return listOf(question)

    val safeMinScore = minAnswerScore.coerceIn(1, 10)
    val safeMaxScore = maxAnswerScore.coerceIn(safeMinScore, 10)
    val requiredTagTokens = if (generationMode.usesTagFilter) {
        AnswerTagFilterCodec.decode(answerTagFilter)
    } else {
        emptyList()
    }
    val scoredBlankPresent = descriptors.take(answerableBlankCount).any { it.score != null }
    val tagFilterPresent = requiredTagTokens.isNotEmpty()
    val eligibleBlankIndices = (0 until answerableBlankCount).filter { index ->
        val descriptor = descriptors.getOrNull(index)
        val tagAllowed = !tagFilterPresent || requiredTagTokens.any { token ->
            descriptor?.category?.contains(token, ignoreCase = true) == true
        }
        val scoreAllowed = when (generationMode) {
            FillQuestionGenerationMode.SCORE_DESC -> true
            FillQuestionGenerationMode.SCORE_ASC -> true
            FillQuestionGenerationMode.TAG_RANDOM -> true
            FillQuestionGenerationMode.SCORE_RANGE_RANDOM -> {
                if (!scoredBlankPresent) {
                    true
                } else {
                    descriptor?.score in safeMinScore..safeMaxScore
                }
            }
            FillQuestionGenerationMode.FULL_ANSWER -> {
                if (!scoredBlankPresent) {
                    true
                } else {
                    descriptor?.score in safeMinScore..safeMaxScore
                }
            }
        }
        scoreAllowed && tagAllowed
    }

    if (eligibleBlankIndices.isEmpty()) {
        return if (scoredBlankPresent || tagFilterPresent) emptyList() else listOf(question)
    }

    if (
        generationMode == FillQuestionGenerationMode.SCORE_RANGE_RANDOM &&
        eligibleBlankIndices.size == answerableBlankCount &&
        (maxVisibleBlanks <= 0 || maxVisibleBlanks >= answerableBlankCount)
    ) {
        return listOf(question)
    }

    val targetVisibleBlankCount = when {
        maxVisibleBlanks <= 0 -> eligibleBlankIndices.size
        else -> maxVisibleBlanks.coerceIn(1, eligibleBlankIndices.size)
    }
    val visibleIndexGroups = when (generationMode) {
        FillQuestionGenerationMode.FULL_ANSWER -> buildFullAnswerVisibleIndexGroups(
            eligibleIndices = eligibleBlankIndices,
            count = targetVisibleBlankCount,
            random = Random(seed),
            randomOrder = fullAnswerRandomOrder
        )
        else -> listOf(
            when (generationMode) {
                FillQuestionGenerationMode.SCORE_DESC -> eligibleBlankIndices
                    .sortedWith(compareByDescending<Int> { descriptors.getOrNull(it)?.score ?: 0 }.thenBy { it })
                    .take(targetVisibleBlankCount)
                    .sorted()
                FillQuestionGenerationMode.SCORE_ASC -> eligibleBlankIndices
                    .sortedWith(compareBy<Int> { descriptors.getOrNull(it)?.score ?: Int.MAX_VALUE }.thenBy { it })
                    .take(targetVisibleBlankCount)
                    .sorted()
                FillQuestionGenerationMode.TAG_RANDOM,
                FillQuestionGenerationMode.SCORE_RANGE_RANDOM -> pickRandomBlankIndices(
                    eligibleIndices = eligibleBlankIndices,
                    count = targetVisibleBlankCount,
                    random = Random(seed)
                ).sorted()
                FillQuestionGenerationMode.FULL_ANSWER -> emptyList()
            }
        )
    }

    return visibleIndexGroups.mapIndexedNotNull { variantIndex, visibleIndices ->
        buildConfiguredFillQuestion(
            sourceQuestion = question,
            matches = matches,
            descriptors = descriptors,
            correctParts = correctParts,
            answerableBlankCount = answerableBlankCount,
            visibleIndices = visibleIndices,
            variantQuestionId = if (visibleIndexGroups.size <= 1) question.id else buildDerivedFillQuestionId(question.id, variantIndex)
        )
    }
}

fun buildDerivedFillQuestionId(sourceQuestionId: Int, variantIndex: Int): Int {
    return -((sourceQuestionId.coerceAtLeast(0) * DERIVED_FILL_QUESTION_ID_SCALE) + variantIndex + 1)
}

fun extractSourceQuestionId(questionId: Int): Int {
    if (questionId >= 0) return questionId
    return ((-questionId) - 1) / DERIVED_FILL_QUESTION_ID_SCALE
}

fun extractDerivedFillQuestionRound(questionId: Int): Int? {
    if (questionId >= 0) return null
    return ((-questionId) - 1) % DERIVED_FILL_QUESTION_ID_SCALE + 1
}

private fun buildConfiguredFillQuestion(
    sourceQuestion: Question,
    matches: List<DisplayBlankMatchSnapshot>,
    descriptors: List<FillAnswerPartDescriptor>,
    correctParts: List<String>,
    answerableBlankCount: Int,
    visibleIndices: List<Int>,
    variantQuestionId: Int
): Question? {
    if (visibleIndices.isEmpty()) return null

    val visibleIndexSet = visibleIndices.toSet()
    val rebuiltContent = buildString {
        var cursor = 0
        matches.forEachIndexed { index, match ->
            append(sourceQuestion.content.substring(cursor, match.range.first))
            append(
                when {
                    index >= answerableBlankCount -> match.value
                    index in visibleIndexSet -> match.value
                    else -> correctParts[index]
                }
            )
            cursor = match.range.last + 1
        }
        append(sourceQuestion.content.substring(cursor))
    }

    val visibleAnswers = visibleIndices.mapNotNull {
        descriptors.getOrNull(it)?.answerText?.trim()?.takeIf(String::isNotBlank)
    }
    if (visibleAnswers.isEmpty()) return null

    val rebuiltAnswer = if (visibleAnswers.size == 1) {
        visibleAnswers.first()
    } else {
        visibleAnswers.joinToString(FILL_PART_DELIMITER)
    }

    return sourceQuestion.copy(
        id = variantQuestionId,
        content = rebuiltContent,
        answer = rebuiltAnswer
    )
}

private fun pickRandomBlankIndices(
    eligibleIndices: List<Int>,
    count: Int,
    random: Random
): List<Int> {
    if (count <= 0 || eligibleIndices.isEmpty()) return emptyList()
    if (count >= eligibleIndices.size) return eligibleIndices

    val remaining = eligibleIndices.toMutableList()
    val selected = mutableListOf<Int>()

    repeat(count.coerceAtMost(remaining.size)) {
        val chosenIndex = remaining[random.nextInt(remaining.size)]
        selected += chosenIndex
        remaining.remove(chosenIndex)
    }

    return selected
}

private fun buildFullAnswerVisibleIndexGroups(
    eligibleIndices: List<Int>,
    count: Int,
    random: Random,
    randomOrder: Boolean
): List<List<Int>> {
    if (eligibleIndices.isEmpty()) return emptyList()
    if (count <= 0 || count >= eligibleIndices.size) return listOf(eligibleIndices.sorted())

    val orderedIndices = if (randomOrder) {
        eligibleIndices.shuffled(random)
    } else {
        eligibleIndices.sorted()
    }

    return orderedIndices
        .chunked(count)
        .map { it.sorted() }
        .filter { it.isNotEmpty() }
}

fun buildFillQuestionFilterSummary(
    questions: List<Question>,
    maxVisibleBlanks: Int,
    generationMode: FillQuestionGenerationMode,
    fullAnswerRandomOrder: Boolean,
    minAnswerScore: Int,
    maxAnswerScore: Int,
    answerTagFilter: String
): FillQuestionFilterSummary {
    val dynamicQuestions = questions.filter { QuestionTypes.isInlineBlank(it.type) }
    if (dynamicQuestions.isEmpty()) return FillQuestionFilterSummary()

    val eligibleCount = dynamicQuestions.countIndexed { index, question ->
        transformQuestionVariantsForFillSettings(
            question = question,
            maxVisibleBlanks = maxVisibleBlanks,
            generationMode = generationMode,
            fullAnswerRandomOrder = fullAnswerRandomOrder,
            minAnswerScore = minAnswerScore,
            maxAnswerScore = maxAnswerScore,
            answerTagFilter = answerTagFilter,
            seed = question.id.toLong() + index
        ).isNotEmpty()
    }

    return FillQuestionFilterSummary(
        dynamicQuestionCount = dynamicQuestions.size,
        eligibleQuestionCount = eligibleCount,
        filteredQuestionCount = (dynamicQuestions.size - eligibleCount).coerceAtLeast(0)
    )
}

private inline fun <T> Iterable<T>.countIndexed(predicate: (Int, T) -> Boolean): Int {
    var count = 0
    forEachIndexed { index, item ->
        if (predicate(index, item)) count += 1
    }
    return count
}

private fun getParsedFillQuestionSnapshot(question: Question): ParsedFillQuestionSnapshot {
    val cacheKey = ParsedFillQuestionCacheKey(
        questionId = question.id,
        content = question.content,
        answer = question.answer
    )
    synchronized(parsedFillQuestionCache) {
        parsedFillQuestionCache[cacheKey]?.let { return it }
    }

    val matches = DISPLAY_BLANK_REGEX.findAll(question.content)
        .map { match -> DisplayBlankMatchSnapshot(range = match.range, value = match.value) }
        .toList()
    val descriptors = splitFillAnswerDescriptors(question.answer)
    val correctParts = descriptors.map { it.answerText }
    val snapshot = ParsedFillQuestionSnapshot(
        matches = matches,
        descriptors = descriptors,
        correctParts = correctParts,
        answerableBlankCount = min(matches.size, correctParts.size)
    )

    synchronized(parsedFillQuestionCache) {
        parsedFillQuestionCache[cacheKey] = snapshot
    }
    return snapshot
}

