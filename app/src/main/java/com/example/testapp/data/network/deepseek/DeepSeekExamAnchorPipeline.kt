package com.example.testapp.data.network.deepseek

import com.example.testapp.domain.model.Question

object DeepSeekExamAnchorPipeline {

    fun fromQuestion(question: Question?): DeepSeekExamAnchor? {
        if (question == null) return null
        val content = question.content.trim()
        if (content.isBlank()) return null
        return DeepSeekExamAnchor(
            questionType = question.type.trim(),
            content = content,
            options = question.options.map { it.trim() }.filter { it.isNotBlank() },
            standardAnswer = question.answer.trim(),
            officialExplanation = question.explanation.trim()
        )
    }
}
