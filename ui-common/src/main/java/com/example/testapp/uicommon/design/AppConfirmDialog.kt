package com.example.testapp.uicommon.design

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppConfirmDialog(
    onDismiss: () -> Unit,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
        title = title?.let { { Text(it) } },
        text = { Text(message) },
        shape = appOverlayDialogShape(),
        containerColor = appOverlayContainerColor(),
        tonalElevation = AppOverlayMetrics.dialogElevation,
    )
}
