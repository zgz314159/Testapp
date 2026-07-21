package com.example.testapp.presentation.screen.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import com.example.testapp.presentation.screen.home.model.HomeQuestionBankCardModel
import com.example.testapp.presentation.screen.home.model.HomeQuestionBankCardModelPipeline

@Composable
fun HomeFileList(
    visibleFolders: List<String>,
    folderFileCounts: Map<String, Int>,
    fileCards: List<HomeQuestionBankCardModel>,
    folders: Map<String, String?>,
    enableItemGestures: Boolean = true,
    preferEagerCompose: Boolean = true,
    selectedFileName: String,
    draggingFile: String?,
    dragPosition: Offset,
    hoverFolder: String?,
    hoverFile: String?,
    useGridLayout: Boolean = false,
    showFilesFirst: Boolean = true,
    cardLayout: HomeDashboardPipeline.QuestionBankCardLayout =
        if (useGridLayout) {
            HomeDashboardPipeline.QuestionBankCardLayout.Dense
        } else {
            HomeDashboardPipeline.QuestionBankCardLayout.Wide
        },
    onFolderClick: (String) -> Unit,
    onFolderLongPress: (String) -> Unit,
    onDeleteFolderClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDoubleClick: (String) -> Unit,
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
    val shouldTrackDropTargets = draggingFile != null
    val swipeRevealEnabled = enableItemGestures && draggingFile == null
    var dragScrollLocked by remember { mutableStateOf(false) }
    val userScrollEnabled = draggingFile == null && !dragScrollLocked

    LaunchedEffect(draggingFile) {
        if (draggingFile == null) dragScrollLocked = false
    }

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    val currentEnableItemGestures by rememberUpdatedState(enableItemGestures)
    val currentDraggingFile by rememberUpdatedState(draggingFile)
    val currentSwipeRevealEnabled by rememberUpdatedState(swipeRevealEnabled)

    val wrapDragStart = remember<(String, Offset, IntSize, Offset) -> Unit> {
        { fileName, pos, size, offset ->
            dragScrollLocked = true
            currentOnDragStart(fileName, pos, size, offset)
        }
    }
    val wrapDragEnd = remember<(String) -> Unit> {
        { fileName ->
            dragScrollLocked = false
            currentOnDragEnd(fileName)
        }
    }
    val wrapDragCancel = remember<(String) -> Unit> {
        { fileName ->
            dragScrollLocked = false
            currentOnDragCancel(fileName)
        }
    }
    val canKeepSwipeNodeStable = remember<(String) -> Boolean> {
        { _ -> currentSwipeRevealEnabled }
    }
    val canHandleDrag = remember<(String, Boolean) -> Boolean> {
        { fileName, isScrolling ->
            currentEnableItemGestures &&
                (currentDraggingFile == fileName || (currentDraggingFile == null && !isScrolling))
        }
    }

    if (useGridLayout) {
        HomeFileListGrid(
            fileCards = fileCards,
            folders = folders,
            selectedFileName = selectedFileName,
            draggingFile = draggingFile,
            hoverFile = hoverFile,
            userScrollEnabled = userScrollEnabled,
            cardLayout = cardLayout,
            canHandleDrag = canHandleDrag,
            onCardClick = onCardClick,
            onDragStart = wrapDragStart,
            onDragUpdate = onDragUpdate,
            onDragEnd = wrapDragEnd,
            onDragCancel = wrapDragCancel,
            onFileCtaClick = onFileCtaClick,
            headerContent = headerContent,
            showHeader = showHeader,
            modifier = modifier,
        )
    } else {
        HomeFileListColumn(
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
            preferEagerCompose = preferEagerCompose,
            cardLayout = cardLayout,
            canKeepSwipeNodeStable = canKeepSwipeNodeStable,
            canHandleDrag = canHandleDrag,
            onFolderClick = onFolderClick,
            onFolderLongPress = onFolderLongPress,
            onDeleteFolderClick = onDeleteFolderClick,
            onCardClick = onCardClick,
            onDeleteClick = onDeleteClick,
            onDragStart = wrapDragStart,
            onDragUpdate = onDragUpdate,
            onDragEnd = wrapDragEnd,
            onDragCancel = wrapDragCancel,
            onReportFolderBounds = onReportFolderBounds,
            onReportCardBounds = onReportCardBounds,
            onFileCtaClick = onFileCtaClick,
            headerContent = headerContent,
            showHeader = showHeader,
            modifier = modifier,
        )
    }
}

/** 错题库/收藏库兼容入口：在调用点现场聚合成 card model。 */
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
    showFilesFirst: Boolean = true,
    onFolderClick: (String) -> Unit,
    onFolderLongPress: (String) -> Unit,
    onDeleteFolderClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDoubleClick: (String) -> Unit,
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
    val fileCards = remember(displayFileNames, fileStatistics, practiceProgress) {
        HomeQuestionBankCardModelPipeline.buildList(
            fileNames = displayFileNames,
            fileStatistics = fileStatistics,
            practiceProgress = practiceProgress,
        )
    }
    HomeFileList(
        visibleFolders = visibleFolders,
        folderFileCounts = folderFileCounts,
        fileCards = fileCards,
        folders = folders,
        enableItemGestures = enableItemGestures,
        preferEagerCompose = true,
        selectedFileName = selectedFileName,
        draggingFile = draggingFile,
        dragPosition = dragPosition,
        hoverFolder = hoverFolder,
        hoverFile = hoverFile,
        useGridLayout = useGridLayout,
        showFilesFirst = showFilesFirst,
        cardLayout = if (useGridLayout) {
            HomeDashboardPipeline.QuestionBankCardLayout.Dense
        } else {
            HomeDashboardPipeline.QuestionBankCardLayout.Wide
        },
        onFolderClick = onFolderClick,
        onFolderLongPress = onFolderLongPress,
        onDeleteFolderClick = onDeleteFolderClick,
        onCardClick = onCardClick,
        onDeleteClick = onDeleteClick,
        onDoubleClick = onDoubleClick,
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
}
