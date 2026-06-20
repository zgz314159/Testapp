package com.example.testapp.presentation.screen.exam.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.feature.exam.R
import com.example.testapp.uicommon.component.LocalFontSize

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
        AlertDialog(
            onDismissRequest = onDismissDeleteNote,
            confirmButton = {
                TextButton(onClick = onConfirmDeleteNote) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteNote) { Text(stringResource(R.string.cancel)) }
            },
            text = { Text(stringResource(R.string.confirm_delete_note)) }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            confirmButton = {
                TextButton(onClick = onConfirmDelete) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) { Text(stringResource(R.string.cancel)) }
            },
            text = { Text(stringResource(R.string.confirm_delete_analysis, deleteReadableLabel)) }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = onDismissExit,
            confirmButton = {
                TextButton(onClick = onConfirmExit) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissExit) { Text(stringResource(R.string.cancel)) }
            },
            text = { Text(exitConfirmText) }
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
                    Text(if (chatGptResult.isNullOrBlank()) stringResource(R.string.no_analysis_result) else chatGptResult, fontSize = LocalFontSize.current)
                }
            }
        )
    }
}


