package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
fun HomeFileListGrid(
    displayFileNames: List<String>,
    folders: Map<String, String?>,
    fileStatistics: Map<String, FileStatistics>,
    practiceProgress: Map<String, Int>,
    selectedFileName: String,
    draggingFile: String?,
    dragPosition: Offset,
    hoverFile: String?,
    shouldTrackDropTargets: Boolean,
    userScrollEnabled: Boolean,
    canKeepSwipeNodeStable: (String) -> Boolean,
    canHandleDrag: (String, Boolean) -> Boolean,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onReportCardBounds: (String, Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val autoScrollEdge = HomeFileListDragScroll.autoScrollEdge
    val autoScrollAmount = HomeFileListDragScroll.autoScrollAmount
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
            val scrollDelta = HomeFileListDragScroll.scrollDelta(
                dragY = pos.y,
                boundsTop = bounds.top,
                boundsBottom = bounds.bottom,
                edge = autoScrollEdge,
                amount = autoScrollAmount,
            )
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
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
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
                    showTypeSummary = true,
                    useCompactStyle = false,
                    enableDragDrop = canHandleDrag(fileName, isGridScrolling),
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
}
