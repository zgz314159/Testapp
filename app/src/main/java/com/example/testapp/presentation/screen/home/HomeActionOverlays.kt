package com.example.testapp.presentation.screen.home

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.home.components.HomeDraggingFileOverlay
import com.example.testapp.uicommon.design.AppLoadingOverlay
import com.example.testapp.uicommon.design.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeActionOverlays(
    context: Context,
    viewModel: HomeViewModel,
    isLoading: Boolean,
    importProgress: Float,
    draggingFile: String?,
    folders: Map<String, String>,
    dragPosition: Offset,
    dragOffset: Offset,
    dragItemSize: IntSize,
    showSheet: Boolean,
    pendingFileName: String,
    bottomNavIndex: Int,
    onDismissSheet: () -> Unit,
    onStartQuiz: (String) -> Unit,
    onStartExam: (String) -> Unit,
    onStartWrongBookQuiz: (String) -> Unit,
    onStartWrongBookExam: (String) -> Unit,
    onStartFavoriteQuiz: (String) -> Unit,
    onStartFavoriteExam: (String) -> Unit,
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
    onConfirmDeleteFolder: () -> Unit
) {
    AppLoadingOverlay(visible = isLoading) {
        if (importProgress > 0f) {
            CircularProgressIndicator(progress = { importProgress.coerceIn(0f, 1f) })
        } else {
            CircularProgressIndicator()
        }
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = "正在处理，请稍后",
            style = MaterialTheme.typography.bodyMedium
        )
        if (importProgress > 0f) {
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            LinearProgressIndicator(
                progress = { importProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    draggingFile?.let { file ->
        HomeDraggingFileOverlay(
            viewModel = viewModel,
            fileName = file,
            folderDisplayName = folders[file],
            dragPosition = dragPosition,
            dragOffset = dragOffset,
            dragItemSize = dragItemSize,
            showTypeSummary = false,
            modifier = Modifier.graphicsLayer { scaleX = 0.75f; scaleY = 0.75f }
        )
    }

    if (showSheet) {
        HomeStartQuizSheet(
            visible = true,
            pendingFileName = pendingFileName,
            bottomNavIndex = bottomNavIndex,
            onDismiss = onDismissSheet,
            onStartQuiz = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onStartQuiz(name)
            },
            onStartExam = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onStartExam(name)
            },
            onStartWrongBookQuiz = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onStartWrongBookQuiz(name)
            },
            onStartWrongBookExam = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onStartWrongBookExam(name)
            },
            onStartFavoriteQuiz = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onStartFavoriteQuiz(name)
            },
            onStartFavoriteExam = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onStartFavoriteExam(name)
            }
        )
    }

    HomeDialogs(
        showDeleteDialog = showDeleteDialog,
        fileToDelete = fileToDelete,
        onDismissDeleteFile = onDismissDeleteFile,
        onConfirmDeleteFile = onConfirmDeleteFile,
        showAddFolderDialog = showAddFolderDialog,
        newFolderName = newFolderName,
        onNewFolderNameChange = onNewFolderNameChange,
        onDismissAddFolder = onDismissAddFolder,
        onConfirmAddFolder = onConfirmAddFolder,
        renameFolderTarget = renameFolderTarget,
        renameFolderName = renameFolderName,
        onRenameFolderNameChange = onRenameFolderNameChange,
        onDismissRenameFolder = onDismissRenameFolder,
        onConfirmRenameFolder = onConfirmRenameFolder,
        showDeleteFolderDialog = showDeleteFolderDialog,
        folderToDelete = folderToDelete,
        onDismissDeleteFolder = onDismissDeleteFolder,
        onConfirmDeleteFolder = onConfirmDeleteFolder
    )
}

@Composable
private fun HomeDialogs(
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
    onConfirmDeleteFolder: () -> Unit
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

private fun persistHomeSelection(context: Context, fileName: String, bottomNavIndex: Int) {
    kotlinx.coroutines.runBlocking {
        FontSettingsDataStore.markFileAsRecent(context, fileName)
        FontSettingsDataStore.setLastSelectedFile(context, fileName)
        FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
    }
}
