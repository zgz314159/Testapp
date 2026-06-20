package com.example.testapp.presentation.screen.exam.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ExamAIResults(
    text: String,
    collapsed: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    backgroundColor: Color,
    onToggle: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit
) {
    ExamAnalysisSection(
        text = text,
        collapsed = collapsed,
        scrollState = scrollState,
        backgroundColor = backgroundColor,
        onToggle = { onToggle() },
        onDoubleTap = { onDoubleTap() },
        onLongPress = { onLongPress() }
    )
}
