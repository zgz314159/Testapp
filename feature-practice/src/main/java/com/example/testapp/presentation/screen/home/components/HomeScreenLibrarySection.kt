package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.home.HomeEmptyLibraryPanel
import com.example.testapp.presentation.screen.home.HomeLibraryEmptyReason
import com.example.testapp.presentation.screen.home.HomeViewModel

@Composable
fun HomeScreenLibrarySection(
    currentFolder: String?,
    displayFolders: List<String>,
    displayFileNames: List<String>,
    folderFileCounts: Map<String, Int>,
    folders: Map<String, String?>,
    homeContentReady: Boolean,
    homeLibraryEmptyReason: HomeLibraryEmptyReason?,
    viewModel: HomeViewModel,
    selectedFileName: String,
    draggingFile: String?,
    dragPosition: androidx.compose.ui.geometry.Offset?,
    hoverFolder: String?,
    hoverFile: String?,
    bottomNavIndex: Int,
    folderBounds: MutableMap<String, Rect>,
    fileCardBounds: MutableMap<String, Rect>,
    dragViewModel: DragDropViewModel,
    onBeforeDragStart: () -> Unit = {},
    homeDropTargetKey: String,
    currentFolderFileNames: Set<String>,
    onCurrentFolderChange: (String?) -> Unit,
    onRenameFolderTarget: (String) -> Unit,
    onRenameFolderName: (String) -> Unit,
    onFolderToDelete: (String) -> Unit,
    onShowDeleteFolderDialog: () -> Unit,
    onSelectedFileNameChange: (String) -> Unit,
    onPendingFileName: (String) -> Unit,
    onShowSheet: () -> Unit,
    onFileToDelete: (String) -> Unit,
    onShowDeleteDialog: () -> Unit,
    onViewResult: (String) -> Unit,
    onViewQuestionDetail: (String) -> Unit,
    persistFileUsage: (String) -> Unit,
    onUpdateDragHover: (
        position: androidx.compose.ui.geometry.Offset,
        currentFolder: String?,
        homeDropTargetKey: String,
        currentFolderFileNames: Set<String>,
    ) -> Unit,
    onFinishDrag: (
        fileName: String,
        currentFolder: String?,
        homeDropTargetKey: String,
        currentFolderFileNames: Set<String>,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomeFolderRow(
            currentFolder = currentFolder,
            folderNames = emptyList(),
            hoverFolder = hoverFolder,
            showFolderList = false,
            showBackAction = true,
            onBackFolder = { onCurrentFolderChange(null) },
            onFolderClick = onCurrentFolderChange,
            onFolderLongPress = {
                onRenameFolderTarget(it)
                onRenameFolderName(it)
            }
        )

        if (homeContentReady && homeLibraryEmptyReason != null) {
            HomeEmptyLibraryPanel(
                reason = homeLibraryEmptyReason,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            )
        } else {
            HomeFileListContainer(
                viewModel = viewModel,
                visibleFolders = displayFolders,
                folderFileCounts = folderFileCounts,
                displayFileNames = displayFileNames,
                folders = folders,
                enableItemGestures = homeContentReady,
                selectedFileName = selectedFileName,
                draggingFile = draggingFile,
                dragPosition = dragPosition ?: Offset.Zero,
                hoverFolder = hoverFolder,
                hoverFile = hoverFile,
                showFilesFirst = true,
                onFolderClick = onCurrentFolderChange,
                onFolderLongPress = {
                    onRenameFolderTarget(it)
                    onRenameFolderName(it)
                },
                onDeleteFolderClick = {
                    onFolderToDelete(it)
                    onShowDeleteFolderDialog()
                },
                onCardClick = { fileName ->
                    when (bottomNavIndex) {
                        2 -> {
                            onSelectedFileNameChange(fileName)
                            persistFileUsage(fileName)
                            onViewResult(fileName)
                        }
                        else -> {
                            if (selectedFileName == fileName) {
                                onPendingFileName(fileName)
                                viewModel.preloadQuestionFile(fileName)
                                onShowSheet()
                            } else {
                                onSelectedFileNameChange(fileName)
                                viewModel.preloadQuestionFile(fileName)
                            }
                        }
                    }
                },
                onDeleteClick = { fileName ->
                    onFileToDelete(fileName)
                    onShowDeleteDialog()
                },
                onDoubleClick = { fileName ->
                    onSelectedFileNameChange(fileName)
                    persistFileUsage(fileName)
                    onViewQuestionDetail(fileName)
                },
                onDragStart = { fileName, position, size, offset ->
                    onBeforeDragStart()
                    dragViewModel.startDragging(fileName, position, size, offset)
                },
                onDragUpdate = { position ->
                    onUpdateDragHover(position, currentFolder, homeDropTargetKey, currentFolderFileNames)
                },
                onDragEnd = { fileName ->
                    onFinishDrag(fileName, currentFolder, homeDropTargetKey, currentFolderFileNames)
                },
                onDragCancel = { _ -> },
                onReportFolderBounds = { name, rect -> folderBounds[name] = rect },
                onReportCardBounds = { name, rect -> fileCardBounds[name] = rect },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 8.dp)
            )
        }
    }
}
