package com.example.testapp.uicommon.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

fun resolveSessionReadingAnswerFeedbackColors(darkTheme: Boolean): AnswerFeedbackColors {
    val t = SessionReadingSectionTokens
    return if (darkTheme) {
        AnswerFeedbackColors(
            resultContainer = t.resultContainerDark,
            correctText = t.correctTextDark,
            incorrectText = t.incorrectTextDark,
            incorrectHintText = t.incorrectHintDark,
            correctFieldBackground = t.correctFieldDark,
            incorrectFieldBackground = t.incorrectFieldDark
        )
    } else {
        AnswerFeedbackColors(
            resultContainer = t.resultContainerLight,
            correctText = t.correctTextLight,
            incorrectText = t.incorrectTextLight,
            incorrectHintText = t.incorrectHintLight,
            correctFieldBackground = t.correctFieldLight,
            incorrectFieldBackground = t.incorrectFieldLight
        )
    }
}

@Composable
fun sessionReadingAnswerFeedbackColors(): AnswerFeedbackColors =
    resolveSessionReadingAnswerFeedbackColors(isSystemInDarkTheme())
