package com.example.testapp.uicommon.util

import com.example.testapp.core.util.FILL_PART_DELIMITER
import com.example.testapp.core.util.normalizeFillAnswer
import com.example.testapp.core.util.splitFillAnswerDescriptors
import com.example.testapp.core.util.splitFillAnswerParts

private val BLANK_REGEX = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")

data class FillAnswerDisplayPart(
    val label: String,
    val value: String,
    val appendedCorrectValue: String? = null,
    val isCorrect: Boolean
)

fun formatFillCorrectAnswerDisplay(content: String, correctAnswer: String): String {
    val blankCount = BLANK_REGEX.findAll(content).count()
    val parts = extractDisplayableFillAnswerParts(correctAnswer)

    if (blankCount <= 1 || parts.size <= 1) {
        return parts.firstOrNull().orEmpty()
    }

    val size = maxOf(blankCount, parts.size)
    return (0 until size)
        .joinToString("；") { index ->
            val value = parts.getOrElse(index) { "" }
            "第${index + 1}空：$value"
        }
}

fun buildFillAnswerDisplayParts(
    content: String,
    correctAnswer: String,
    userAnswer: String
): List<FillAnswerDisplayPart> {
    val blankCount = BLANK_REGEX.findAll(content).count()
    val correctParts = extractDisplayableFillAnswerParts(correctAnswer)
    val userParts = when {
        userAnswer.contains(FILL_PART_DELIMITER) -> userAnswer.split(FILL_PART_DELIMITER)
        else -> splitFillAnswerParts(userAnswer)
    }

    if (blankCount <= 1 || correctParts.size <= 1) {
        val correctValue = correctParts.firstOrNull().orEmpty()
        val userValue = userParts.firstOrNull().orEmpty()
        val normalizedUser = normalizeFillAnswer(userValue)
        val matches = normalizedUser.isNotBlank() && normalizedUser == normalizeFillAnswer(correctValue)
        return listOf(
            FillAnswerDisplayPart(
                label = "",
                value = correctValue,
                appendedCorrectValue = null,
                isCorrect = matches
            )
        )
    }

    val size = maxOf(blankCount, correctParts.size)
    return (0 until size).map { index ->
        val correctValue = correctParts.getOrElse(index) { "" }
        val userValue = userParts.getOrElse(index) { "" }
        val normalizedUser = normalizeFillAnswer(userValue)
        val isCorrect = normalizedUser.isNotBlank() &&
            normalizedUser == normalizeFillAnswer(correctValue)
        FillAnswerDisplayPart(
            label = "第${index + 1}空：",
            value = correctValue,
            appendedCorrectValue = null,
            isCorrect = isCorrect
        )
    }
}

private fun extractDisplayableFillAnswerParts(correctAnswer: String): List<String> {
    return splitFillAnswerDescriptors(correctAnswer)
        .map { it.answerText.trim() }
        .filter { it.isNotBlank() }
        .ifEmpty { splitFillAnswerParts(correctAnswer).map { it.trim() }.filter { it.isNotBlank() } }
}
