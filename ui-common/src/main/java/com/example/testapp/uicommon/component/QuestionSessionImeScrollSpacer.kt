package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 滚动区末尾占位：键盘弹出时可手动滚入视口，不推动底栏。 */
@Composable
fun QuestionSessionImeScrollSpacer() {
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    if (imeBottom > 0.dp) {
        Spacer(modifier = Modifier.height(imeBottom))
    }
}
