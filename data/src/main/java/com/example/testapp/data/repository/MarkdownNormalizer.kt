package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.entity.QuestionEntity
import com.example.testapp.core.util.normalizeRichMarkdownStructure
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.MarkdownCleanupPreview
import javax.inject.Inject
import javax.inject.Singleton

fun Question.normalizeRichMarkdownFields(): Question {
    return copy(
        content = normalizeRichMarkdownStructure(content),
        answer = normalizeRichMarkdownStructure(answer),
        explanation = normalizeRichMarkdownStructure(explanation)
    )
}

@Singleton
class MarkdownNormalizer @Inject constructor(private val dao: QuestionDao) {

    fun buildCleanupPreview(entity: QuestionEntity): MarkdownCleanupPreview? {
        val normalized = entity.normalizeMarkdownFields()
        if (normalized == entity) return null
        val changedFields = buildList {
            if (entity.content != normalized.content) add("content")
            if (entity.answer != normalized.answer) add("answer")
            if (entity.explanation != normalized.explanation) add("explanation")
        }
        return MarkdownCleanupPreview(
            questionId = entity.id,
            fileName = entity.fileName,
            changedFields = changedFields,
            beforeSnippet = entity.markdownCleanupSnippet(),
            afterSnippet = normalized.markdownCleanupSnippet()
        )
    }

    fun QuestionEntity.normalizeMarkdownFields(): QuestionEntity {
        return copy(
            content = normalizeRichMarkdownStructure(content),
            answer = normalizeRichMarkdownStructure(answer),
            explanation = normalizeRichMarkdownStructure(explanation)
        )
    }

    fun QuestionEntity.markdownCleanupSnippet(): String {
        return listOf(
            "answer" to answer,
            "explanation" to explanation,
            "content" to content
        ).firstOrNull { (_, value) ->
            value.contains("解：") ||
                value.contains("已知条件") ||
                value.contains("计算步骤") ||
                value.contains("*")
        }?.second
            ?.lineSequence()
            ?.take(12)
            ?.joinToString("\n")
            .orEmpty()
            .take(1200)
    }
}
