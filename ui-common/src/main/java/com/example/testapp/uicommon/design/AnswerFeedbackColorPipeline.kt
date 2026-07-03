package com.example.testapp.uicommon.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class AnswerFeedbackColors(
    val resultContainer: Color,
    val correctText: Color,
    val incorrectText: Color,
    val incorrectHintText: Color,
    val correctFieldBackground: Color,
    val incorrectFieldBackground: Color
)

@Composable
fun answerFeedbackColors(): AnswerFeedbackColors = sessionReadingAnswerFeedbackColors()
