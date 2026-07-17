package com.example.testapp.uicommon.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object AnswerPageColors {
    val PageBg = Color(0xFFF1F1F1)
    val Surface = Color(0xFFF8F7F4)
    val Surface2 = Color(0xFFFDFCF9)
    val BorderSoft = Color(0xFFE9E6DF)
    val ShadowDark = Color(0x14000000)
    val ShadowLight = Color(0x66FFFFFF)
    val TextPrimary = Color(0xFF2B2B2B)
    val TextSecondary = Color(0xFF6A6A6A)
    val AccentBlue = Color(0xFF2F66F3)
    val ProgressTrack = Color(0xFFE6DCF7)
    val ProgressFill = Color(0xFF4F7DFF)
    val SubmitTray = Color(0xFFF4F1EC)
}

@Composable
fun questionSessionSurfaceColor(): Color =
    if (isSystemInDarkTheme()) {
        androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        AnswerPageColors.Surface
    }

@Composable
fun questionSessionOptionColor(): Color =
    if (isSystemInDarkTheme()) {
        androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
    } else {
        AnswerPageColors.Surface2
    }

@Composable
fun questionSessionBorderColor(): Color =
    if (isSystemInDarkTheme()) {
        androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
    } else {
        AnswerPageColors.BorderSoft
    }

@Composable
fun questionSessionSubmitTrayColor(): Color =
    if (isSystemInDarkTheme()) {
        androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
    } else {
        AnswerPageColors.SubmitTray
    }

@Composable
fun Modifier.questionSessionSoftCard(
    shape: Shape,
    elevation: Dp,
    containerColor: Color = questionSessionSurfaceColor(),
    borderColor: Color = questionSessionBorderColor(),
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = AnswerPageColors.ShadowDark,
        spotColor = AnswerPageColors.ShadowDark,
        clip = false,
    )
    .background(containerColor, shape)
    .border(1.dp, borderColor, shape)
