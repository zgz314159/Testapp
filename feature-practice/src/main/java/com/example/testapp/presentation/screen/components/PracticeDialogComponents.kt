package com.example.testapp.presentation.screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.uicommon.design.AppConfirmDialog
import com.example.testapp.uicommon.design.AppOverlayMetrics
import com.example.testapp.uicommon.design.appOverlayContainerColor
import com.example.testapp.uicommon.design.appOverlayDialogShape
import com.example.testapp.uicommon.design.AppLoadingIndicator

@Composable
fun PracticeConfirmDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit
) {
    if (!show) return
    AppConfirmDialog(
        onDismiss = onDismiss,
        message = message,
        confirmLabel = confirmLabel,
        dismissLabel = dismissLabel,
        onConfirm = onConfirm,
    )
}

@Composable
fun PracticeChatGptDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    loading: Boolean,
    resultPair: Pair<Int, LocalizedResult>?,
    currentIndex: Int,
    onSaveToAnalysis: (String) -> Unit
) {
    if (!show) return
    val resultText = resultPair?.second?.toString().orEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (resultPair?.first == currentIndex && resultText.isNotBlank()) {
                    onSaveToAnalysis(resultText)
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            Column {
                if (loading) {
                    AppLoadingIndicator()
                } else {
                    Text(resultText)
                }
            }
        },
        shape = appOverlayDialogShape(),
        containerColor = appOverlayContainerColor(),
        tonalElevation = AppOverlayMetrics.dialogElevation,
    )
}
