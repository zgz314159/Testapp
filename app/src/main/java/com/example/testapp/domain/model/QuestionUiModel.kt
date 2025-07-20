package com.example.testapp.presentation.model

import com.example.testapp.domain.model.Question

/**
 * UI model that combines a [Question] with the user's current answer state.
 */
data class QuestionUiModel(
    val question: Question,
    var status: AnswerStatus = AnswerStatus.UNANSWERED,
    var selectedOptions: List<Int> = emptyList()
)