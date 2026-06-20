package com.example.testapp.uicommon.component

enum class AnswerCardStatus { UNANSWERED, SELECTED, CORRECT, WRONG }

data class AnswerCardItemState(
    val index: Int,
    val label: String,
    val status: AnswerCardStatus,
    val isCurrent: Boolean = false
)
