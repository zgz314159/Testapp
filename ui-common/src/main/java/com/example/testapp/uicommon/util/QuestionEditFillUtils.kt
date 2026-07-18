package com.example.testapp.uicommon.util

import com.example.testapp.core.util.FILL_PART_DELIMITER
import com.example.testapp.core.util.parseFillAnswerPartDescriptor
import com.example.testapp.core.util.splitFillAnswerParts

private val EDITABLE_BLANK_REGEX = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")
private val PASTED_NUMBERED_LINE_REGEX = Regex("^\\s*(\\d+)[.、．:：]\\s*(.+?)\\s*$")
private const val DEFAULT_EDITABLE_BLANK = "____"
/** 选择题题干空位，与常见卷面 `( )` 一致。 */
private const val DEFAULT_CHOICE_BLANK = "( )"

data class EditableBlankInsertion(
    val content: String,
    val blankIndex: Int,
    val cursorPosition: Int
)

fun countEditableFillBlanks(content: String): Int {
    return EDITABLE_BLANK_REGEX.findAll(content).count()
}

fun normalizeEditableFillAnswers(content: String, answer: String): List<String> {
    val parts = splitFillAnswerParts(answer)
    val targetCount = maxOf(countEditableFillBlanks(content), parts.size, 1)
    return syncEditableFillAnswers(parts, targetCount)
}

fun syncEditableFillAnswers(answerParts: List<String>, blankCount: Int): List<String> {
    val targetCount = blankCount.coerceAtLeast(1)
    return List(targetCount) { index -> answerParts.getOrElse(index) { "" } }
}

fun appendEditableBlank(content: String): String {
    val suffix = if (content.isBlank() || content.last().isWhitespace()) "" else " "
    return content + suffix + DEFAULT_EDITABLE_BLANK
}

fun insertEditableBlankAtCursor(content: String, cursor: Int): EditableBlankInsertion =
    insertMarkerAtCursor(content, cursor, DEFAULT_EDITABLE_BLANK)

/** 在光标处插入选择题空位 `( )`，并返回插入后内容与光标位置。 */
fun insertEditableChoiceBlankAtCursor(content: String, cursor: Int): EditableBlankInsertion =
    insertMarkerAtCursor(content, cursor, DEFAULT_CHOICE_BLANK)

private fun insertMarkerAtCursor(
    content: String,
    cursor: Int,
    marker: String,
): EditableBlankInsertion {
    val safeCursor = cursor.coerceIn(0, content.length)
    val leadingSpace = safeCursor > 0 && !content[safeCursor - 1].isWhitespace()
    val trailingSpace = safeCursor < content.length && !content[safeCursor].isWhitespace()
    val insertionText = buildString {
        if (leadingSpace) append(' ')
        append(marker)
        if (trailingSpace) append(' ')
    }
    val blankIndex = EDITABLE_BLANK_REGEX.findAll(content.substring(0, safeCursor)).count()
    val newContent = content.substring(0, safeCursor) + insertionText + content.substring(safeCursor)
    val cursorPosition = safeCursor + (if (leadingSpace) 1 else 0) + marker.length
    return EditableBlankInsertion(
        content = newContent,
        blankIndex = blankIndex,
        cursorPosition = cursorPosition,
    )
}

fun removeLastEditableBlank(content: String): String {
    val matches = EDITABLE_BLANK_REGEX.findAll(content).toList()
    if (matches.size <= 1) return content

    val lastMatch = matches.last()
    val before = content.substring(0, lastMatch.range.first).trimEnd()
    val after = content.substring(lastMatch.range.last + 1).trimStart()

    return buildString {
        append(before)
        if (before.isNotBlank() && after.isNotBlank()) append(' ')
        append(after)
    }
}

fun removeEditableBlankAt(content: String, blankIndex: Int): String {
    val matches = EDITABLE_BLANK_REGEX.findAll(content).toList()
    if (matches.size <= 1) return content

    val safeIndex = blankIndex.coerceIn(0, matches.lastIndex)
    val targetMatch = matches[safeIndex]
    val before = content.substring(0, targetMatch.range.first).trimEnd()
    val after = content.substring(targetMatch.range.last + 1).trimStart()

    return buildString {
        append(before)
        if (before.isNotBlank() && after.isNotBlank()) append(' ')
        append(after)
    }
}

fun buildEditableFillAnswer(answerParts: List<String>): String {
    return answerParts.joinToString(FILL_PART_DELIMITER)
}

/** 单空答案的编辑视图字段：答案正文 / 属性标签 / 分值，均不含「【】」。 */
data class EditableFillAnswerFields(
    val answerText: String,
    val tag: String,
    val score: String,
)

/** 把存储串 `答案【标签】【N分】` 拆成三个可编辑字段。 */
fun parseEditableFillAnswerFields(rawPart: String): EditableFillAnswerFields {
    if (rawPart.isBlank()) return EditableFillAnswerFields("", "", "")
    val descriptor = parseFillAnswerPartDescriptor(rawPart)
    return EditableFillAnswerFields(
        answerText = descriptor.answerText,
        tag = descriptor.category.orEmpty(),
        score = descriptor.score?.toString().orEmpty(),
    )
}

/**
 * 把三个字段拼回存储串。答案为空时整体视为空（避免残留孤立后缀）；
 * 分值按存储规约收敛到 1..10（与 core 解析正则一致）。
 */
fun buildEditableFillAnswerPart(answerText: String, tag: String, score: String): String {
    val text = answerText.trim()
    if (text.isBlank()) return ""
    return buildString {
        append(text)
        tag.trim().takeIf { it.isNotBlank() }?.let { append('【').append(it).append('】') }
        score.trim().toIntOrNull()?.coerceIn(1, 10)?.let { append('【').append(it).append("分】") }
    }
}

fun insertEditableAnswerPart(answerParts: List<String>, index: Int): List<String> {
    val safeIndex = index.coerceIn(0, answerParts.size)
    return buildList(answerParts.size + 1) {
        addAll(answerParts.take(safeIndex))
        add("")
        addAll(answerParts.drop(safeIndex))
    }
}

fun removeEditableAnswerPart(answerParts: List<String>, index: Int): List<String> {
    if (answerParts.size <= 1) return answerParts
    val safeIndex = index.coerceIn(0, answerParts.lastIndex)
    return answerParts.filterIndexed { currentIndex, _ -> currentIndex != safeIndex }
}

fun parsePastedEditableFillAnswers(
    pastedText: String,
    currentContent: String,
    blankCount: Int
): List<String> {
    val normalizedText = pastedText.trim()
    if (normalizedText.isBlank()) return emptyList()

    val normalizedContent = normalizePasteComparableText(currentContent)
    val sourceLines = normalizedText
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .filterNot { normalizePasteComparableText(it) == normalizedContent }
        .filterNot { countEditableFillBlanks(it) > 0 }
        .toList()

    val collected = mutableListOf<String>()
    val candidateLines = if (sourceLines.isNotEmpty()) sourceLines else normalizedText.lines().map { it.trim() }.filter { it.isNotBlank() }
    for (line in candidateLines) {
        val numberedMatch = PASTED_NUMBERED_LINE_REGEX.matchEntire(line)
        val candidate = when {
            numberedMatch != null -> {
                val expectedNumber = collected.size + 1
                val parsedNumber = numberedMatch.groupValues[1].toIntOrNull()
                if (parsedNumber == expectedNumber) numberedMatch.groupValues[2] else line
            }
            line.contains(FILL_PART_DELIMITER) -> splitFillAnswerParts(line).firstOrNull().orEmpty()
            else -> line
        }

        val cleaned = candidate.trim().trimEnd('，', ';', '；', ',')
        if (cleaned.isBlank()) continue
        collected += cleaned
        if (collected.size >= blankCount) {
            return syncEditableFillAnswers(collected.take(blankCount), blankCount)
        }
    }

    return if (collected.isEmpty()) emptyList() else syncEditableFillAnswers(collected, blankCount)
}

private fun normalizePasteComparableText(text: String): String {
    return text
        .replace(EDITABLE_BLANK_REGEX, "")
        .replace(Regex("[\\s，、。？（）【】\\[\\]{}]+"), "")
        .lowercase()
}
