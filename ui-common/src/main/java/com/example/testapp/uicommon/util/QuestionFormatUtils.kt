package com.example.testapp.uicommon.util

import com.example.testapp.domain.model.Question

fun formatQuestionForCopy(question: Question): String {
    val sb = StringBuilder()
    sb.appendLine(question.content)
    question.options.forEachIndexed { i, opt ->
        sb.appendLine("${('A' + i)}. $opt")
    }
    sb.appendLine("答案: ${question.answer}")
    return sb.toString()
}
