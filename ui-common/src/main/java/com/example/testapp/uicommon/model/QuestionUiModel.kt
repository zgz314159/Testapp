package com.example.testapp.uicommon.model

import com.example.testapp.domain.model.AnswerStatus
import com.example.testapp.domain.model.Question

data class QuestionUiModel(
    val question: Question,
    var status: AnswerStatus = AnswerStatus.UNANSWERED,
    var selectedOptions: List<Int> = emptyList()
)
