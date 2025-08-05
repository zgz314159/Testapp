package com.example.testapp.presentation.component

import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

val LocalFontFamily = compositionLocalOf { FontFamily.Default }
val LocalFontSize = compositionLocalOf { 18.sp }

@Composable
fun FontStyleProvider(fontSize: Float, fontStyle: String, content: @Composable () -> Unit) {
    val fontFamily = when (fontStyle) {
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
    CompositionLocalProvider(
        LocalFontFamily provides fontFamily,
        LocalFontSize provides fontSize.sp
    ) {
        content()
    }
}
