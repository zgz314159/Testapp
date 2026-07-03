package com.example.testapp.data.repository.parser

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.util.guessQuestionType

internal fun resolveExcelQuestionType(
    rawType: String,
    answer: String,
    content: String,
    workbookSuggestsShortAnswer: Boolean
): String {
    val isShortAnswerCandidate = workbookSuggestsShortAnswer &&
        !ImportedFillBlankRegex.containsMatchIn(content) &&
        !ImportedFillSpaceRegex.containsMatchIn(content)

    if (QuestionTypes.isTextResponse(rawType)) return rawType.trim().ifBlank { "简答题" }

    if (rawType.isNotBlank()) {
        if (isShortAnswerCandidate && QuestionTypes.isInlineBlank(rawType)) {
            return "简答题"
        }
        return when {
            QuestionTypes.isSingle(rawType) -> QuestionTypes.SINGLE
            QuestionTypes.isMulti(rawType) -> QuestionTypes.MULTI
            QuestionTypes.isJudge(rawType) -> QuestionTypes.JUDGE
            QuestionTypes.isInlineBlank(rawType) -> QuestionTypes.BLANK
            else -> rawType
        }
    }
    return if (isShortAnswerCandidate) "简答题" else guessQuestionType(answer)
}

internal fun normalizeExcelOptionsForType(type: String, options: List<String>): List<String> {
    return if (QuestionTypes.isJudge(type) && options.isEmpty()) {
        listOf("对", "错")
    } else {
        options
    }
}
