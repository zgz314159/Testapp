package com.example.testapp.uicommon.util

import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.core.util.prepareRichDisplayText
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question

data class PracticeAnswerResult(
    val allCorrect: Boolean,
    val correctText: String
)

fun buildPracticeAnswerResult(
    question: Question,
    textAnswer: String,
    selectedOption: List<Int>,
    resolvedFillAnswer: String,
    displayOptions: List<String>,
    correctIndices: List<Int>
): PracticeAnswerResult {
    val allCorrect = if (QuestionTypes.isFill(question.type)) {
        isFillAnswerCorrect(textAnswer, resolvedFillAnswer)
    } else {
        selectedOption.toSet() == correctIndices.toSet()
    }

    val correctText = when {
        QuestionTypes.isInlineBlank(question.type) ->
            formatFillCorrectAnswerDisplay(question.content, resolvedFillAnswer)
        QuestionTypes.isTextResponse(question.type) ->
            prepareRichDisplayText(stripDrawingTags(resolvedFillAnswer))
        correctIndices.all { it in displayOptions.indices } ->
            correctIndices.joinToString("，") { displayOptions[it] }
        else -> question.answer
    }

    return PracticeAnswerResult(allCorrect = allCorrect, correctText = correctText)
}
