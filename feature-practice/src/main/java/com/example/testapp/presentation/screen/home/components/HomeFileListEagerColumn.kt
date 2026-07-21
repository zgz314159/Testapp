package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.testapp.presentation.screen.home.HomeListBoundsRef
import com.example.testapp.presentation.screen.home.HomePerformanceLog
import com.example.testapp.presentation.screen.home.HomeScrollFrameMonitor
import com.example.testapp.presentation.screen.home.model.HomeQuestionBankCardModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

private const val FillStepDelayMs = 24L
private const val FillStepFiles = 3

/**
 * 中等规模题库非 Lazy 滚动。
 * - 禁止 composition 订阅 isScrollInProgress（Google defer-reads）
 * - 首帧先组合约一屏，其余按 delay 节奏补齐（避免一次组合造成主页空白）
 * - 不做可见 scrollTo 预热（会把列表滚走导致空白闪烁）
 */
@Composable
internal fun HomeFileListEagerColumn(
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
    val scrollState = rememberScrollState()
    val scrollFlag = remember { HomeScrollProgressFlag() }
    val refreshRate = androidx.compose.ui.platform.LocalView.current.display?.refreshRate ?: 60f
    val frameMonitor = remember(refreshRate) { HomeScrollFrameMonitor(refreshRate) }
    val totalItems = fileCards.size + visibleFolders.size + if (showHeader) 1 else 0
    DisposableEffect(frameMonitor) {
        onDispose { frameMonitor.stop(0, totalItems - 1) }
    }
    LaunchedEffect(scrollState, frameMonitor, totalItems) {
        snapshotFlow { scrollState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { scrolling ->
                scrollFlag.value = scrolling
                if (scrolling) {
                    frameMonitor.start(0, totalItems - 1, totalItems)
                } else {
                    frameMonitor.stop(0, totalItems - 1)
                }
            }
    }
    val fileNamesKey = remember(fileCards) { fileCards.map { it.fileName } }
    val folderNamesKey = remember(visibleFolders) { visibleFolders.toList() }
    // 渐进补齐只服务冷启动首帧。首次补齐完成后列表再变化（拖拽合并/入文件夹）
    // 必须全量组合：否则内容塌缩到一屏高，ScrollState 被钳制到 0，页面跳回顶端。
    val initialFillDone = remember { mutableStateOf(false) }
    var composedFileCount by remember(fileNamesKey) {
        mutableIntStateOf(
            if (initialFillDone.value) {
                fileNamesKey.size
            } else {
                fileNamesKey.size.coerceAtMost(HomeQuestionBankCachePolicy.InitialPaintCount)
            },
        )
    }
    var composedFolderCount by remember(folderNamesKey, fileNamesKey) {
        mutableIntStateOf(
            when {
                initialFillDone.value -> folderNamesKey.size
                fileNamesKey.size <= HomeQuestionBankCachePolicy.InitialPaintCount ->
                    folderNamesKey.size.coerceAtMost(2)
                else -> 0
            },
        )
    }
    LaunchedEffect(fileNamesKey, folderNamesKey) {
        HomePerformanceLog.event(
            "eager_compose files=${fileNamesKey.size} folders=${folderNamesKey.size} " +
                "firstPaint=$composedFileCount",
        )
        // 用 delay 而非 withFrameNanos：首页静止时 Compose 不产帧，
        // withFrameNanos 会挂起导致剩余卡直到用户滑动才补组合。
        // 正在滑动时暂停补齐，避免补齐组合帧与滑动帧相撞掉帧。
        while (composedFileCount < fileNamesKey.size) {
            delay(FillStepDelayMs)
            if (scrollFlag.value) continue
            composedFileCount =
                (composedFileCount + FillStepFiles).coerceAtMost(fileNamesKey.size)
        }
        while (composedFolderCount < folderNamesKey.size) {
            delay(FillStepDelayMs)
            if (scrollFlag.value) continue
            composedFolderCount =
                (composedFolderCount + 1).coerceAtMost(folderNamesKey.size)
        }
        initialFillDone.value = true
        HomePerformanceLog.event(
            "eager_compose_done files=$composedFileCount folders=$composedFolderCount",
        )
    }
    val listBoundsRef = remember { HomeListBoundsRef() }
    val currentListDragPosition by rememberUpdatedState(dragPosition)
    val currentListDraggingFile by rememberUpdatedState(draggingFile)
    LaunchedEffect(draggingFile) {
        while (currentListDraggingFile != null) {
            withFrameNanos { }
            val bounds = listBoundsRef.value ?: continue
            val pos = currentListDragPosition
            val scrollDelta = HomeFileListDragScroll.scrollDelta(
                dragY = pos.y,
                boundsTop = bounds.top,
                boundsBottom = bounds.bottom,
            )
            if (scrollDelta != 0f) scrollState.scrollBy(scrollDelta)
        }
    }
    val isScrolling = remember(scrollFlag) { { scrollFlag.value } }
    Column(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords -> listBoundsRef.value = coords.boundsInRoot() }
            .verticalScroll(scrollState, enabled = userScrollEnabled)
            .padding(bottom = 120.dp),
    ) {
        if (showHeader) {
            headerContent()
        }
        val visibleFiles = remember(fileCards, composedFileCount) {
            fileCards.take(composedFileCount)
        }
        val visibleFolderNames = remember(visibleFolders, composedFolderCount) {
            visibleFolders.take(composedFolderCount)
        }
        if (showFilesFirst) {
            HomeFileListEagerFiles(
                visibleFiles, folders, selectedFileName, draggingFile, hoverFile,
                shouldTrackDropTargets, isScrolling, cardLayout,
                canKeepSwipeNodeStable, canHandleDrag,
                onCardClick, onDeleteClick, onDragStart, onDragUpdate, onDragEnd, onDragCancel,
                onReportCardBounds, onFileCtaClick,
            )
            HomeFileListEagerFolders(
                visibleFolderNames, folderFileCounts, hoverFolder, shouldTrackDropTargets,
                swipeRevealEnabled, onFolderClick, onFolderLongPress, onDeleteFolderClick,
                onReportFolderBounds,
            )
        } else {
            HomeFileListEagerFolders(
                visibleFolderNames, folderFileCounts, hoverFolder, shouldTrackDropTargets,
                swipeRevealEnabled, onFolderClick, onFolderLongPress, onDeleteFolderClick,
                onReportFolderBounds,
            )
            HomeFileListEagerFiles(
                visibleFiles, folders, selectedFileName, draggingFile, hoverFile,
                shouldTrackDropTargets, isScrolling, cardLayout,
                canKeepSwipeNodeStable, canHandleDrag,
                onCardClick, onDeleteClick, onDragStart, onDragUpdate, onDragEnd, onDragCancel,
                onReportCardBounds, onFileCtaClick,
            )
        }
    }
}

@Composable
private fun HomeFileListEagerFiles(
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
) {
    fileCards.forEach { card ->
        key("file:${card.fileName}") {
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
            )
        }
    }
}

@Composable
private fun HomeFileListEagerFolders(
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
    visibleFolders.forEach { folderName ->
        key("folder:$folderName") {
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
}
