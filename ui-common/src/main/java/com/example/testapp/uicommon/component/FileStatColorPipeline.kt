package com.example.testapp.uicommon.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class FileStatPalette(
    val total: Color,
    val wrong: Color,
    val favorite: Color,
    val progress: Color
)

@Composable
fun fileStatPalette(): FileStatPalette {
    val scheme = MaterialTheme.colorScheme
    return FileStatPalette(
        total = scheme.primary,
        wrong = scheme.error,
        favorite = scheme.secondary,
        progress = scheme.tertiary
    )
}
