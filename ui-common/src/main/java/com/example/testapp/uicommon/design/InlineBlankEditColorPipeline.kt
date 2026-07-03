package com.example.testapp.uicommon.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class InlineBlankEditColors(
    val blankText: Color,
    val cursor: Color
)

@Composable
fun inlineBlankEditColors(): InlineBlankEditColors {
    val primary = MaterialTheme.colorScheme.primary
    return InlineBlankEditColors(
        blankText = primary,
        cursor = primary
    )
}
