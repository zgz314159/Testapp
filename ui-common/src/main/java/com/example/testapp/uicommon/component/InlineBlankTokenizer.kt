package com.example.testapp.uicommon.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.testapp.core.util.FILL_PART_DELIMITER
import com.example.testapp.core.util.normalizeFillAnswer
import com.example.testapp.core.util.splitFillAnswerParts

private val FILL_PART_REGEX = Regex("[|｜/；;，,、]")
private val FILL_ACCEPTED_VARIANT_REGEX = Regex("[|｜/／；;\\n\\r]+")
val CorrectGreen = Color(0xFF16A34A)
val EditingBlue = Color(0xFF2563EB)
const val DEFAULT_INLINE_BLANK_CHARS = 6
const val MIN_INLINE_BLANK_CHARS = 4
const val BLANK_PLACEHOLDER_CHAR = '\u00A0'
const val LINE_BREAK_OPPORTUNITY_CHAR = '\u200B'

// ---- Editor spec & transformation ----

fun resolveEmptyBlankWidthChars(blankToken: String): Int {
    val underscoreLength = blankToken.count { it == '_' }
    return underscoreLength.coerceAtLeast(DEFAULT_INLINE_BLANK_CHARS).coerceAtLeast(MIN_INLINE_BLANK_CHARS)
}

data class InlineBlankEditorSpec(
    val prefixes: List<String>,
    val blankWidths: List<Int>
) {
    val blankCount: Int get() = blankWidths.size
}

class InlineBlankVisualTransformation(
    private val editorSpec: InlineBlankEditorSpec
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return buildInlineBlankTransformedText(
            rawText = text.text,
            editorSpec = editorSpec
        )
    }
}

fun buildInlineBlankEditorSpec(
    content: String,
    matches: List<MatchResult>
): InlineBlankEditorSpec {
    val prefixes = mutableListOf<String>()
    val blankWidths = mutableListOf<Int>()
    var cursor = 0
    matches.forEach { match ->
        prefixes += content.substring(cursor, match.range.first)
        blankWidths += resolveEmptyBlankWidthChars(match.value)
        cursor = match.range.last + 1
    }
    prefixes += content.substring(cursor)
    return InlineBlankEditorSpec(prefixes = prefixes, blankWidths = blankWidths)
}

fun buildInlineEditorRawText(parts: List<String>, blankCount: Int): String {
    val normalizedParts = List(blankCount) { index -> parts.getOrElse(index) { "" } }
    return if (blankCount <= 1) {
        normalizedParts.firstOrNull().orEmpty()
    } else {
        normalizedParts.joinToString(FILL_PART_DELIMITER)
    }
}

fun splitInlineEditorRawText(rawText: String, blankCount: Int): List<String> {
    if (blankCount <= 1) return listOf(rawText)
    val parsed = rawText.split(FILL_PART_DELIMITER, ignoreCase = false, limit = blankCount)
    return List(blankCount) { index -> parsed.getOrElse(index) { "" } }
}

fun defaultInlineEditorSelection(rawText: String, blankCount: Int): TextRange {
    val cursor = if (blankCount > 1 && splitInlineEditorRawText(rawText, blankCount).all { it.isBlank() }) {
        0
    } else {
        rawText.length
    }
    return TextRange(cursor)
}

fun normalizeInlineEditorValue(
    candidate: TextFieldValue,
    previous: TextFieldValue,
    blankCount: Int
): TextFieldValue {
    val cleanedText = candidate.text.replace("\r", "").replace("\n", "")

    if (blankCount <= 1) {
        val normalizedSelection = clampTextRange(candidate.selection, cleanedText.length)
        val normalizedComposition = clampCompositionRange(candidate.composition, cleanedText)
        return candidate.copy(
            text = cleanedText,
            selection = normalizedSelection,
            composition = normalizedComposition
        )
    }

    if (cleanedText.count { it.toString() == FILL_PART_DELIMITER } != blankCount - 1) {
        return previous
    }

    val normalizedSelection = clampTextRange(candidate.selection, cleanedText.length)
    val normalizedComposition = clampCompositionRange(candidate.composition, cleanedText)
    return candidate.copy(
        text = cleanedText,
        selection = normalizedSelection,
        composition = normalizedComposition
    )
}

fun clampTextRange(range: TextRange, textLength: Int): TextRange {
    return TextRange(
        start = range.start.coerceIn(0, textLength),
        end = range.end.coerceIn(0, textLength)
    )
}

fun clampCompositionRange(range: TextRange?, text: String): TextRange? {
    if (range == null) return null
    val clamped = clampTextRange(range, text.length)
    if (clamped.collapsed) return null
    if (text.substring(clamped.start, clamped.end).contains(FILL_PART_DELIMITER)) return null
    return clamped
}

fun buildInlineBlankTransformedText(
    rawText: String,
    editorSpec: InlineBlankEditorSpec
): TransformedText {
    val rawParts = splitInlineEditorRawText(rawText, editorSpec.blankCount)
    val normalizedRawText = buildInlineEditorRawText(rawParts, editorSpec.blankCount)
    val rawToTransformed = IntArray(normalizedRawText.length + 1)
    val transformedToRaw = mutableListOf(0)
    val transformedText = buildAnnotatedString {
        var rawOffset = 0
        var transformedOffset = 0

        fun appendFixedSegment(value: String) {
            value.forEach { character ->
                append(character)
                transformedOffset += 1
                transformedToRaw += rawOffset
            }
        }

        fun appendLineBreakOpportunity() {
            append(LINE_BREAK_OPPORTUNITY_CHAR)
            transformedOffset += 1
            transformedToRaw += rawOffset
        }

        appendFixedSegment(editorSpec.prefixes.firstOrNull().orEmpty())

        repeat(editorSpec.blankCount) { blankIndex ->
            val answer = rawParts.getOrElse(blankIndex) { "" }
            val blankWidth = editorSpec.blankWidths[blankIndex]
            val blankVisualWidth = maxOf(blankWidth, answer.length)
            val leadingPadding = ((blankVisualWidth - answer.length).coerceAtLeast(0)) / 2
            val trailingPadding = (blankVisualWidth - answer.length - leadingPadding).coerceAtLeast(0)

            appendLineBreakOpportunity()

            withStyle(
                SpanStyle(
                    color = EditingBlue,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                repeat(leadingPadding) {
                    append(BLANK_PLACEHOLDER_CHAR)
                    transformedOffset += 1
                    transformedToRaw += rawOffset
                }
                rawToTransformed[rawOffset] = transformedOffset

                answer.forEach { character ->
                    rawToTransformed[rawOffset] = transformedOffset
                    append(if (character == ' ') BLANK_PLACEHOLDER_CHAR else character)
                    rawOffset += 1
                    transformedOffset += 1
                    transformedToRaw += rawOffset
                }
                rawToTransformed[rawOffset] = transformedOffset

                repeat(trailingPadding) {
                    append(BLANK_PLACEHOLDER_CHAR)
                    transformedOffset += 1
                    transformedToRaw += rawOffset
                }
            }

            appendLineBreakOpportunity()

            val nextPrefix = editorSpec.prefixes[blankIndex + 1]
            if (blankIndex < editorSpec.blankCount - 1) {
                rawOffset += 1
                rawToTransformed[rawOffset] = transformedOffset
                if (nextPrefix.isEmpty()) {
                    append(' ')
                    transformedOffset += 1
                    transformedToRaw += rawOffset
                }
            }

            appendFixedSegment(nextPrefix)
        }
    }

    return TransformedText(
        text = transformedText,
        offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return rawToTransformed[offset.coerceIn(0, rawToTransformed.lastIndex)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transformedToRaw[offset.coerceIn(0, transformedToRaw.lastIndex)]
            }
        }
    )
}

// ---- Answer encoding / decoding ----

fun decodeUserAnswers(answerText: String, blankCount: Int): List<String> {
    if (blankCount <= 1) return listOf(answerText)

    val parsed = when {
        answerText.contains(FILL_PART_DELIMITER) -> answerText.split(FILL_PART_DELIMITER)
        answerText.contains(FILL_PART_REGEX) -> answerText.split(FILL_PART_REGEX)
        else -> listOf(answerText)
    }

    return List(blankCount) { index -> parsed.getOrElse(index) { "" } }
}

fun decodeCorrectAnswers(correctAnswer: String, blankCount: Int): List<String> {
    if (blankCount <= 1) return listOf(correctAnswer)
    val parsed = splitFillAnswerParts(correctAnswer)
    return List(blankCount) { index -> parsed.getOrElse(index) { "" } }
}

fun encodeUserAnswers(parts: List<String>): String {
    if (parts.all { it.isBlank() }) return ""
    return parts.joinToString(FILL_PART_DELIMITER)
}

fun matchesDisplayedFillAnswer(userAnswer: String, correctAnswer: String): Boolean {
    val normalizedUser = normalizeFillAnswer(userAnswer)
    if (normalizedUser.isBlank()) return false

    val normalizedWholeAnswer = normalizeFillAnswer(correctAnswer)
    if (normalizedWholeAnswer.isNotBlank() && normalizedWholeAnswer == normalizedUser) {
        return true
    }

    return splitAcceptedFillVariants(correctAnswer)
        .map(::normalizeFillAnswer)
        .any { it == normalizedUser }
}

fun splitAcceptedFillVariants(answerText: String): List<String> {
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
        .split(FILL_ACCEPTED_VARIANT_REGEX)
        .map { it.trim() }
        .filter { it.isNotBlank() }

    return if (directParts.size > 1) directParts else listOf(trimmed)
}

// ---- Result rendering ----

fun buildResultQuestionAnnotatedString(
    content: String,
    matches: List<MatchResult>,
    userParts: List<String>,
    correctParts: List<String>,
    questionFontSize: Float,
    errorColor: Color
) = buildAnnotatedString {
    var cursor = 0
    matches.forEachIndexed { index, match ->
        append(content.substring(cursor, match.range.first))
        append(LINE_BREAK_OPPORTUNITY_CHAR)

        val userPart = userParts.getOrElse(index) { "" }
        val correctPart = correctParts.getOrElse(index) { "" }
        appendResultBlankText(
            userAnswer = userPart,
            correctAnswer = correctPart,
            questionFontSize = questionFontSize,
            errorColor = errorColor
        )
        append(LINE_BREAK_OPPORTUNITY_CHAR)

        cursor = match.range.last + 1
    }
    append(content.substring(cursor))
}

@JvmName("appendResultBlankText")
fun AnnotatedString.Builder.appendResultBlankText(
    userAnswer: String,
    correctAnswer: String,
    questionFontSize: Float,
    errorColor: Color
) {
    val normalizedUser = normalizeFillAnswer(userAnswer)
    val isCorrect = matchesDisplayedFillAnswer(userAnswer, correctAnswer)
    val resultColor = if (isCorrect) CorrectGreen else errorColor

    when {
        normalizedUser.isBlank() -> {
            withStyle(
                SpanStyle(
                    color = resultColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(correctAnswer)
            }
        }

        isCorrect -> {
            withStyle(
                SpanStyle(
                    color = resultColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(userAnswer)
            }
        }

        else -> {
            withStyle(
                SpanStyle(
                    color = resultColor,
                    textDecoration = TextDecoration.combine(
                        listOf(TextDecoration.Underline, TextDecoration.LineThrough)
                    )
                )
            ) {
                append(userAnswer)
            }
            withStyle(
                SpanStyle(
                    color = resultColor,
                    textDecoration = TextDecoration.Underline,
                    fontSize = (questionFontSize - 2f).coerceAtLeast(10f).sp
                )
            ) {
                append("（$correctAnswer）")
            }
        }
    }
}
