package com.example.testapp.presentation.screen.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.LocalFontFamily

@Composable
fun rememberQuestionTextStyle(
    questionFontSize: Float,
    lineSpacingMultiplier: Float,
    letterSpacing: Float = 0f,
    color: Color = MaterialTheme.colorScheme.onSurface
): TextStyle {
    return MaterialTheme.typography.titleMedium.copy(
        fontSize = questionFontSize.sp,
        lineHeight = (questionFontSize * lineSpacingMultiplier).sp,
        letterSpacing = letterSpacing.sp,
        fontFamily = LocalFontFamily.current,
        color = color,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
}
