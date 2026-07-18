package com.example.testapp.presentation.screen.home

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens
import com.example.testapp.uicommon.design.AppElevatedConfirmDialog

@Composable
fun HomeScreenDialogs(
    showDeleteDialog: Boolean,
    fileToDelete: String,
    onDismissDeleteFile: () -> Unit,
    onConfirmDeleteFile: () -> Unit,
    showAddFolderDialog: Boolean,
    newFolderName: String,
    onNewFolderNameChange: (String) -> Unit,
    onDismissAddFolder: () -> Unit,
    onConfirmAddFolder: () -> Unit,
    renameFolderTarget: String?,
    renameFolderName: String,
    onRenameFolderNameChange: (String) -> Unit,
    onDismissRenameFolder: () -> Unit,
    onConfirmRenameFolder: () -> Unit,
    showDeleteFolderDialog: Boolean,
    folderToDelete: String?,
    onDismissDeleteFolder: () -> Unit,
    onConfirmDeleteFolder: () -> Unit,
) {
    if (showDeleteDialog) {
        AppElevatedConfirmDialog(
            onDismiss = onDismissDeleteFile,
            title = "删除题库",
            message = "确认删除 $fileToDelete 及其相关数据？",
            confirmLabel = "删除",
            dismissLabel = "取消",
            onConfirm = onConfirmDeleteFile,
            confirmDestructive = true,
        )
    }
    if (showAddFolderDialog) {
        HomeInputDialog(
            title = "新建文件夹",
            value = newFolderName,
            onValueChange = onNewFolderNameChange,
            label = "文件夹名",
            onDismiss = onDismissAddFolder,
            onConfirm = onConfirmAddFolder,
        )
    }
    if (renameFolderTarget != null) {
        HomeInputDialog(
            title = "重命名文件夹",
            value = renameFolderName,
            onValueChange = onRenameFolderNameChange,
            label = "重命名",
            onDismiss = onDismissRenameFolder,
            onConfirm = onConfirmRenameFolder,
        )
    }
    if (showDeleteFolderDialog && folderToDelete != null) {
        AppElevatedConfirmDialog(
            onDismiss = onDismissDeleteFolder,
            title = "删除文件夹",
            message = "确认删除 $folderToDelete 吗？",
            confirmLabel = "删除",
            dismissLabel = "取消",
            onConfirm = onConfirmDeleteFolder,
            confirmDestructive = true,
        )
    }
}

@Composable
private fun HomeInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val tokens = AppElevatedActionSheetTokens
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
            ) {
                Text(
                    text = "确定",
                    color = tokens.brandBlue,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消", color = tokens.textSecondary)
            }
        },
        title = {
            Text(
                text = title,
                color = tokens.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = true,
            )
        },
        shape = RoundedCornerShape(tokens.cardCorner),
        containerColor = tokens.cardWhite,
        tonalElevation = 4.dp,
    )
}
