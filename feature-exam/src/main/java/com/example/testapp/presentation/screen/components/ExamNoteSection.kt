package com.example.testapp.presentation.screen.exam.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ExamNoteSection(
    text: String,
    collapsed: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    backgroundColor: Color,
    contentColor: Color,
    onToggle: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit
) {
    ExamAnalysisSection(
        text = text,
        collapsed = collapsed,
        scrollState = scrollState,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        onToggle = { onToggle() },
        onDoubleTap = { onDoubleTap() },
        onLongPress = { onLongPress() }
    )
}
