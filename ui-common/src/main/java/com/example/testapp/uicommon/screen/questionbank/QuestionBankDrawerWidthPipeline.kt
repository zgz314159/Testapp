package com.example.testapp.uicommon.screen.questionbank

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val DRAWER_WIDTH_FRACTION = 0.85f
private val DRAWER_MAX_WIDTH_DP = 320.dp
private val DRAWER_MIN_SCRIM_DP = 56.dp

fun resolveQuestionBankDrawerWidth(screenWidthDp: Int): Dp {
    val maxDrawerWidth = (screenWidthDp.dp - DRAWER_MIN_SCRIM_DP)
        .coerceAtMost(DRAWER_MAX_WIDTH_DP)
    return (screenWidthDp.dp * DRAWER_WIDTH_FRACTION).coerceAtMost(maxDrawerWidth)
}
