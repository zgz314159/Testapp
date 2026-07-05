package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamAnswerRules @Inject constructor() {

    fun isQuestionAnswered(state: UnifiedQuestionState) =
        state.showResult || state.selectedOptions.isNotEmpty() || state.textAnswer.isNotBlank()

    fun isQuestionCorrect(question: Question, state: UnifiedQuestionState): Boolean {
        if (!isQuestionAnswered(state)) return false
        return if (QuestionTypes.isFill(question.type))
            isFillAnswerCorrect(state.textAnswer, resolveFillCorrectAnswer(question))
        else state.selectedOptions.sorted() == answerToOptionIndices(question).sorted()
    }
}

