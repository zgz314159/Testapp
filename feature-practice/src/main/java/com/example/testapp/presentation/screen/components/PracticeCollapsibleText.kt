package com.example.testapp.presentation.screen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.AnalysisSectionTone
import com.example.testapp.uicommon.design.analysisSectionColors

@Composable
fun PracticeExplanationBox(
    text: String,
    collapsed: Boolean,
    scrollStateProvider: () -> ScrollState,
    questionFontSize: Float,
    onToggle: () -> Unit
) {
    CollapsibleTextBox(
        text = text,
        collapsed = collapsed,
        scrollState = scrollStateProvider(),
        backgroundColor = analysisSectionColors(AnalysisSectionTone.Explanation).container,
        questionFontSize = questionFontSize,
        onToggle = onToggle
    )
}

@Composable
fun PracticeNoteBox(
    note: String,
    collapsed: Boolean,
    scrollStateProvider: () -> ScrollState,
    questionFontSize: Float,
    onToggle: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit
) {
    CollapsibleTextBox(
        text = note,
        collapsed = collapsed,
        scrollState = scrollStateProvider(),
        backgroundColor = analysisSectionColors(AnalysisSectionTone.Note).container,
        questionFontSize = questionFontSize,
        onToggle = onToggle,
        onDoubleTap = onDoubleTap,
        onLongPress = onLongPress
    )
}

@Composable
fun ExamAnalysisSection(
    text: String,
    collapsed: Boolean,
    scrollState: ScrollState,
    backgroundColor: Color,
    onToggle: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit
) {
    CollapsibleTextBox(
        text = text,
        collapsed = collapsed,
        scrollState = scrollState,
        backgroundColor = backgroundColor,
        questionFontSize = LocalFontSize.current.value,
        onToggle = onToggle,
        onDoubleTap = onDoubleTap,
        onLongPress = onLongPress
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun CollapsibleTextBox(
    text: String,
    collapsed: Boolean,
    scrollState: ScrollState,
    backgroundColor: Color,
    questionFontSize: Float,
    onToggle: () -> Unit,
    onDoubleTap: () -> Unit = onToggle,
    onLongPress: () -> Unit = onToggle
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = onToggle,
                onDoubleClick = onDoubleTap,
                onLongClick = onLongPress
            )
            .padding(8.dp)
            .then(if (collapsed) Modifier else Modifier.verticalScroll(scrollState)),
        fontSize = questionFontSize.sp,
        fontFamily = LocalFontFamily.current,
        maxLines = if (collapsed) 3 else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis
    )
}
