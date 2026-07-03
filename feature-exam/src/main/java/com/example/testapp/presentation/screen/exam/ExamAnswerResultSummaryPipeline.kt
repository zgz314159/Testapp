package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.util.stripDrawingTags

fun resolveExamAnswerResultWrongToken(
    question: Question,
    correctIndices: List<Int>,
    displayOptions: List<String>,
    resolvedFillAnswer: String
): String {
    if (QuestionTypes.isInlineBlank(question.type) || QuestionTypes.isTextResponse(question.type)) {
        return stripDrawingTags(resolvedFillAnswer)
    }
    return if (correctIndices.all { it in displayOptions.indices }) {
        correctIndices.joinToString(", ") { displayOptions[it] }
    } else {
        question.answer
    }
}
