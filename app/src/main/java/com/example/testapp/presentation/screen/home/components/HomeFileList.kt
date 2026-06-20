package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.example.testapp.presentation.component.OptimizedFileCard
import com.example.testapp.presentation.components.SwipeRevealActionBox
import com.example.testapp.domain.usecase.FileStatistics

@Composable
fun HomeFileList(
    visibleFolders: List<String>,
    folderFileCounts: Map<String, Int>,
    displayFileNames: List<String>,
    folders: Map<String, String?>,
    fileStatistics: Map<String, FileStatistics>,
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
    onDragStart: (String, androidx.compose.ui.geometry.Offset, androidx.compose.ui.unit.IntSize, androidx.compose.ui.geometry.Offset) -> Unit,
    onDragUpdate: (androidx.compose.ui.geometry.Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onReportFolderBounds: (String, Rect) -> Unit,
    onReportCardBounds: (String, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val autoScrollEdge = 96f
    val autoScrollAmount = 36f
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
        val gridState = rememberLazyGridState()
        val isGridScrolling by remember { derivedStateOf { gridState.isScrollInProgress } }
        var gridBounds by remember { mutableStateOf<Rect?>(null) }
        val currentGridBounds by rememberUpdatedState(gridBounds)
        val currentGridDragPosition by rememberUpdatedState(dragPosition)
        val currentGridDraggingFile by rememberUpdatedState(draggingFile)

        LaunchedEffect(draggingFile) {
            while (currentGridDraggingFile != null) {
                withFrameNanos { }
                val bounds = currentGridBounds ?: continue
                val pos = currentGridDragPosition
                val scrollDelta = when {
                    pos.y < bounds.top + autoScrollEdge -> -autoScrollAmount
                    pos.y > bounds.bottom - autoScrollEdge -> autoScrollAmount
                    else -> 0f
                }
                if (scrollDelta != 0f) {
                    gridState.scrollBy(scrollDelta)
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coords -> gridBounds = coords.boundsInRoot() },
            state = gridState,
            userScrollEnabled = userScrollEnabled,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
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
                                Modifier.onGloballyPositioned { coords -> onReportCardBounds(fileName, coords.boundsInRoot()) }
                            } else {
                                Modifier
                            }
                        ),
                    background = { closeAction ->
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .fillMaxHeight()
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            IconButton(onClick = {
                                closeAction(true)
                                onDeleteClick(fileName)
                            }, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = fileName,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                ) {
                    OptimizedFileCard(
                        fileName = fileName,
                        statistics = fileStats,
                        progressCount = progressCount,
                        isSelected = selectedFileName == fileName,
                        isDropTarget = hoverFile == fileName && draggingFile != fileName,
                        folderDisplayName = folders[fileName],
                        isDragging = draggingFile == fileName,
                        showTypeSummary = true,
                        useCompactStyle = false,
                        enableDragDrop = canHandleDrag(fileName, isGridScrolling),
                        enableLongClickAction = false,
                        onCardClick = { onCardClick(fileName) },
                        onLongClick = null,
                        onDoubleClick = null,
                        onDragStart = { pos, size, offset -> wrapDragStart(fileName, pos, size, offset) },
                        onDragUpdate = onDragUpdate,
                        onDragEnd = { wrapDragEnd(fileName) },
                        onDragCancel = { wrapDragCancel(fileName) }
                    )
                }
            }
        }
    } else {
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
                val scrollDelta = when {
                    pos.y < bounds.top + autoScrollEdge -> -autoScrollAmount
                    pos.y > bounds.bottom - autoScrollEdge -> autoScrollAmount
                    else -> 0f
                }
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
            verticalArrangement = Arrangement.Top
        ) {
            if (showFilesFirst) {
                items(items = displayFileNames, key = { it }, contentType = { "file_card" }) { fileName ->
                    val fileStats = fileStatistics[fileName] ?: FileStatistics()
                    val progressCount = practiceProgress[fileName] ?: 0
                    SwipeRevealActionBox(
                        enabled = canKeepSwipeNodeStable(fileName),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (shouldTrackDropTargets) {
                                    Modifier.onGloballyPositioned { coords -> onReportCardBounds(fileName, coords.boundsInRoot()) }
                                } else {
                                    Modifier
                                }
                            ),
                        background = { closeAction ->
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(onClick = {
                                    closeAction(true)
                                    onDeleteClick(fileName)
                                }, modifier = Modifier.size(48.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = fileName,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
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
                            onDragStart = { pos, size, offset -> wrapDragStart(fileName, pos, size, offset) },
                            onDragUpdate = onDragUpdate,
                            onDragEnd = { wrapDragEnd(fileName) },
                            onDragCancel = { wrapDragCancel(fileName) }
                        )
                    }
                }
                items(items = visibleFolders, key = { "folder_$it" }, contentType = { "folder_card" }) { folderName ->
                    SwipeRevealActionBox(
                        enabled = swipeRevealEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (shouldTrackDropTargets) {
                                    Modifier.onGloballyPositioned { coords -> onReportFolderBounds(folderName, coords.boundsInRoot()) }
                                } else {
                                    Modifier
                                }
                            ),
                        background = { closeAction ->
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(onClick = {
                                    closeAction(true)
                                    onDeleteFolderClick(folderName)
                                }, modifier = Modifier.size(48.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = folderName,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    ) {
                        HomeFolderCard(
                            folderName = folderName,
                            itemCount = folderFileCounts[folderName] ?: 0,
                            isDropTarget = hoverFolder == folderName,
                            onClick = { onFolderClick(folderName) },
                            onLongClick = { onFolderLongPress(folderName) },
                            onReportBounds = { }
                        )
                    }
                }
            } else {
                items(items = visibleFolders, key = { "folder_$it" }, contentType = { "folder_card" }) { folderName ->
                    SwipeRevealActionBox(
                        enabled = swipeRevealEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (shouldTrackDropTargets) {
                                    Modifier.onGloballyPositioned { coords -> onReportFolderBounds(folderName, coords.boundsInRoot()) }
                                } else {
                                    Modifier
                                }
                            ),
                        background = { closeAction ->
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(onClick = {
                                    closeAction(true)
                                    onDeleteFolderClick(folderName)
                                }, modifier = Modifier.size(48.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = folderName,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    ) {
                        HomeFolderCard(
                            folderName = folderName,
                            itemCount = folderFileCounts[folderName] ?: 0,
                            isDropTarget = hoverFolder == folderName,
                            onClick = { onFolderClick(folderName) },
                            onLongClick = { onFolderLongPress(folderName) },
                            onReportBounds = { }
                        )
                    }
                }

                items(items = displayFileNames, key = { it }, contentType = { "file_card" }) { fileName ->
                    val fileStats = fileStatistics[fileName] ?: FileStatistics()
                    val progressCount = practiceProgress[fileName] ?: 0
                    SwipeRevealActionBox(
                        enabled = canKeepSwipeNodeStable(fileName),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (shouldTrackDropTargets) {
                                    Modifier.onGloballyPositioned { coords -> onReportCardBounds(fileName, coords.boundsInRoot()) }
                                } else {
                                    Modifier
                                }
                            ),
                        background = { closeAction ->
                            Box(
                                modifier = Modifier
                                    .width(92.dp)
                                    .fillMaxHeight()
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                IconButton(onClick = {
                                    closeAction(true)
                                    onDeleteClick(fileName)
                                }, modifier = Modifier.size(48.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = fileName,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
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
                            onDragStart = { pos, size, offset -> wrapDragStart(fileName, pos, size, offset) },
                            onDragUpdate = onDragUpdate,
                            onDragEnd = { wrapDragEnd(fileName) },
                            onDragCancel = { wrapDragCancel(fileName) }
                        )
                    }
                }
            }
        }
    }
}
