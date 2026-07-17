package com.example.testapp.presentation.screen.exam.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.testapp.feature.exam.R
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.AppConfirmDialog
import com.example.testapp.uicommon.design.AppOverlayMetrics
import com.example.testapp.uicommon.design.appOverlayContainerColor
import com.example.testapp.uicommon.design.appOverlayDialogShape

@Composable
fun ExamDialogs(
    showDeleteNoteDialog: Boolean,
    onDismissDeleteNote: () -> Unit,
    onConfirmDeleteNote: () -> Unit,

    showDeleteDialog: Boolean,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    deleteReadableLabel: String,

    showExitDialog: Boolean,
    onDismissExit: () -> Unit,
    onConfirmExit: () -> Unit,
    exitConfirmText: String,

    showChatGptDialog: Boolean,
    onDismissChatGpt: () -> Unit,
    onConfirmChatGpt: () -> Unit,
    chatGptLoading: Boolean,
    chatGptResult: String?,
) {
    if (showDeleteNoteDialog) {
        AppConfirmDialog(
            onDismiss = onDismissDeleteNote,
            message = stringResource(R.string.confirm_delete_note),
            confirmLabel = stringResource(R.string.confirm),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = onConfirmDeleteNote,
        )
    }

    if (showDeleteDialog) {
        AppConfirmDialog(
            onDismiss = onDismissDelete,
            message = stringResource(R.string.confirm_delete_analysis, deleteReadableLabel),
            confirmLabel = stringResource(R.string.confirm),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = onConfirmDelete,
        )
    }

    if (showExitDialog) {
        AppConfirmDialog(
            onDismiss = onDismissExit,
            message = exitConfirmText,
            confirmLabel = stringResource(R.string.confirm),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = onConfirmExit,
        )
    }

    if (showChatGptDialog) {
        val context = LocalContext.current
        fun resolve(res: com.example.testapp.core.common.LocalizedResult?): String {
            return res?.let { r ->
                val resId = context.resources.getIdentifier(r.key, "string", context.packageName)
                if (resId != 0) try {
                    context.getString(resId, *r.args.toTypedArray())
                } catch (e: Exception) {
                    if (r.args.isEmpty()) r.key else r.key + " " + r.args.joinToString(",")
                } else r.key
            } ?: ""
        }
        AlertDialog(
            onDismissRequest = onDismissChatGpt,
            confirmButton = {
                TextButton(onClick = onConfirmChatGpt) { Text(stringResource(R.string.save_to_analysis)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissChatGpt) { Text(stringResource(R.string.close)) }
            },
            text = {
                if (chatGptLoading) {
                    Text(stringResource(R.string.baidu_parsing), fontSize = LocalFontSize.current)
                } else {
                    Text(
                        if (chatGptResult.isNullOrBlank()) {
                            stringResource(R.string.no_analysis_result)
                        } else {
                            chatGptResult
                        },
                        fontSize = LocalFontSize.current,
                    )
                }
            },
            shape = appOverlayDialogShape(),
            containerColor = appOverlayContainerColor(),
            tonalElevation = AppOverlayMetrics.dialogElevation,
        )
    }
}
