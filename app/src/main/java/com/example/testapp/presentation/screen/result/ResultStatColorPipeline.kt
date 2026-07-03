package com.example.testapp.presentation.screen.result

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class ResultStatPalette(
    val correct: Color,
    val wrong: Color,
    val unanswered: Color,
    val chartAxis: Color
)

@Composable
fun resultStatPalette(): ResultStatPalette {
    val scheme = MaterialTheme.colorScheme
    return ResultStatPalette(
        correct = scheme.tertiary,
        wrong = scheme.error,
        unanswered = scheme.onSurfaceVariant,
        chartAxis = scheme.outlineVariant
    )
}
