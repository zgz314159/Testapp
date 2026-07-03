package com.example.testapp.data.repository.parser

import com.example.testapp.domain.util.splitFillAnswerParts

internal val ImportedFillSpaceRegex = Regex("[\\u0020\\t\\u00A0\\u3000]{2,}")
internal val ImportedFillBlankRegex = Regex("_{2,}|（\\s*）|\\(\\s*\\)|【\\s*】|\\[\\s*]")

internal fun normalizeImportedFillContent(content: String, answer: String): String {
    val answerParts = splitFillAnswerParts(answer)
    if (answerParts.isEmpty()) return content

    val existingBlankCount = ImportedFillBlankRegex.findAll(content).count()
    if (existingBlankCount >= answerParts.size) return content

    val matches = ImportedFillSpaceRegex.findAll(content).toList()
    val blanksNeeded = (answerParts.size - existingBlankCount).coerceAtLeast(0)
    if (blanksNeeded == 0) return content

    val builder = StringBuilder(content.length + blanksNeeded * 4)
    var lastIndex = 0
    var replacedCount = 0
    for (match in matches) {
        builder.append(content, lastIndex, match.range.first)
        if (replacedCount < blanksNeeded) {
            builder.append("____")
            replacedCount += 1
        } else {
            builder.append(match.value)
        }
        lastIndex = match.range.last + 1
    }
    builder.append(content.substring(lastIndex))

    var normalized = builder.toString()
    val missingBlankCount = (answerParts.size - ImportedFillBlankRegex.findAll(normalized).count()).coerceAtLeast(0)
    if (missingBlankCount == 0) return normalized

    val blankMatches = ImportedFillBlankRegex.findAll(normalized).toList()
    if (blankMatches.isNotEmpty()) {
        val lastBlank = blankMatches.last()
        val suffix = buildString(missingBlankCount * 5) {
            repeat(missingBlankCount) { append("、____") }
        }
        normalized = buildString(normalized.length + suffix.length) {
            append(normalized, 0, lastBlank.range.last + 1)
            append(suffix)
            append(normalized.substring(lastBlank.range.last + 1))
        }
        return normalized
    }

    val trailingPunctuation = Regex("[。．；;，,、：:！？!?）)】\\]]+$")
    val punctuationMatch = trailingPunctuation.find(normalized)
    val appendedBlanks = buildString(missingBlankCount * 5) {
        repeat(missingBlankCount) {
            append(if (isEmpty()) "____" else "、____")
        }
    }

    return if (punctuationMatch != null) {
        buildString(normalized.length + appendedBlanks.length + 1) {
            append(normalized, 0, punctuationMatch.range.first)
            if (isNotEmpty() && this[lastIndex] != ' ') append(' ')
            append(appendedBlanks)
            append(normalized.substring(punctuationMatch.range.first))
        }
    } else {
        normalized + " " + appendedBlanks
    }
}
