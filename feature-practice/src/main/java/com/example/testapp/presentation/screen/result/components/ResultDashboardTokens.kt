package com.example.testapp.presentation.screen.result.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.testapp.uicommon.design.AppThemeColors

object ResultDashboardColors {
    val PageBackground: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val Card: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val Primary: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val PrimaryLight: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val Track: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val TextPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val TextSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val TextTertiary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
    val Success: Color
        @Composable get() = AppThemeColors.success
    val SuccessBackground: Color
        @Composable get() = AppThemeColors.successSoft
    val Error: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val ErrorBackground: Color
        @Composable get() = MaterialTheme.colorScheme.errorContainer
    val Neutral: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val NeutralBackground: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val BlueBackground: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val Border: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val Trophy: Color = Color(0xFFFFB84D)
}
