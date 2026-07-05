package com.example.testapp.presentation.screen.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

@Composable
fun HomeFileList(
    visibleFolders: List<String>,
    folderFileCounts: Map<String, Int>,
    displayFileNames: List<String>,
    folders: Map<String, String?>,
    fileStatistics: Map<String, com.example.testapp.domain.usecase.FileStatistics>,
    practiceProgress: Map<String, Int>,
    enableItemGestures: Boolean = true,
    selectedFileName: String,
    draggingFile: String?,
    dragPosition: Offset,
    hoverFolder: String?,
    hoverFile: String?,
    useGridLayout: Boolean = false,
    showFilesFirst: Boolean = false,
    onFolderClick: (String) -> Unit,
    onFolderLongPress: (String) -> Unit,
    onDeleteFolderClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDoubleClick: (String) -> Unit,
    onDragStart:
    (String, Offset, IntSize, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onReportFolderBounds: (String, Rect) -> Unit,
    onReportCardBounds: (String, Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shouldTrackDropTargets = draggingFile != null
    val swipeRevealEnabled = enableItemGestures && draggingFile == null
    var dragScrollLocked by remember { mutableStateOf(false) }
    val userScrollEnabled = draggingFile == null && !dragScrollLocked

    LaunchedEffect(draggingFile) {
        if (draggingFile == null) dragScrollLocked = false
    }

    fun wrapDragStart(fileName: String, pos: Offset, size: IntSize, offset: Offset) {
        dragScrollLocked = true
        onDragStart(fileName, pos, size, offset)
    }

    fun wrapDragEnd(fileName: String) {
        dragScrollLocked = false
        onDragEnd(fileName)
    }

    fun wrapDragCancel(fileName: String) {
        dragScrollLocked = false
        onDragCancel(fileName)
    }

    fun canKeepSwipeNodeStable(fileName: String): Boolean = swipeRevealEnabled

    fun canHandleDrag(fileName: String, isScrolling: Boolean): Boolean {
        return enableItemGestures && (draggingFile == fileName || (draggingFile == null && !isScrolling))
    }

    if (useGridLayout) {
        HomeFileListGrid(
            displayFileNames = displayFileNames,
            folders = folders,
            fileStatistics = fileStatistics,
            practiceProgress = practiceProgress,
            selectedFileName = selectedFileName,
            draggingFile = draggingFile,
            dragPosition = dragPosition,
            hoverFile = hoverFile,
            shouldTrackDropTargets = shouldTrackDropTargets,
            userScrollEnabled = userScrollEnabled,
            canKeepSwipeNodeStable = ::canKeepSwipeNodeStable,
            canHandleDrag = ::canHandleDrag,
            onCardClick = onCardClick,
            onDeleteClick = onDeleteClick,
            onDragStart = ::wrapDragStart,
            onDragUpdate = onDragUpdate,
            onDragEnd = ::wrapDragEnd,
            onDragCancel = ::wrapDragCancel,
            onReportCardBounds = onReportCardBounds,
            modifier = modifier,
        )
    } else {
        HomeFileListColumn(
            visibleFolders = visibleFolders,
            folderFileCounts = folderFileCounts,
            displayFileNames = displayFileNames,
            folders = folders,
            fileStatistics = fileStatistics,
            practiceProgress = practiceProgress,
            showFilesFirst = showFilesFirst,
            selectedFileName = selectedFileName,
            draggingFile = draggingFile,
            dragPosition = dragPosition,
            hoverFolder = hoverFolder,
            hoverFile = hoverFile,
            shouldTrackDropTargets = shouldTrackDropTargets,
            swipeRevealEnabled = swipeRevealEnabled,
            userScrollEnabled = userScrollEnabled,
            canKeepSwipeNodeStable = ::canKeepSwipeNodeStable,
            canHandleDrag = ::canHandleDrag,
            onFolderClick = onFolderClick,
            onFolderLongPress = onFolderLongPress,
            onDeleteFolderClick = onDeleteFolderClick,
            onCardClick = onCardClick,
            onDeleteClick = onDeleteClick,
            onDragStart = ::wrapDragStart,
            onDragUpdate = onDragUpdate,
            onDragEnd = ::wrapDragEnd,
            onDragCancel = ::wrapDragCancel,
            onReportFolderBounds = onReportFolderBounds,
            onReportCardBounds = onReportCardBounds,
            modifier = modifier,
        )
    }
}
