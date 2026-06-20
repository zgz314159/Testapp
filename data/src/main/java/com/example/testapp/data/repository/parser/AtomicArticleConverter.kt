package com.example.testapp.data.repository.parser

import com.example.testapp.data.repository.ImportedQuestionPayload
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.util.FILL_PART_DELIMITER

fun AtomicArticlePayload.toImportedQuestionPayload(originFileName: String): ImportedQuestionPayload? {
    if (segments.isEmpty()) return null

    val maskableSegments = segments.filter { it.s > 0 && it.t.isNotBlank() }
    if (maskableSegments.isEmpty()) return null

    val articlePrefix = article_no?.let { "第${it}条 " }.orEmpty()

    val generatedBody = buildString {
        segments.forEach { segment ->
            append(if (segment.s > 0 && segment.t.isNotBlank()) JsonQuestionParser.INLINE_BLANK_PLACEHOLDER else segment.t)
        }
    }.trim()

    val generatedContent = buildString {
        if (articlePrefix.isNotBlank() && !generatedBody.startsWith(articlePrefix.trim())) {
            append(articlePrefix)
        }
        append(generatedBody)
    }.trim()

    val answerParts = maskableSegments.map { segment ->
        buildString {
            append(segment.t.trim())
            segment.tag.trim().takeIf { it.isNotBlank() }?.let {
                append("【")
                append(it)
                append("】")
            }
            append("【")
            append(segment.s.coerceAtLeast(0))
            append("分】")
        }
    }

    val answer = if (answerParts.size == 1) answerParts.first() else answerParts.joinToString(FILL_PART_DELIMITER)
    val explanation = buildString {
        source.trim().takeIf { it.isNotBlank() }?.let { append(it) }
        article_no?.let {
            if (isNotEmpty()) append(' ')
            append("第")
            append(it)
            append("条")
        }
        item_id.trim().takeIf { it.isNotBlank() }?.let {
            if (isNotEmpty()) append(" | ")
            append(it)
        }
    }

    return ImportedQuestionPayload(
        question = Question(
            id = 0,
            content = generatedContent,
            type = QuestionTypes.BLANK,
            options = emptyList(),
            answer = answer,
            explanation = explanation,
            isFavorite = false,
            isWrong = false,
            isEdited = false,
            fileName = originFileName
        )
    )
}
