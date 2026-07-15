package com.example.testapp.presentation.screen.home

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntSize
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.home.components.HomeDraggingFileOverlay
import com.example.testapp.presentation.screen.home.components.HomeImportLoadingOverlay

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
    hasProgress: Boolean,
    bottomNavIndex: Int,
    onDismissSheet: () -> Unit,
    onStartQuiz: (String) -> Unit,
    onStartExam: (String) -> Unit,
    onRestartQuiz: (String) -> Unit,
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
    HomeImportLoadingOverlay(visible = isLoading, importProgress = importProgress)

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
            hasProgress = hasProgress,
            onDismiss = onDismissSheet,
            onStartQuiz = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onStartQuiz(name)
            },
            onStartExam = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onStartExam(name)
            },
            onRestart = { name ->
                persistHomeSelection(context, name, bottomNavIndex)
                onRestartQuiz(name)
            },
        )
    }

    HomeScreenDialogs(
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
        onConfirmDeleteFolder = onConfirmDeleteFolder,
    )
}

private fun persistHomeSelection(context: Context, fileName: String, bottomNavIndex: Int) {
    kotlinx.coroutines.runBlocking {
        FontSettingsDataStore.markFileAsRecent(context, fileName)
        FontSettingsDataStore.setLastSelectedFile(context, fileName)
        FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
    }
}
