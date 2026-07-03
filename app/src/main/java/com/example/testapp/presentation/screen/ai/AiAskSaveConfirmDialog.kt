package com.example.testapp.presentation.screen.ai

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun AiAskSaveConfirmDialog(
    visible: Boolean,
    message: String,
    saveLabel: String,
    dismissLabel: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onSave) { Text(saveLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
        text = { Text(message) }
    )
}
