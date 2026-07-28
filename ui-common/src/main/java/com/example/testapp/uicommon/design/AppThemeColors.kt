package com.example.testapp.uicommon.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Theme-aware semantic colors shared by custom elevated UI components. */
object AppThemeColors {
    val isDark: Boolean
        @Composable get() = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val pageBackground: Color
        @Composable get() = MaterialTheme.colorScheme.background

    val card: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val cardVariant: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val textPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val textSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val outline: Color
        @Composable get() = MaterialTheme.colorScheme.outline

    val primary: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val primarySoft: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer

    val success: Color
        @Composable get() = if (isDark) Color(0xFF65C695) else Color(0xFF3B9C70)

    val successSoft: Color
        @Composable get() = if (isDark) Color(0xFF183A2B) else Color(0xFFE4F5EC)

    val warningSoft: Color
        @Composable get() = if (isDark) Color(0xFF3D321A) else Color(0xFFFFF4D6)
}
