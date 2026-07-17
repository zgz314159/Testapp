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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import com.example.testapp.presentation.screen.home.HomeScrollFrameMonitor
import com.example.testapp.presentation.screen.home.HomeViewModel
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import com.example.testapp.uicommon.component.OptimizedFileCard
import com.example.testapp.uicommon.component.SwipeRevealActionBox
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun HomeFileListColumn(
    visibleFolders: List<String>, folderFileCounts: Map<String, Int>, displayFileNames: List<String>,
    folders: Map<String, String?>, fileStatistics: Map<String, FileStatistics>, practiceProgress: Map<String, Int>,
    showFilesFirst: Boolean, selectedFileName: String, draggingFile: String?, dragPosition: Offset,
    hoverFolder: String?, hoverFile: String?, shouldTrackDropTargets: Boolean, swipeRevealEnabled: Boolean,
    userScrollEnabled: Boolean, canKeepSwipeNodeStable: (String) -> Boolean, canHandleDrag: (String, Boolean) -> Boolean,
    onFolderClick: (String) -> Unit, onFolderLongPress: (String) -> Unit, onDeleteFolderClick: (String) -> Unit,
    onCardClick: (String) -> Unit, onDeleteClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit, onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit, onDragCancel: (String) -> Unit,
    onReportFolderBounds: (String, Rect) -> Unit, onReportCardBounds: (String, Rect) -> Unit,
    onFileCtaClick: ((String) -> Unit)? = null, headerContent: @Composable () -> Unit = {}, showHeader: Boolean = true,
    viewModel: HomeViewModel? = null, onPendingFileName: ((String) -> Unit)? = null, onShowSheet: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val refreshRate = androidx.compose.ui.platform.LocalView.current.display?.refreshRate ?: 60f
    val frameMonitor = remember(refreshRate) { HomeScrollFrameMonitor(refreshRate) }
    DisposableEffect(frameMonitor) {
        onDispose {
            val visible = listState.layoutInfo.visibleItemsInfo
            frameMonitor.stop(visible.firstOrNull()?.index ?: -1, visible.lastOrNull()?.index ?: -1)
        }
    }
    LaunchedEffect(listState, frameMonitor) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                val visible = listState.layoutInfo.visibleItemsInfo
                val first = visible.firstOrNull()?.index ?: -1
                val last = visible.lastOrNull()?.index ?: -1
                if (scrolling) {
                    frameMonitor.start(first, last, listState.layoutInfo.totalItemsCount)
                } else {
                    frameMonitor.stop(first, last)
                }
            }
    }
    val isListScrolling by remember { derivedStateOf { listState.isScrollInProgress } }
    val isScrollingRef = rememberUpdatedState(isListScrolling)
    var listBounds by remember { mutableStateOf<Rect?>(null) }
    val currentListBounds by rememberUpdatedState(listBounds)
    val currentListDragPosition by rememberUpdatedState(dragPosition)
    val currentListDraggingFile by rememberUpdatedState(draggingFile)
    LaunchedEffect(draggingFile) {
        while (currentListDraggingFile != null) {
            withFrameNanos { }
            val bounds = currentListBounds ?: continue
            val pos = currentListDragPosition
            val scrollDelta = HomeFileListDragScroll.scrollDelta(dragY = pos.y, boundsTop = bounds.top, boundsBottom = bounds.bottom)
            if (scrollDelta != 0f) listState.scrollBy(scrollDelta)
        }
    }
    val homeCardShape = RoundedCornerShape(20.dp)
    val homeCardElevation = CardDefaults.cardElevation(
        defaultElevation = HomeDesignTokens.questionCardElevation,
    )
    LazyColumn(
        modifier = modifier.fillMaxSize().onGloballyPositioned { coords -> listBounds = coords.boundsInRoot() },
        state = listState,
        userScrollEnabled = userScrollEnabled,
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        if (showHeader) {
            item(key = "home_header", contentType = "home_header") { headerContent() }
        }
        if (showFilesFirst) {
            homeFileListColumnFiles(
                displayFileNames, folders, fileStatistics, practiceProgress, selectedFileName, draggingFile,
                hoverFile, shouldTrackDropTargets, isScrollingRef, canKeepSwipeNodeStable, canHandleDrag,
                onCardClick, onDeleteClick, onDragStart, onDragUpdate, onDragEnd, onDragCancel,
                onReportCardBounds, onFileCtaClick, homeCardShape, homeCardElevation,
            )
            homeFileListColumnFolders(
                visibleFolders, folderFileCounts, hoverFolder, shouldTrackDropTargets, swipeRevealEnabled,
                onFolderClick, onFolderLongPress, onDeleteFolderClick, onReportFolderBounds,
            )
        } else {
            homeFileListColumnFolders(
                visibleFolders, folderFileCounts, hoverFolder, shouldTrackDropTargets, swipeRevealEnabled,
                onFolderClick, onFolderLongPress, onDeleteFolderClick, onReportFolderBounds,
            )
            homeFileListColumnFiles(
                displayFileNames, folders, fileStatistics, practiceProgress, selectedFileName, draggingFile,
                hoverFile, shouldTrackDropTargets, isScrollingRef, canKeepSwipeNodeStable, canHandleDrag,
                onCardClick, onDeleteClick, onDragStart, onDragUpdate, onDragEnd, onDragCancel,
                onReportCardBounds, onFileCtaClick, homeCardShape, homeCardElevation,
            )
        }
    }
}

private fun LazyListScope.homeFileListColumnFiles(
    displayFileNames: List<String>, folders: Map<String, String?>, fileStatistics: Map<String, FileStatistics>,
    practiceProgress: Map<String, Int>, selectedFileName: String, draggingFile: String?, hoverFile: String?,
    shouldTrackDropTargets: Boolean,
    isScrollingRef: androidx.compose.runtime.State<Boolean>,
    canKeepSwipeNodeStable: (String) -> Boolean, canHandleDrag: (String, Boolean) -> Boolean,
    onCardClick: (String) -> Unit, onDeleteClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit, onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit, onDragCancel: (String) -> Unit, onReportCardBounds: (String, Rect) -> Unit,
    onFileCtaClick: ((String) -> Unit)?, cardShape: RoundedCornerShape,
    cardElev: androidx.compose.material3.CardElevation,
) {
    items(items = displayFileNames, key = { it }, contentType = { "file_card" }) { fileName ->
        val isScrolling = isScrollingRef.value
        val fileStats = fileStatistics[fileName] ?: FileStatistics()
        val progressCount = practiceProgress[fileName] ?: 0
        val qc = fileStats.questionCount
        val pct = if (qc > 0) (progressCount * 100 / qc).coerceIn(0, 100) else 0
        val dn = remember(fileName) { HomeDashboardPipeline.cleanupDisplayName(fileName) }
        SwipeRevealActionBox(
            enabled = !isScrolling && canKeepSwipeNodeStable(fileName),
            modifier = Modifier.fillMaxWidth().then(
                if (shouldTrackDropTargets) {
                    Modifier.onGloballyPositioned { coords -> onReportCardBounds(fileName, coords.boundsInRoot()) }
                } else {
                    Modifier
                },
            ),
            background = { ca -> HomeFileListSwipeDeleteBackground(fileName, { onDeleteClick(fileName) }, ca) },
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
                useCompactStyle = false,
                // 不把 isScrolling 编进 enableDragDrop，避免滚动时重建 pointerInput
                enableDragDrop = !isScrolling && canHandleDrag(fileName, false),
                allowDragStart = { canHandleDrag(fileName, isScrollingRef.value) },
                enableLongClickAction = false,
                cardShapeOverride = cardShape,
                cardContainerColorOverride = Color.Transparent,
                cardElevationOverride = cardElev,
                cardOuterPaddingOverride = PaddingValues(horizontal = 24.dp, vertical = 5.dp),
                visualContent = {
                    HomeQuestionBankCard(
                        displayName = dn,
                        fileName = fileName,
                        progressPercent = pct,
                        questionCount = qc,
                        wrongCount = fileStats.wrongCount,
                        favoriteCount = fileStats.favoriteCount,
                        statistics = fileStats,
                        onCtaClick = { onFileCtaClick?.invoke(fileName) },
                    )
                },
                onCardClick = { onCardClick(fileName) },
                onLongClick = null,
                onDoubleClick = null,
                onDragStart = { pos, sz, off -> onDragStart(fileName, pos, sz, off) },
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
            modifier = Modifier.fillMaxWidth().then(
                if (shouldTrackDropTargets) {
                    Modifier.onGloballyPositioned { coords -> onReportFolderBounds(folderName, coords.boundsInRoot()) }
                } else {
                    Modifier
                },
            ),
            background = { ca -> HomeFileListSwipeDeleteBackground(folderName, { onDeleteFolderClick(folderName) }, ca) },
        ) {
            HomeFolderCard(
                folderName,
                folderFileCounts[folderName] ?: 0,
                isDropTarget = hoverFolder == folderName,
                onClick = { onFolderClick(folderName) },
                onLongClick = { onFolderLongPress(folderName) },
                onReportBounds = { },
            )
        }
    }
}
