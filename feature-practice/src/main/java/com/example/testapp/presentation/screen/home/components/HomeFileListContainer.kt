package com.example.testapp.presentation.screen.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.example.testapp.presentation.screen.home.HomeViewModel
import com.example.testapp.presentation.screen.home.model.HomeQuestionBankCardModelPipeline

@Composable
fun HomeFileListContainer(
    viewModel: HomeViewModel,
    visibleFolders: List<String>,
    folderFileCounts: Map<String, Int>,
    displayFileNames: List<String>,
    folders: Map<String, String?>,
    enableItemGestures: Boolean,
    preferEagerCompose: Boolean = true,
    selectedFileName: String,
    draggingFile: String?,
    dragPosition: Offset,
    hoverFolder: String?,
    hoverFile: String?,
    useGridLayout: Boolean = false,
    showFilesFirst: Boolean = true,
    cardLayout: com.example.testapp.presentation.screen.home.HomeDashboardPipeline.QuestionBankCardLayout,
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
    modifier: Modifier = Modifier
) {
    val homeContent by viewModel.contentState.collectAsState()
    val fileCards = remember(
        displayFileNames,
        homeContent.fileStatistics,
        homeContent.practiceProgress,
    ) {
        HomeQuestionBankCardModelPipeline.buildList(
            fileNames = displayFileNames,
            fileStatistics = homeContent.fileStatistics,
            practiceProgress = homeContent.practiceProgress,
        )
    }

    HomeFileList(
        visibleFolders = visibleFolders,
        folderFileCounts = folderFileCounts,
        fileCards = fileCards,
        folders = folders,
        enableItemGestures = enableItemGestures,
        preferEagerCompose = preferEagerCompose,
        selectedFileName = selectedFileName,
        draggingFile = draggingFile,
        dragPosition = dragPosition,
        hoverFolder = hoverFolder,
        hoverFile = hoverFile,
        useGridLayout = useGridLayout,
        showFilesFirst = showFilesFirst,
        cardLayout = cardLayout,
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
        modifier = modifier
    )
}
