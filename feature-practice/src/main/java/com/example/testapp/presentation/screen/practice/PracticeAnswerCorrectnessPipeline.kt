package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question

/** 与 [com.example.testapp.uicommon.util.buildPracticeAnswerResult] 一致的判题。 */
object PracticeAnswerCorrectnessPipeline {

    fun isAllCorrect(
        question: Question,
        textAnswer: String,
        selectedOptions: List<Int>,
        resolvedFillAnswer: String,
        correctIndices: List<Int>
    ): Boolean = if (QuestionTypes.isFill(question.type)) {
        isFillAnswerCorrect(textAnswer, resolvedFillAnswer)
    } else {
        selectedOptions.toSet() == correctIndices.toSet()
    }
}
