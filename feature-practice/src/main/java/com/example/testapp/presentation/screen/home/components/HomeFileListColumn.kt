package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.uicommon.component.OptimizedFileCard
import com.example.testapp.uicommon.component.SwipeRevealActionBox

@Composable
fun HomeFileListColumn(
    visibleFolders: List<String>,
    folderFileCounts: Map<String, Int>,
    displayFileNames: List<String>,
    folders: Map<String, String?>,
    fileStatistics: Map<String, FileStatistics>,
    practiceProgress: Map<String, Int>,
    showFilesFirst: Boolean,
    selectedFileName: String,
    draggingFile: String?,
    dragPosition: Offset,
    hoverFolder: String?,
    hoverFile: String?,
    shouldTrackDropTargets: Boolean,
    swipeRevealEnabled: Boolean,
    userScrollEnabled: Boolean,
    canKeepSwipeNodeStable: (String) -> Boolean,
    canHandleDrag: (String, Boolean) -> Boolean,
    onFolderClick: (String) -> Unit,
    onFolderLongPress: (String) -> Unit,
    onDeleteFolderClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onReportFolderBounds: (String, Rect) -> Unit,
    onReportCardBounds: (String, Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val isListScrolling by remember { derivedStateOf { listState.isScrollInProgress } }
    var listBounds by remember { mutableStateOf<Rect?>(null) }
    val currentListBounds by rememberUpdatedState(listBounds)
    val currentListDragPosition by rememberUpdatedState(dragPosition)
    val currentListDraggingFile by rememberUpdatedState(draggingFile)

    LaunchedEffect(draggingFile) {
        while (currentListDraggingFile != null) {
            withFrameNanos { }
            val bounds = currentListBounds ?: continue
            val pos = currentListDragPosition
            val scrollDelta = HomeFileListDragScroll.scrollDelta(
                dragY = pos.y,
                boundsTop = bounds.top,
                boundsBottom = bounds.bottom,
            )
            if (scrollDelta != 0f) {
                listState.scrollBy(scrollDelta)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords -> listBounds = coords.boundsInRoot() },
        state = listState,
        userScrollEnabled = userScrollEnabled,
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        if (showFilesFirst) {
            homeFileListColumnFiles(
                displayFileNames = displayFileNames,
                folders = folders,
                fileStatistics = fileStatistics,
                practiceProgress = practiceProgress,
                selectedFileName = selectedFileName,
                draggingFile = draggingFile,
                hoverFile = hoverFile,
                shouldTrackDropTargets = shouldTrackDropTargets,
                isListScrolling = isListScrolling,
                canKeepSwipeNodeStable = canKeepSwipeNodeStable,
                canHandleDrag = canHandleDrag,
                onCardClick = onCardClick,
                onDeleteClick = onDeleteClick,
                onDragStart = onDragStart,
                onDragUpdate = onDragUpdate,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onReportCardBounds = onReportCardBounds,
            )
            homeFileListColumnFolders(
                visibleFolders = visibleFolders,
                folderFileCounts = folderFileCounts,
                hoverFolder = hoverFolder,
                shouldTrackDropTargets = shouldTrackDropTargets,
                swipeRevealEnabled = swipeRevealEnabled,
                onFolderClick = onFolderClick,
                onFolderLongPress = onFolderLongPress,
                onDeleteFolderClick = onDeleteFolderClick,
                onReportFolderBounds = onReportFolderBounds,
            )
        } else {
            homeFileListColumnFolders(
                visibleFolders = visibleFolders,
                folderFileCounts = folderFileCounts,
                hoverFolder = hoverFolder,
                shouldTrackDropTargets = shouldTrackDropTargets,
                swipeRevealEnabled = swipeRevealEnabled,
                onFolderClick = onFolderClick,
                onFolderLongPress = onFolderLongPress,
                onDeleteFolderClick = onDeleteFolderClick,
                onReportFolderBounds = onReportFolderBounds,
            )
            homeFileListColumnFiles(
                displayFileNames = displayFileNames,
                folders = folders,
                fileStatistics = fileStatistics,
                practiceProgress = practiceProgress,
                selectedFileName = selectedFileName,
                draggingFile = draggingFile,
                hoverFile = hoverFile,
                shouldTrackDropTargets = shouldTrackDropTargets,
                isListScrolling = isListScrolling,
                canKeepSwipeNodeStable = canKeepSwipeNodeStable,
                canHandleDrag = canHandleDrag,
                onCardClick = onCardClick,
                onDeleteClick = onDeleteClick,
                onDragStart = onDragStart,
                onDragUpdate = onDragUpdate,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onReportCardBounds = onReportCardBounds,
            )
        }
    }
}

private fun LazyListScope.homeFileListColumnFiles(
    displayFileNames: List<String>,
    folders: Map<String, String?>,
    fileStatistics: Map<String, FileStatistics>,
    practiceProgress: Map<String, Int>,
    selectedFileName: String,
    draggingFile: String?,
    hoverFile: String?,
    shouldTrackDropTargets: Boolean,
    isListScrolling: Boolean,
    canKeepSwipeNodeStable: (String) -> Boolean,
    canHandleDrag: (String, Boolean) -> Boolean,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onReportCardBounds: (String, Rect) -> Unit,
) {
    items(items = displayFileNames, key = { it }, contentType = { "file_card" }) { fileName ->
        val fileStats = fileStatistics[fileName] ?: FileStatistics()
        val progressCount = practiceProgress[fileName] ?: 0
        SwipeRevealActionBox(
            enabled = canKeepSwipeNodeStable(fileName),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (shouldTrackDropTargets) {
                        Modifier.onGloballyPositioned { coords ->
                            onReportCardBounds(fileName, coords.boundsInRoot())
                        }
                    } else {
                        Modifier
                    },
                ),
            background = { closeAction ->
                HomeFileListSwipeDeleteBackground(
                    label = fileName,
                    onDelete = { onDeleteClick(fileName) },
                    closeAction = closeAction,
                )
            },
        ) {
            OptimizedFileCard(
                fileName = fileName,
                statistics = fileStats,
                progressCount = progressCount,
                isSelected = selectedFileName == fileName,
                isDropTarget = hoverFile == fileName && draggingFile != fileName,
                folderDisplayName = folders[fileName],
                isDragging = draggingFile == fileName,
                showTypeSummary = false,
                useCompactStyle = true,
                enableDragDrop = canHandleDrag(fileName, isListScrolling),
                enableLongClickAction = false,
                onCardClick = { onCardClick(fileName) },
                onLongClick = null,
                onDoubleClick = null,
                onDragStart = { pos, size, offset -> onDragStart(fileName, pos, size, offset) },
                onDragUpdate = onDragUpdate,
                onDragEnd = { onDragEnd(fileName) },
                onDragCancel = { onDragCancel(fileName) },
            )
        }
    }
}

private fun LazyListScope.homeFileListColumnFolders(
    visibleFolders: List<String>,
    folderFileCounts: Map<String, Int>,
    hoverFolder: String?,
    shouldTrackDropTargets: Boolean,
    swipeRevealEnabled: Boolean,
    onFolderClick: (String) -> Unit,
    onFolderLongPress: (String) -> Unit,
    onDeleteFolderClick: (String) -> Unit,
    onReportFolderBounds: (String, Rect) -> Unit,
) {
    items(items = visibleFolders, key = { "folder_$it" }, contentType = { "folder_card" }) { folderName ->
        SwipeRevealActionBox(
            enabled = swipeRevealEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (shouldTrackDropTargets) {
                        Modifier.onGloballyPositioned { coords ->
                            onReportFolderBounds(folderName, coords.boundsInRoot())
                        }
                    } else {
                        Modifier
                    },
                ),
            background = { closeAction ->
                HomeFileListSwipeDeleteBackground(
                    label = folderName,
                    onDelete = { onDeleteFolderClick(folderName) },
                    closeAction = closeAction,
                )
            },
        ) {
            HomeFolderCard(
                folderName = folderName,
                itemCount = folderFileCounts[folderName] ?: 0,
                isDropTarget = hoverFolder == folderName,
                onClick = { onFolderClick(folderName) },
                onLongClick = { onFolderLongPress(folderName) },
                onReportBounds = { },
            )
        }
    }
}
