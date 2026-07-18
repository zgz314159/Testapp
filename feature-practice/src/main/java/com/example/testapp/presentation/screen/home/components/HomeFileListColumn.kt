package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import com.example.testapp.presentation.screen.home.HomePerformanceLog
import com.example.testapp.presentation.screen.home.HomeScrollFrameMonitor
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import com.example.testapp.presentation.screen.home.model.HomeQuestionBankCardModel
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeFileListColumn(
    visibleFolders: List<String>,
    folderFileCounts: Map<String, Int>,
    fileCards: List<HomeQuestionBankCardModel>,
    folders: Map<String, String?>,
    showFilesFirst: Boolean,
    selectedFileName: String,
    draggingFile: String?,
    dragPosition: Offset,
    hoverFolder: String?,
    hoverFile: String?,
    shouldTrackDropTargets: Boolean,
    swipeRevealEnabled: Boolean,
    userScrollEnabled: Boolean,
    preferEagerCompose: Boolean,
    cardLayout: HomeDashboardPipeline.QuestionBankCardLayout,
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
    onFileCtaClick: ((String) -> Unit)? = null,
    headerContent: @Composable () -> Unit = {},
    showHeader: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val fileNamesKey = remember(fileCards) { fileCards.map { it.fileName } }
    val folderNamesKey = remember(visibleFolders) { visibleFolders.toList() }
    // 冷启动仍走已经验证流畅的 Eager 路径；仅 HomeViewModel 确认的返回走 Lazy。
    val useEagerCompose = preferEagerCompose &&
        HomeQuestionBankCachePolicy.shouldEagerCompose(
            fileCount = fileCards.size,
            folderCount = visibleFolders.size,
        )
    if (useEagerCompose) {
        HomeFileListEagerColumn(
            visibleFolders = visibleFolders,
            folderFileCounts = folderFileCounts,
            fileCards = fileCards,
            folders = folders,
            showFilesFirst = showFilesFirst,
            selectedFileName = selectedFileName,
            draggingFile = draggingFile,
            dragPosition = dragPosition,
            hoverFolder = hoverFolder,
            hoverFile = hoverFile,
            shouldTrackDropTargets = shouldTrackDropTargets,
            swipeRevealEnabled = swipeRevealEnabled,
            userScrollEnabled = userScrollEnabled,
            cardLayout = cardLayout,
            canKeepSwipeNodeStable = canKeepSwipeNodeStable,
            canHandleDrag = canHandleDrag,
            onFolderClick = onFolderClick,
            onFolderLongPress = onFolderLongPress,
            onDeleteFolderClick = onDeleteFolderClick,
            onCardClick = onCardClick,
            onDeleteClick = onDeleteClick,
            onDragStart = onDragStart,
            onDragUpdate = onDragUpdate,
            onDragEnd = onDragEnd,
            onDragCancel = onDragCancel,
            onReportFolderBounds = onReportFolderBounds,
            onReportCardBounds = onReportCardBounds,
            onFileCtaClick = onFileCtaClick,
            headerContent = headerContent,
            showHeader = showHeader,
            modifier = modifier,
        )
        return
    }

    LaunchedEffect(preferEagerCompose, fileNamesKey, folderNamesKey) {
        if (!preferEagerCompose) {
            HomePerformanceLog.event(
                "home_list_return_lazy files=${fileNamesKey.size} folders=${folderNamesKey.size}",
            )
        }
    }
    val listState = rememberLazyListState(cacheWindow = HomeQuestionBankCacheWindow)
    val scrollFlag = remember { HomeScrollProgressFlag() }
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
                scrollFlag.value = scrolling
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
    val isScrolling = remember(scrollFlag) { { scrollFlag.value } }
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
            if (scrollDelta != 0f) listState.scrollBy(scrollDelta)
        }
    }
    val homeCardShape = RoundedCornerShape(20.dp)
    val homeCardElevation = CardDefaults.cardElevation(
        defaultElevation = HomeDesignTokens.elevationLow,
    )
    LazyColumn(
        modifier = modifier.fillMaxSize().onGloballyPositioned { coords ->
            listBounds = coords.boundsInRoot()
        },
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
                fileCards, folders, selectedFileName, draggingFile,
                hoverFile, shouldTrackDropTargets, isScrolling, cardLayout,
                canKeepSwipeNodeStable, canHandleDrag,
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
                fileCards, folders, selectedFileName, draggingFile,
                hoverFile, shouldTrackDropTargets, isScrolling, cardLayout,
                canKeepSwipeNodeStable, canHandleDrag,
                onCardClick, onDeleteClick, onDragStart, onDragUpdate, onDragEnd, onDragCancel,
                onReportCardBounds, onFileCtaClick, homeCardShape, homeCardElevation,
            )
        }
    }
}

private fun LazyListScope.homeFileListColumnFiles(
    fileCards: List<HomeQuestionBankCardModel>,
    folders: Map<String, String?>,
    selectedFileName: String,
    draggingFile: String?,
    hoverFile: String?,
    shouldTrackDropTargets: Boolean,
    isScrolling: () -> Boolean,
    cardLayout: HomeDashboardPipeline.QuestionBankCardLayout,
    canKeepSwipeNodeStable: (String) -> Boolean,
    canHandleDrag: (String, Boolean) -> Boolean,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onReportCardBounds: (String, Rect) -> Unit,
    onFileCtaClick: ((String) -> Unit)?,
    cardShape: RoundedCornerShape,
    cardElev: androidx.compose.material3.CardElevation,
) {
    items(items = fileCards, key = { it.fileName }, contentType = { "file_card" }) { card ->
        HomeFileListFileRow(
            card = card,
            folders = folders,
            selectedFileName = selectedFileName,
            draggingFile = draggingFile,
            hoverFile = hoverFile,
            shouldTrackDropTargets = shouldTrackDropTargets,
            isScrolling = isScrolling,
            cardLayout = cardLayout,
            canKeepSwipeNodeStable = canKeepSwipeNodeStable,
            canHandleDrag = canHandleDrag,
            onCardClick = onCardClick,
            onDeleteClick = onDeleteClick,
            onDragStart = onDragStart,
            onDragUpdate = onDragUpdate,
            onDragEnd = onDragEnd,
            onDragCancel = onDragCancel,
            onReportCardBounds = onReportCardBounds,
            onFileCtaClick = onFileCtaClick,
            cardShape = cardShape,
            cardElev = cardElev,
        )
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
        HomeFileListFolderRow(
            folderName = folderName,
            fileCount = folderFileCounts[folderName] ?: 0,
            hoverFolder = hoverFolder,
            shouldTrackDropTargets = shouldTrackDropTargets,
            swipeRevealEnabled = swipeRevealEnabled,
            onFolderClick = onFolderClick,
            onFolderLongPress = onFolderLongPress,
            onDeleteFolderClick = onDeleteFolderClick,
            onReportFolderBounds = onReportFolderBounds,
        )
    }
}
