package com.example.testapp.uicommon.design

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.component.QuestionSessionImeScrollSpacer

/** Scrollable body between fixed chrome slots. No layout-level IME padding. */
@Composable
fun QuestionSessionBodyScroll(
    scrollState: ScrollState,
    onScrollInProgress: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    LaunchedEffect(scrollState.isScrollInProgress) {
        onScrollInProgress(scrollState.isScrollInProgress)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        content = {
            content()
            QuestionSessionImeScrollSpacer()
        }
    )
}
