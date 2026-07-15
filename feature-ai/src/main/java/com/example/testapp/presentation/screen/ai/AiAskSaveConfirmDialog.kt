package com.example.testapp.presentation.screen.ai

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

/**
 * AI 问答退出保存确认。
 *
 * - 点窗外汇：无动作（不关、不取消）
 * - 系统返回 / 侧滑返回：仅关掉弹窗，不执行「不保存并退出」
 * - 「保存」「取消」按钮：显式确认 / 放弃
 */
@Composable
fun AiAskSaveConfirmDialog(
    visible: Boolean,
    message: String,
    saveLabel: String,
    dismissLabel: String,
    onSave: () -> Unit,
    onDiscardAndLeave: () -> Unit,
    onCloseDialogOnly: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onCloseDialogOnly,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        ),
        confirmButton = {
            TextButton(onClick = onSave) { Text(saveLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDiscardAndLeave) { Text(dismissLabel) }
        },
        text = { Text(message) },
    )
}
