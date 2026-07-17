package com.example.testapp.uicommon.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class AnswerChoicePalette(
    val default: Color,
    val selected: Color,
    val correct: Color,
    val wrong: Color
)

fun resolveAnswerChoicePalette(
    darkTheme: Boolean,
    surface: Color,
    secondaryContainer: Color
): AnswerChoicePalette {
    val tokens = AnswerChoiceCorrectColorTokens
    return AnswerChoicePalette(
        default = surface,
        selected = secondaryContainer,
        correct = if (darkTheme) tokens.correctContainerDark else tokens.correctContainerLight,
        wrong = if (darkTheme) tokens.wrongContainerDark else tokens.wrongContainerLight
    )
}

@Composable
fun answerChoicePalette(): AnswerChoicePalette {
    val scheme = MaterialTheme.colorScheme
    val darkTheme = isSystemInDarkTheme()
    return resolveAnswerChoicePalette(
        darkTheme = darkTheme,
        surface = questionSessionOptionColor(),
        secondaryContainer = if (darkTheme) scheme.secondaryContainer else Color(0xFFF1F5FF)
    )
}

@Composable
fun answerChoiceBorderColor(tone: AnswerChoiceTone): Color = when (tone) {
    AnswerChoiceTone.Default -> questionSessionBorderColor()
    AnswerChoiceTone.Selected -> Color(0xFFBFD0FF)
    AnswerChoiceTone.Correct -> Color(0xFF72B879)
    AnswerChoiceTone.Wrong -> Color(0xFFE58B8B)
}

fun AnswerChoicePalette.colorFor(tone: AnswerChoiceTone): Color = when (tone) {
    AnswerChoiceTone.Default -> default
    AnswerChoiceTone.Selected -> selected
    AnswerChoiceTone.Correct -> correct
    AnswerChoiceTone.Wrong -> wrong
}
