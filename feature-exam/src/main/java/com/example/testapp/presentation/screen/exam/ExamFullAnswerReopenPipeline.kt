package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.QuestionWithState

object ExamFullAnswerReopenPipeline {

    fun reopenAt(
        questionsWithState: List<QuestionWithState>,
        index: Int
    ): List<QuestionWithState>? {
        if (index !in questionsWithState.indices) return null
        return questionsWithState.mapIndexed { idx, qws ->
            if (idx == index) ExamQuestionRetryPipeline.reopenWrongBlanks(qws) else qws
        }
    }
}
