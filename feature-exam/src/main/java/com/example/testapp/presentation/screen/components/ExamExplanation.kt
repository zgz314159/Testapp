package com.example.testapp.presentation.screen.exam.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ExamExplanation(
    text: String,
    collapsed: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    backgroundColor: Color,
    contentColor: Color,
    onToggle: () -> Unit,
    onDoubleTap: (() -> Unit)? = null
) {
    ExamAnalysisSection(
        text = text,
        collapsed = collapsed,
        scrollState = scrollState,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        onToggle = { onToggle() },
        onDoubleTap = onDoubleTap
    )
}
