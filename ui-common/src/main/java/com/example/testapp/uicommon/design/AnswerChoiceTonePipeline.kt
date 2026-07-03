package com.example.testapp.uicommon.design

enum class AnswerChoiceTone {
    Default,
    Selected,
    Correct,
    Wrong
}

fun resolveAnswerChoiceTone(
    showResult: Boolean,
    isSelected: Boolean,
    isCorrectOption: Boolean
): AnswerChoiceTone = when {
    showResult && isCorrectOption -> AnswerChoiceTone.Correct
    showResult && isSelected && !isCorrectOption -> AnswerChoiceTone.Wrong
    isSelected -> AnswerChoiceTone.Selected
    else -> AnswerChoiceTone.Default
}
