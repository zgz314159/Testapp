package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.design.AppLazyBottomSheet
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun AnswerCardListDialogShell(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AppLazyBottomSheet(onDismiss = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm)
        ) {
            content()
        }
    }
}
