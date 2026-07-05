package com.example.testapp.presentation.screen.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
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
        AlertDialog(
            onDismissRequest = onDismissDeleteFile,
            confirmButton = { TextButton(onClick = onConfirmDeleteFile) { Text("确定") } },
            dismissButton = { TextButton(onClick = onDismissDeleteFile) { Text("取消") } },
            text = { Text("确认删除 $fileToDelete 及其相关数据？") }
        )
    }
    if (showAddFolderDialog) {
        AlertDialog(
            onDismissRequest = onDismissAddFolder,
            confirmButton = { TextButton(onClick = onConfirmAddFolder) { Text("确定") } },
            dismissButton = { TextButton(onClick = onDismissAddFolder) { Text("取消") } },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = onNewFolderNameChange,
                    label = { Text("文件夹名") }
                )
            }
        )
    }
    if (renameFolderTarget != null) {
        AlertDialog(
            onDismissRequest = onDismissRenameFolder,
            confirmButton = { TextButton(onClick = onConfirmRenameFolder) { Text("确定") } },
            dismissButton = { TextButton(onClick = onDismissRenameFolder) { Text("取消") } },
            text = {
                OutlinedTextField(
                    value = renameFolderName,
                    onValueChange = onRenameFolderNameChange,
                    label = { Text("重命名") }
                )
            }
        )
    }
    if (showDeleteFolderDialog && folderToDelete != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteFolder,
            confirmButton = { TextButton(onClick = onConfirmDeleteFolder) { Text("确定") } },
            dismissButton = { TextButton(onClick = onDismissDeleteFolder) { Text("取消") } },
            text = { Text("确认删除 $folderToDelete 吗？") }
        )
    }
}
