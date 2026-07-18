package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
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
    dragPosition: Offset?,
    hoverFolder: String?,
    hoverFile: String?,
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
    onViewQuestionDetail: (String) -> Unit,
    persistFileUsage: (String) -> Unit,
    onUpdateDragHover: (
        position: Offset,
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
    onFileCtaClick: ((String) -> Unit)? = null,
    headerContent: @Composable () -> Unit = {},
    showHeader: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (currentFolder != null) {
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
        }

        if (homeContentReady && homeLibraryEmptyReason != null) {
            HomeEmptyLibraryPanel(
                reason = homeLibraryEmptyReason,
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp)
            )
        } else {
            // BoxWithConstraints 响应式：<600dp 单列，>=600dp 两列
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(bottom = 8.dp)
            ) {
                val columnCount = HomeDashboardPipeline.resolveHomeColumnCount(maxWidth.value)
                val maxContentWidth = if (columnCount > 1) 960.dp else maxWidth

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (columnCount > 1) Modifier.padding(horizontal = (maxWidth - maxContentWidth) / 2)
                            else Modifier
                        )
                ) {
                    HomeFileListContainer(
                        viewModel = viewModel,
                        visibleFolders = if (currentFolder == null) displayFolders else emptyList(),
                        folderFileCounts = folderFileCounts,
                        displayFileNames = if (currentFolder == null) displayFileNames else displayFileNames,
                        folders = folders,
                        enableItemGestures = homeContentReady,
                        selectedFileName = selectedFileName,
                        draggingFile = draggingFile,
                        dragPosition = dragPosition ?: Offset.Zero,
                        hoverFolder = hoverFolder,
                        hoverFile = hoverFile,
                        // 分组内与错题库/收藏库一致：两列网格；根目录仅宽屏才多列
                        useGridLayout = columnCount > 1 || currentFolder != null,
                        // 2026 常见做法：分组文件夹排在散卡之前（Drive/Files 同款信息层级），
                        // 拖拽合并后新文件夹出现在列表顶部、即时可见。
                        // 根目录：题库卡在前（使用时间倒序），文件夹在后（建立时间升序）
                        showFilesFirst = true,
                        onFolderClick = onCurrentFolderChange,
                        onFolderLongPress = {
                            onRenameFolderTarget(it); onRenameFolderName(it)
                        },
                        onDeleteFolderClick = {
                            onFolderToDelete(it); onShowDeleteFolderDialog()
                        },
                        onCardClick = { fileName ->
                            if (selectedFileName == fileName) {
                                onPendingFileName(fileName)
                                viewModel.preloadQuestionFile(fileName)
                                onShowSheet()
                            } else {
                                onSelectedFileNameChange(fileName)
                                viewModel.preloadQuestionFile(fileName)
                            }
                        },
                        onDeleteClick = { fileName -> onFileToDelete(fileName); onShowDeleteDialog() },
                        onDoubleClick = { fileName -> onSelectedFileNameChange(fileName); persistFileUsage(fileName); onViewQuestionDetail(fileName) },
                        onDragStart = { fileName, position, size, offset ->
                            onBeforeDragStart(); dragViewModel.startDragging(fileName, position, size, offset)
                        },
                        onDragUpdate = { position -> onUpdateDragHover(position, currentFolder, homeDropTargetKey, currentFolderFileNames) },
                        onDragEnd = { fileName -> onFinishDrag(fileName, currentFolder, homeDropTargetKey, currentFolderFileNames) },
                        // 手势被系统/窗口打断时必须复位，否则 draggingFile 悬挂导致首页滚动被锁死
                        onDragCancel = { _ -> dragViewModel.endDragging() },
                        onReportFolderBounds = { name, rect -> folderBounds[name] = rect },
                        onReportCardBounds = { name, rect -> fileCardBounds[name] = rect },
                        onFileCtaClick = onFileCtaClick,
                        headerContent = headerContent,
                        showHeader = showHeader && currentFolder == null,
                    )
                }
            }
        }
    }
}
