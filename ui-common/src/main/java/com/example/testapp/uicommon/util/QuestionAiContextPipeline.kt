package com.example.testapp.uicommon.util

import com.example.testapp.domain.model.Question

/** 发往 AI 问答的题干上下文：含题型，不含标准答案。 */
object QuestionAiContextPipeline {

    fun formatQuestionForAi(question: Question): String = buildString {
        val type = question.type.trim()
        if (type.isNotBlank()) appendLine("题型：$type")
        appendLine(question.content.trim())
        question.options.map { it.trim() }.filter { it.isNotBlank() }.forEachIndexed { index, option ->
            appendLine("${('A' + index)}. $option")
        }
    }.trimEnd()
}

fun formatQuestionForAi(question: Question): String =
    QuestionAiContextPipeline.formatQuestionForAi(question)
