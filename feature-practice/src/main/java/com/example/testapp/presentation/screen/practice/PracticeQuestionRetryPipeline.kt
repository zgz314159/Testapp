package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.core.util.retainCorrectFillAnswerParts
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.QuestionWithState

/** 批改后重答：整题清空 / 填空保留答对空。 */
object PracticeQuestionRetryPipeline {

    fun reopenCurrent(qws: QuestionWithState): QuestionWithState =
        qws.copy(
            selectedOptions = emptyList(),
            textAnswer = "",
            showResult = false,
            sessionAnswerTime = 0L
        )

    fun reopenWrongBlanks(qws: QuestionWithState): QuestionWithState {
        if (!QuestionTypes.isInlineBlank(qws.question.type)) return reopenCurrent(qws)
        val retained = retainCorrectFillAnswerParts(
            userAnswer = qws.textAnswer,
            correctAnswer = resolveFillCorrectAnswer(qws.question)
        )
        return qws.copy(
            textAnswer = retained,
            selectedOptions = if (retained.isNotBlank()) listOf(-1) else emptyList(),
            showResult = false,
            sessionAnswerTime = 0L
        )
    }
}
