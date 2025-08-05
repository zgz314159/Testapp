package com.example.testapp.util

import com.example.testapp.domain.model.Question

/**
 * Format question content and its options into a single string.
 */
fun formatQuestionWithOptions(content: String, options: List<String>): String {
    return buildString {
        appendLine(content)
        options.forEachIndexed { index, option ->
            val letter = ('A' + index)
            appendLine("$letter. $option")
        }
    }.trim()
}

/**
 * Overload for Question model.
 */
fun formatQuestionWithOptions(question: Question): String =
    formatQuestionWithOptions(question.content, question.options)