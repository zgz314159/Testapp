package com.example.testapp.presentation.screen.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import com.example.testapp.presentation.screen.home.components.DraggingFileOverlay
import com.example.testapp.presentation.screen.home.components.HomeFileList
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.design.AppEmptyState
import com.example.testapp.uicommon.design.AppTopBar

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScopedQuestionLibraryScreen(
    scope: String,
    homeDropTargetKey: String,
    rootTitleRes: Int,
    emptyMessageRes: Int,
    fileNames: List<String>,
    fileStatistics: Map<String, FileStatistics>,
    folderViewModel: FileFolderViewModel,
    dragViewModel: DragDropViewModel,
    onDeleteFile: (String) -> Unit,
    onOpenFile: (String) -> Unit
) {
    val folders by folderViewModel.folders.collectAsState()
    val folderNames by folderViewModel.folderNames.collectAsState()
    val draggingFile by dragViewModel.draggingFile.collectAsState()
    val dragPosition by dragViewModel.dragPosition.collectAsState()
    val dragItemSize by dragViewModel.dragItemSize.collectAsState()
    val dragOffset by dragViewModel.offsetWithinItem.collectAsState()
    val hoverFolder by dragViewModel.hoverFolder.collectAsState()
    val hoverFile by dragViewModel.hoverFile.collectAsState()

    var currentFolder by remember { mutableStateOf<String?>(null) }
    var renameFolderTarget by remember { mutableStateOf<String?>(null) }
    var renameFolderName by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteFolderDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteFileDialog by remember { mutableStateOf(false) }

    val folderBounds = remember { mutableStateMapOf<String, Rect>() }
    val fileCardBounds = remember { mutableStateMapOf<String, Rect>() }

    val layout = remember(fileNames, folders, folderNames, currentFolder, scope) {
        buildScopedQuestionLibraryLayout(scope, fileNames, folders, folderNames, currentFolder)
    }
    val currentFolderFileNames = remember(layout.currentFolderDisplayFileNames) {
        layout.currentFolderDisplayFileNames.toSet()
    }

    LaunchedEffect(currentFolder, draggingFile) {
        if (currentFolder == null || draggingFile == null) {
            folderBounds.remove(homeDropTargetKey)
        }
    }

    LaunchedEffect(layout.rootDisplayFileNames, layout.currentFolderDisplayFileNames, layout.visibleFolderCards, currentFolder) {
        val validFiles = if (currentFolder == null) {
            layout.rootDisplayFileNames.toSet()
        } else {
            layout.currentFolderDisplayFileNames.toSet()
        }
        fileCardBounds.keys.retainAll(validFiles)
        val validFolders = buildSet {
            if (currentFolder == null) addAll(layout.visibleFolderCards)
            if (currentFolder != null) add(homeDropTargetKey)
        }
        folderBounds.keys.retainAll(validFolders)
    }

    fun scoped(value: String) = scopedLibraryName(scope, value)

    Scaffold(
        containerColor = HomeDesignTokens.backgroundLight,
        topBar = {
            if (currentFolder == null) {
                AppTopBar(title = stringResource(rootTitleRes))
            } else {
                AppTopBar(
                    title = currentFolder.orEmpty(),
                    onBack = { currentFolder = null }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        // HomeFileList 的卡片自身已有与主页相同的 24dp 外边距，
                        // 此处不能再次叠加，否则收藏夹/错题本卡片会明显变短。
                        horizontal = 0.dp,
                        vertical = HomeDesignTokens.spacingMd,
                    )
            ) {
                if (currentFolder == null && layout.rootDisplayFileNames.isEmpty() && layout.visibleFolderCards.isEmpty()) {
                    AppEmptyState(
                        message = stringResource(emptyMessageRes),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    HomeFileList(
                        visibleFolders = if (currentFolder == null) layout.visibleFolderCards else emptyList(),
                        folderFileCounts = layout.folderFileCounts,
                        displayFileNames = if (currentFolder == null) {
                            layout.rootDisplayFileNames
                        } else {
                            layout.currentFolderDisplayFileNames
                        },
                        folders = layout.scopedFolders,
                        fileStatistics = fileStatistics,
                        practiceProgress = emptyMap(),
                        selectedFileName = "",
                        draggingFile = draggingFile,
                        dragPosition = dragPosition,
                        hoverFolder = hoverFolder,
                        hoverFile = hoverFile,
                        useGridLayout = currentFolder != null,
                        showFilesFirst = true,
                        onFolderClick = { currentFolder = it },
                        onFolderLongPress = {
                            renameFolderTarget = it
                            renameFolderName = it
                        },
                        onDeleteFolderClick = {
                            folderToDelete = it
                            showDeleteFolderDialog = true
                        },
                        onCardClick = onOpenFile,
                        onDeleteClick = {
                            fileToDelete = it
                            showDeleteFileDialog = true
                        },
                        onDoubleClick = onOpenFile,
                        onDragStart = { name, pos, size, offset ->
                            dragViewModel.startDragging(name, pos, size, offset)
                        },
                        onDragUpdate = { pos ->
                            dragViewModel.updatePosition(pos)
                            if (currentFolder == null) {
                                dragViewModel.setHoverFolder(
                                    folderBounds.entries.find { it.value.contains(pos) }?.key
                                )
                                dragViewModel.setHoverFile(
                                    fileCardBounds.entries.firstOrNull {
                                        it.key != draggingFile && it.value.contains(pos)
                                    }?.key
                                )
                            } else {
                                dragViewModel.setHoverFolder(
                                    folderBounds[homeDropTargetKey]?.takeIf { it.contains(pos) }
                                        ?.let { homeDropTargetKey }
                                )
                                dragViewModel.setHoverFile(
                                    fileCardBounds.entries.firstOrNull {
                                        it.key != draggingFile &&
                                            it.key in currentFolderFileNames &&
                                            it.value.contains(pos)
                                    }?.key
                                )
                            }
                        },
                        onDragEnd = { name ->
                            val dropPosition = dragViewModel.dragPosition.value
                            if (currentFolder == null) {
                                val targetFolder = folderBounds.entries
                                    .find { it.value.contains(dropPosition) }?.key
                                val targetFile = fileCardBounds.entries.firstOrNull {
                                    it.key != name && it.value.contains(dropPosition)
                                }?.key
                                when {
                                    targetFolder != null -> folderViewModel.moveFile(
                                        scoped(name),
                                        scoped(targetFolder)
                                    )
                                    targetFile != null -> folderViewModel.groupFiles(
                                        scoped(name),
                                        scoped(targetFile)
                                    )
                                }
                            } else {
                                val targetFolder = folderBounds[homeDropTargetKey]
                                    ?.takeIf { it.contains(dropPosition) }
                                    ?.let { homeDropTargetKey }
                                val targetFile = fileCardBounds.entries.firstOrNull {
                                    it.key != name &&
                                        it.key in currentFolderFileNames &&
                                        it.value.contains(dropPosition)
                                }?.key
                                when {
                                    targetFolder == homeDropTargetKey -> folderViewModel.removeFileFromFolder(
                                        scoped(name)
                                    )
                                    targetFile != null -> folderViewModel.groupFiles(
                                        scoped(name),
                                        scoped(targetFile)
                                    )
                                }
                            }
                            dragViewModel.endDragging()
                        },
                        onDragCancel = { dragViewModel.endDragging() },
                        onReportFolderBounds = { name, rect -> folderBounds[name] = rect },
                        onReportCardBounds = { name, rect -> fileCardBounds[name] = rect },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AnimatedVisibility(
                visible = currentFolder != null && draggingFile != null,
                enter = fadeIn() + scaleIn(initialScale = 0.86f) + slideInVertically { it / 2 },
                exit = fadeOut() + scaleOut(targetScale = 0.9f) + slideOutVertically { it / 2 },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .onGloballyPositioned { coords ->
                            folderBounds[homeDropTargetKey] = coords.boundsInRoot()
                        },
                    shape = RoundedCornerShape(32.dp),
                    color = if (hoverFolder == homeDropTargetKey) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    shadowElevation = 10.dp,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = if (hoverFolder == homeDropTargetKey) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Home,
                                    contentDescription = null,
                                    tint = if (hoverFolder == homeDropTargetKey) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.drag_to_home_hint),
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.drag_to_home_subtitle),
                                fontSize = (LocalFontSize.current.value - 4f).coerceAtLeast(11f).sp,
                                fontFamily = LocalFontFamily.current,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            draggingFile?.let { file ->
                DraggingFileOverlay(
                    fileName = file,
                    statistics = fileStatistics[file] ?: FileStatistics(),
                    folderDisplayName = layout.scopedFolders[file],
                    dragPosition = dragPosition,
                    dragOffset = dragOffset,
                    dragItemSize = dragItemSize,
                    modifier = Modifier.graphicsLayer {
                        scaleX = 0.75f
                        scaleY = 0.75f
                    }
                )
            }
        }
    }

    if (renameFolderTarget != null) {
        AlertDialog(
            onDismissRequest = { renameFolderTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    renameFolderTarget?.let {
                        folderViewModel.renameFolder(scoped(it), scoped(renameFolderName))
                    }
                    renameFolderTarget = null
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { renameFolderTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = {
                OutlinedTextField(
                    value = renameFolderName,
                    onValueChange = { renameFolderName = it },
                    label = { Text(stringResource(R.string.rename)) }
                )
            }
        )
    }

    if (showDeleteFolderDialog && folderToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteFolderDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteFolderDialog = false
                    folderToDelete?.let { folderViewModel.deleteFolder(scoped(it)) }
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFolderDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { Text(stringResource(R.string.confirm_delete_target, folderToDelete ?: "")) }
        )
    }

    if (showDeleteFileDialog && fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteFileDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteFileDialog = false
                    fileToDelete?.let(onDeleteFile)
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFileDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { Text(stringResource(R.string.confirm_delete_target, fileToDelete ?: "")) }
        )
    }
}
