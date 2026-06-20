package com.example.testapp.presentation.screen.home

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.home.components.HomeDraggingFileOverlay
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize

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
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(progress = { importProgress })
                Spacer(Modifier.height(8.dp))
                Text(
                    "正在处理，请稍后",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
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
        ModalBottomSheet(onDismissRequest = onDismissSheet) {
            Column(
                Modifier
                    .padding(top = 16.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(pendingFileName, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        onDismissSheet()
                        persistHomeSelection(context, pendingFileName, bottomNavIndex)
                        when (bottomNavIndex) {
                            0 -> onStartWrongBookQuiz(pendingFileName)
                            1 -> onStartFavoriteQuiz(pendingFileName)
                            else -> onStartQuiz(pendingFileName)
                        }
                    },
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) { Text("开始练习") }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        onDismissSheet()
                        persistHomeSelection(context, pendingFileName, bottomNavIndex)
                        when (bottomNavIndex) {
                            0 -> onStartWrongBookExam(pendingFileName)
                            1 -> onStartFavoriteExam(pendingFileName)
                            else -> onStartExam(pendingFileName)
                        }
                    },
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) { Text("开始考试") }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onDismissSheet, Modifier.fillMaxWidth()) { Text("取消") }
            }
        }
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
