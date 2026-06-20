package com.example.testapp.core.util

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import java.text.Normalizer

const val FILL_PART_DELIMITER: String = "\u001F"
private val FILL_SPLIT_REGEX = Regex("[|｜/；;，,、\\n\\r]+")
private val FILL_NUMBERED_SPLIT_REGEX = Regex("(?:^|\\s)\\d+[.、．:]\\s*")
private val FILL_BLANK_REGEX = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")
private val FILL_SCORE_SUFFIX_REGEX = Regex("^(.*)【\\s*(10|[1-9])\\s*分\\s*】\\s*$")
private val FILL_CATEGORY_SUFFIX_REGEX = Regex("^(.*)【\\s*([^【】]+?)\\s*】\\s*$")

data class FillAnswerPartDescriptor(
    val rawText: String,
    val answerText: String,
    val category: String? = null,
    val score: Int? = null
)

fun answerLetterToIndex(answer: String): Int? {
    return answer.trim().uppercase().firstOrNull()?.let { it - 'A' }
}

fun answerLettersToIndices(answer: String): List<Int> {
    return answer.trim().uppercase().filter { it in 'A'..'Z' }.map { it - 'A' }
}

private fun judgeAnswerToIndex(answer: String): Int? {
    return when (answer.trim().uppercase()) {
        "对", "正确", "√", "T", "TRUE", "YES", "Y" -> 0
        "错", "错误", "×", "F", "FALSE", "NO", "N" -> 1
        else -> null
    }
}

fun answerToOptionIndices(question: Question): List<Int> {
    return when {
        QuestionTypes.isJudge(question.type) -> judgeAnswerToIndex(question.answer)?.let(::listOf) ?: emptyList()
        else -> answerLettersToIndices(question.answer)
    }
}

fun answerToOptionIndex(question: Question): Int? {
    return answerToOptionIndices(question).firstOrNull()
}

fun resolveDisplayOptions(question: Question): List<String> {
    if (question.options.isNotEmpty()) return question.options
    return if (QuestionTypes.isJudge(question.type)) listOf("对", "错") else emptyList()
}

fun normalizeFillAnswer(text: String): String {
    val normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
        .trim()
        .lowercase()

    return buildString(normalized.length) {
        normalized.forEach { ch ->
            if (ch.isLetterOrDigit()) append(ch)
        }
    }
}

fun splitFillAnswerParts(answerText: String): List<String> {
    val trimmed = answerText.trim()
    if (trimmed.isBlank()) return emptyList()

    if (trimmed.contains(FILL_PART_DELIMITER)) {
        val storedParts = trimmed
            .split(FILL_PART_DELIMITER)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (storedParts.isNotEmpty()) return storedParts
    }

    val directParts = trimmed
        .split(FILL_SPLIT_REGEX)
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (directParts.size > 1) return directParts

    val numberedParts = trimmed
        .split(FILL_NUMBERED_SPLIT_REGEX)
        .map { it.trim() }
        .filter { it.isNotBlank() }
    if (numberedParts.size > 1) return numberedParts

    return listOf(trimmed)
}

fun parseFillAnswerPartDescriptor(rawPart: String): FillAnswerPartDescriptor {
    val trimmed = rawPart.trim()
    if (trimmed.isBlank()) {
        return FillAnswerPartDescriptor(rawText = rawPart, answerText = "")
    }

    var remaining = trimmed
    var score: Int? = null
    var category: String? = null

    while (true) {
        val scoreMatch = FILL_SCORE_SUFFIX_REGEX.matchEntire(remaining)
        if (scoreMatch != null) {
            remaining = scoreMatch.groupValues[1].trim()
            score = scoreMatch.groupValues[2].toIntOrNull() ?: score
            continue
        }

        val categoryMatch = FILL_CATEGORY_SUFFIX_REGEX.matchEntire(remaining)
        if (categoryMatch != null) {
            remaining = categoryMatch.groupValues[1].trim()
            category = categoryMatch.groupValues[2].trim().takeIf { it.isNotBlank() } ?: category
            continue
        }

        break
    }

    return FillAnswerPartDescriptor(
        rawText = rawPart,
        answerText = remaining.ifBlank { trimmed },
        category = category,
        score = score
    )
}

fun splitFillAnswerDescriptors(answerText: String): List<FillAnswerPartDescriptor> {
    return splitFillAnswerParts(answerText).map(::parseFillAnswerPartDescriptor)
}

fun resolveFillCorrectAnswerParts(question: Question): List<String> {
    if (QuestionTypes.isCalculation(question.type)) {
        return listOf(question.answer.trim()).filter { it.isNotBlank() }
    }

    if (QuestionTypes.isShort(question.type)) {
        return listOf(parseFillAnswerPartDescriptor(question.answer).answerText.trim()).filter { it.isNotBlank() }
    }

    val directParts = splitFillAnswerDescriptors(question.answer).map { it.answerText }
    val blankCount = FILL_BLANK_REGEX.findAll(question.content).count()
    if (blankCount <= 1 || directParts.size > 1) return directParts

    val legacyParts = buildList {
        addAll(question.options)
        add(question.explanation)
        add(question.answer)
    }
        .map { it.trim() }
        .filter { candidate ->
            candidate.isNotBlank() &&
                !candidate.matches(Regex("^\\d+$")) &&
                !QuestionTypes.isFill(candidate)
        }

    return if (legacyParts.size >= blankCount) legacyParts.take(blankCount) else directParts
}

fun resolveFillCorrectAnswer(question: Question): String {
    if (QuestionTypes.isCalculation(question.type)) {
        return question.answer
    }

    if (QuestionTypes.isShort(question.type)) {
        return stripLeadingAnswerPrefix(parseFillAnswerPartDescriptor(question.answer).answerText)
    }

    val parts = resolveFillCorrectAnswerParts(question)
    return when {
        parts.isEmpty() -> question.answer
        parts.size == 1 -> parts.first()
        else -> parts.joinToString(FILL_PART_DELIMITER)
    }
}

private fun stripLeadingAnswerPrefix(text: String): String {
    return text.trim()
        .removePrefix("答:")
        .removePrefix("答：")
        .trim()
}

private fun shouldTreatAsWholeTextAnswer(correctAnswer: String): Boolean {
    val trimmed = correctAnswer.trim()
    if (trimmed.isBlank() || trimmed.contains(FILL_PART_DELIMITER)) return false
    if (trimmed.startsWith("答:") || trimmed.startsWith("答：")) return true

    val punctuationCount = Regex("[，,、；;。！？!?：:]").findAll(trimmed).count()
    return trimmed.length >= 24 || (trimmed.length >= 12 && punctuationCount >= 2)
}

fun isFillAnswerCorrect(userAnswer: String, correctAnswer: String): Boolean {
    if (shouldTreatAsWholeTextAnswer(correctAnswer)) {
        val normalizedUser = normalizeFillAnswer(stripLeadingAnswerPrefix(userAnswer))
        val normalizedCorrect = normalizeFillAnswer(stripLeadingAnswerPrefix(correctAnswer))
        return normalizedUser.isNotBlank() && normalizedUser == normalizedCorrect
    }

    if (correctAnswer.contains(FILL_PART_DELIMITER) || userAnswer.contains(FILL_PART_DELIMITER)) {
        val userParts = userAnswer.split(FILL_PART_DELIMITER)
            .map { normalizeFillAnswer(it) }

        val correctParts = splitFillAnswerDescriptors(correctAnswer)
            .map { normalizeFillAnswer(it.answerText) }
            .filter { it.isNotBlank() }

        if (userParts.size != correctParts.size || correctParts.isEmpty()) return false
        return userParts.zip(correctParts).all { (user, correct) ->
            user.isNotBlank() && user == correct
        }
    }

    val normalizedUser = normalizeFillAnswer(userAnswer)
    if (normalizedUser.isBlank()) return false

    val acceptedAnswers = splitFillAnswerDescriptors(correctAnswer)
        .map { normalizeFillAnswer(it.answerText) }
        .filter { it.isNotBlank() }

    return acceptedAnswers.any { it == normalizedUser }
}

fun retainCorrectFillAnswerParts(userAnswer: String, correctAnswer: String): String {
    val correctParts = splitFillAnswerParts(correctAnswer)
    if (correctParts.isEmpty()) return ""

    val userParts = if (userAnswer.contains(FILL_PART_DELIMITER)) {
        userAnswer.split(FILL_PART_DELIMITER)
    } else {
        listOf(userAnswer)
    }

    val retained = List(correctParts.size) { index ->
        val userPart = userParts.getOrElse(index) { "" }
        val correctPart = correctParts[index]
        if (normalizeFillAnswer(userPart).isNotBlank() && normalizeFillAnswer(userPart) == normalizeFillAnswer(correctPart)) {
            userPart
        } else {
            ""
        }
    }

    return if (retained.all { it.isBlank() }) {
        ""
    } else {
        retained.joinToString(FILL_PART_DELIMITER)
    }
}

fun guessQuestionType(answer: String): String {
    val upper = answer.trim().uppercase()
    val judgeKeywords = listOf("正确", "错误", "对", "错", "√", "×", "T", "F", "TRUE", "FALSE", "YES", "NO", "Y", "N")
    if (judgeKeywords.any { upper == it }) return QuestionTypes.JUDGE
    val letters = upper.filter { it in 'A'..'Z' }
    if (letters.length > 1) return QuestionTypes.MULTI
    if (letters.length == 1) return QuestionTypes.SINGLE
    return QuestionTypes.BLANK
}
