package com.example.testapp.presentation.screen.practice

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.components.PracticeChatGptDialog
import com.example.testapp.presentation.screen.components.PracticeConfirmDialog

@Composable
fun PracticeDialogsHost(
    showDeleteNoteDialog: Boolean,
    onDismissDeleteNote: () -> Unit,
    onConfirmDeleteNote: () -> Unit,
    showDeleteDialog: Boolean,
    deleteTarget: String,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    showExitDialog: Boolean,
    sessionInputCount: Int,
    totalCount: Int,
    onDismissExit: () -> Unit,
    onConfirmExit: () -> Unit,
    showChatGptDialog: Boolean,
    onDismissChatGpt: () -> Unit,
    chatGptLoading: Boolean,
    chatGptResult: Pair<Int, com.example.testapp.core.common.LocalizedResult>?,
    currentIndex: Int,
    onSaveChatGptToAnalysis: (String) -> Unit,
    // Delete analysis strings
    deepseekLabel: String,
    sparkLabel: String,
    baiduLabel: String
) {
    PracticeConfirmDialog(
        show = showDeleteNoteDialog,
        onDismiss = onDismissDeleteNote,
        message = stringResource(R.string.confirm_delete_note),
        confirmLabel = stringResource(R.string.confirm),
        dismissLabel = stringResource(R.string.cancel),
        onConfirm = onConfirmDeleteNote
    )
    PracticeConfirmDialog(
        show = showDeleteDialog,
        onDismiss = onDismissDelete,
        message = stringResource(
            R.string.confirm_delete_analysis,
            when (deleteTarget) {
                "deepseek" -> deepseekLabel
                "spark" -> sparkLabel
                "baidu" -> baiduLabel
                else -> ""
            }
        ),
        confirmLabel = stringResource(R.string.confirm),
        dismissLabel = stringResource(R.string.cancel),
        onConfirm = onConfirmDelete
    )
    PracticeConfirmDialog(
        show = showExitDialog,
        onDismiss = onDismissExit,
        message = if (sessionInputCount < totalCount) stringResource(R.string.confirm_submit_unfinished) else stringResource(R.string.confirm_submit),
        confirmLabel = stringResource(R.string.confirm),
        dismissLabel = stringResource(R.string.cancel),
        onConfirm = onConfirmExit
    )
    PracticeChatGptDialog(
        show = showChatGptDialog,
        onDismiss = onDismissChatGpt,
        loading = chatGptLoading,
        resultPair = chatGptResult,
        currentIndex = currentIndex,
        onSaveToAnalysis = onSaveChatGptToAnalysis
    )
}


