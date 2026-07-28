package com.example.testapp.uicommon.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** AI full-screen prompt and bubbles aligned with the active app theme. */
object AiChatPromptDesignTokens {
    val pageBackground: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val cardWhite: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val userBubble: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val userBubbleContent: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
    val brandBlue: Color = Color(0xFF4F8CFF)
    val textSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val sheetTopElevation = 4.dp
    val sheetShadowElevation = 14.dp
    val fieldCornerRadius = 28.dp
    val fieldMinHeight = 48.dp
    val fieldElevation = 6.dp
    val bubbleElevation = 8.dp
    val sendButtonSize = 48.dp
    val sendButtonElevation = 10.dp
    val promptIconSize = 28.dp
    val promptIconShadowAlpha = 0.22f
    val sheetHorizontalPadding = 16.dp
    val sheetVerticalPadding = 12.dp
    val fieldInnerHorizontalPadding = 16.dp
    val fieldInnerVerticalPadding = 12.dp
    val userBubbleCornerRadius = 24.dp
    val assistantCardCornerRadius = 20.dp
}
