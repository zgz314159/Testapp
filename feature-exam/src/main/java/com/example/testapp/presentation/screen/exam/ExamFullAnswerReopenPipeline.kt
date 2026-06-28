package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.core.util.retainCorrectFillAnswerParts
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.QuestionWithState

/** 全答须全对：重开单题作答态（填空保留已对空） */
object ExamFullAnswerReopenPipeline {

    fun reopenAt(
        questionsWithState: List<QuestionWithState>,
        index: Int
    ): List<QuestionWithState>? {
        if (index !in questionsWithState.indices) return null
        val target = questionsWithState[index]
        return questionsWithState.mapIndexed { idx, qws ->
            if (idx != index) qws
            else if (QuestionTypes.isFill(qws.question.type)) {
                val retained = retainCorrectFillAnswerParts(
                    userAnswer = target.textAnswer,
                    correctAnswer = resolveFillCorrectAnswer(target.question)
                )
                qws.copy(
                    textAnswer = retained,
                    selectedOptions = if (retained.isNotBlank()) listOf(-1) else emptyList(),
                    showResult = false,
                    sessionAnswerTime = 0L
                )
            } else {
                qws.copy(
                    selectedOptions = emptyList(),
                    textAnswer = "",
                    showResult = false,
                    sessionAnswerTime = 0L
                )
            }
        }
    }
}
