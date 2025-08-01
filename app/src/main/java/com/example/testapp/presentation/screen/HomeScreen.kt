﻿package com.example.testapp.presentation.screen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.OptimizedFileCard
import com.example.testapp.presentation.component.DraggingFileCard
import kotlin.math.roundToInt
import com.example.testapp.presentation.screen.WrongBookViewModel
import com.example.testapp.presentation.screen.FavoriteViewModel

@Composable
private fun FileStatBlock(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = LocalFontFamily.current
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = LocalFontFamily.current
            ),
            color = valueColor
        )
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun HomeScreen(
    onStartQuiz: (quizId: String) -> Unit = {},
    onStartExam: (quizId: String) -> Unit = {},
    onSettings: () -> Unit = {},
    onViewQuestionDetail: (quizId: String) -> Unit = {},
    onWrongBook: (fileName: String) -> Unit = {},
    onFavoriteBook: (fileName: String) -> Unit = {},
    onViewResult: (fileName: String) -> Unit = {},
    onStartWrongBookQuiz: (fileName: String) -> Unit = {},
    onStartWrongBookExam: (fileName: String) -> Unit = {},
    onStartFavoriteQuiz: (fileName: String) -> Unit = {},
    onStartFavoriteExam: (fileName: String) -> Unit = {},
    settingsViewModel: SettingsViewModel
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val folderViewModel: FileFolderViewModel = hiltViewModel()
    val dragViewModel: DragDropViewModel = hiltViewModel()
    val wrongBookViewModel: WrongBookViewModel = hiltViewModel()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    val questions by viewModel.questions.collectAsState()
    val fileNames by viewModel.fileNames.collectAsState()
    val folders by folderViewModel.folders.collectAsState()
    val folderNames by folderViewModel.folderNames.collectAsState()
    var currentFolder by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val storedFileName by FontSettingsDataStore
        .getLastSelectedFile(context)
        .collectAsState(initial = "")
    val selectedFileName = remember { mutableStateOf("") }

    val isLoading by settingsViewModel.isLoading.collectAsState()
    val importProgress by settingsViewModel.progress.collectAsState()
    
    // 使用优化的统计数据，减少重组
    val fileStatistics by viewModel.fileStatistics.collectAsState()
    val practiceProgress by viewModel.practiceProgress.collectAsState()
    val displayFileNames = remember(fileNames, folders, currentFolder) {
        fileNames.filter { name ->
            val folder = folders[name]
            if (currentFolder == null) folder == null else folder == currentFolder
        }
    }
    
    LaunchedEffect(storedFileName, fileNames) {
        if (storedFileName.isNotBlank() && storedFileName in fileNames) {
            selectedFileName.value = storedFileName
        } else if (fileNames.isNotEmpty()) {
            selectedFileName.value = fileNames.first()
        }
    }
    LaunchedEffect(selectedFileName.value) {
        if (selectedFileName.value.isNotBlank()) {
            FontSettingsDataStore.setLastSelectedFile(context, selectedFileName.value)
        }
    }

    val storedNavIndex by FontSettingsDataStore
        .getLastSelectedNav(context)
        .collectAsState(initial = 3)
    var bottomNavIndex by remember { mutableStateOf(storedNavIndex) }

    LaunchedEffect(storedNavIndex) {
        bottomNavIndex = storedNavIndex
    }

    LaunchedEffect(bottomNavIndex) {
        FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
    }

    var showSheet by remember { mutableStateOf(false) }
    var pendingFileName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf("") }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var renameFolderTarget by remember { mutableStateOf<String?>(null) }
    var renameFolderName by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteFolderDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    // 检测滚动状态，滚动时禁用拖拽以提升性能
    val isScrolling by remember {
        derivedStateOf { listState.isScrollInProgress }
    }

    // TopAppBar 随滚动隐藏显示
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val folderBounds = remember { mutableStateMapOf<String, Rect>() }
    val dragPosition by dragViewModel.dragPosition.collectAsState()
    val draggingFile by dragViewModel.draggingFile.collectAsState()
    val dragItemSize by dragViewModel.dragItemSize.collectAsState()
    val dragOffset by dragViewModel.offsetWithinItem.collectAsState()
    val hoverFolder by dragViewModel.hoverFolder.collectAsState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("主页", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置字体")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = bottomNavIndex == 0,
                    onClick = {
                        bottomNavIndex = 0
                        kotlinx.coroutines.runBlocking {
                            FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
                        }
                    },
                    icon = { Icon(Icons.Filled.Warning, "错题库") },
                    label = { Text("错题库", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 1,
                    onClick = {
                        bottomNavIndex = 1
                        kotlinx.coroutines.runBlocking {
                            FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
                        }
                    },
                    icon = { Icon(Icons.Filled.Favorite, "收藏库") },
                    label = { Text("收藏库", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 2,
                    onClick = {
                        bottomNavIndex = 2
                        kotlinx.coroutines.runBlocking {
                            FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
                        }
                    },
                    icon = { Icon(Icons.Filled.FactCheck, "记录") },
                    label = { Text("记录", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 3,
                    onClick = {
                        bottomNavIndex = 3
                        kotlinx.coroutines.runBlocking {
                            FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
                        }
                    },
                    icon = { Icon(Icons.Filled.SwapHoriz, "模式") },
                    label = { Text("模式", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures(onLongPress = { showAddFolderDialog = true }) }
        ) {
           
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 当前所在文件夹
                if (currentFolder != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentFolder = null }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                        }
                        Text(
                            currentFolder ?: "",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
                // 文件夹展示行
                if (folderNames.isNotEmpty()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        folderNames.forEach { folder ->
                            val dismissState = rememberDismissState(
                                confirmStateChange = {
                                    if (it == DismissValue.DismissedToStart) {
                                        folderToDelete = folder
                                        showDeleteFolderDialog = true
                                        false
                                    } else true
                                }
                            )
                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(DismissDirection.EndToStart),
                                dismissThresholds = { FractionalThreshold(0.2f) },
                                background = {
                                    val showRed = dismissState.dismissDirection != null &&
                                            dismissState.targetValue != DismissValue.Default
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .background(if (showRed) MaterialTheme.colorScheme.error else Color.Transparent)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if (showRed) Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color.White)
                                    }
                                },
                                dismissContent = {
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .onGloballyPositioned { coords ->
                                                folderBounds[folder] = coords.boundsInRoot()
                                            }
                                            .background(
                                                if (hoverFolder == folder || currentFolder == folder)
                                                    MaterialTheme.colorScheme.secondaryContainer
                                                else MaterialTheme.colorScheme.primaryContainer,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .combinedClickable(
                                                onClick = { currentFolder = folder },
                                                onLongClick = {
                                                    renameFolderTarget = folder
                                                    renameFolderName = folder
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (hoverFolder == folder || currentFolder == folder) Icons.Filled.Folder else Icons.Outlined.Folder,
                                                contentDescription = "文件夹",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(folder, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                                        }
                                    }

                                }
                            )
                        }
                        IconButton(onClick = { showAddFolderDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "新增文件夹")
                        }
                    }
                }

                // 优化的文件列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    state = listState,
                    // 添加内容填充，减少列表项创建/销毁频率
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = displayFileNames,
                        key = { fileName -> fileName }, // 保持稳定的 key
                        contentType = { "file_card" } // 添加内容类型优化
                    ) { fileName ->
                        // 使用 derivedStateOf 减少不必要的重组
                        val fileStats by remember(fileName) {
                            derivedStateOf { 
                                fileStatistics[fileName] ?: com.example.testapp.domain.usecase.FileStatistics()
                            }
                        }
                        val progressCount by remember(fileName) {
                            derivedStateOf { practiceProgress[fileName] ?: 0 }
                        }
                        
                        val dismissState = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissValue.DismissedToStart) {
                                    fileToDelete = fileName
                                    showDeleteDialog = true
                                    false
                                } else true
                            }
                        )
                        
                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(DismissDirection.EndToStart),
                            dismissThresholds = { FractionalThreshold(0.2f) },
                            background = {
                                val showRed = dismissState.dismissDirection != null &&
                                        dismissState.targetValue != DismissValue.Default
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(if (showRed) MaterialTheme.colorScheme.error else Color.Transparent)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (showRed) Icon(Icons.Filled.Delete, "删除", tint = Color.White)
                                }
                            },
                            dismissContent = {
                                OptimizedFileCard(
                                    fileName = fileName,
                                    statistics = fileStats,
                                    progressCount = progressCount,
                                    isSelected = selectedFileName.value == fileName,
                                    folderDisplayName = folders[fileName],
                                    isDragging = draggingFile == fileName,
                                    enableDragDrop = !isScrolling, // 滚动时禁用拖拽
                                    onCardClick = {
                                        when (bottomNavIndex) {
                                            2 -> {
                                                selectedFileName.value = fileName
                                                kotlinx.coroutines.runBlocking {
                                                    FontSettingsDataStore.setLastSelectedFile(context, fileName)
                                                }
                                                onViewResult(fileName)
                                            }
                                            else -> {
                                                if (selectedFileName.value == fileName) {
                                                    pendingFileName = fileName
                                                    kotlinx.coroutines.runBlocking {
                                                        FontSettingsDataStore.setLastSelectedFile(context, fileName)
                                                    }
                                                    showSheet = true
                                                } else {
                                                    selectedFileName.value = fileName
                                                    kotlinx.coroutines.runBlocking {
                                                        FontSettingsDataStore.setLastSelectedFile(context, fileName)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onDoubleClick = {
                                        selectedFileName.value = fileName
                                        kotlinx.coroutines.runBlocking {
                                            FontSettingsDataStore.setLastSelectedFile(context, fileName)
                                        }
                                        onViewQuestionDetail(fileName)
                                    },
                                    onDragStart = { position, size, offset ->
                                        dragViewModel.startDragging(fileName, position, size, offset)
                                    },
                                    onDragUpdate = { position ->
                                        dragViewModel.updatePosition(position)
                                        dragViewModel.setHoverFolder(
                                            folderBounds.entries.find { it.value.contains(position) }?.key
                                        )
                                    },
                                    onDragEnd = {
                                        val target = folderBounds.entries
                                            .find { it.value.contains(dragViewModel.dragPosition.value) }?.key
                                        if (target != null) {
                                            folderViewModel.moveFile(fileName, target)
                                        }
                                        dragViewModel.endDragging()
                                    }
                                )
                            }
                        )
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(progress = importProgress)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "正在处理，请稍候…",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                    }
                }
            }

            // 拖拽悬浮卡片（overlay）
            draggingFile?.let { file ->
                val widthDp = with(LocalDensity.current) { dragItemSize.width.toDp() }
                val heightDp = with(LocalDensity.current) { dragItemSize.height.toDp() }
                val fileStats = fileStatistics[file] ?: com.example.testapp.domain.usecase.FileStatistics()
                val fileProgressCount = practiceProgress[file] ?: 0
                
                DraggingFileCard(
                    fileName = file,
                    statistics = fileStats,
                    progressCount = fileProgressCount,
                    folderDisplayName = folders[file],
                    dragPosition = dragPosition,
                    dragOffset = dragOffset,
                    dragItemSize = dragItemSize,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (dragPosition.x - dragOffset.x).roundToInt(),
                                (dragPosition.y - dragOffset.y).roundToInt()
                            )
                        }
                        .width(widthDp)
                        .height(heightDp)
                        .graphicsLayer { scaleX = 0.75f; scaleY = 0.75f }
                )
            }

            // BottomSheet 弹出菜单
            if (showSheet) {
                ModalBottomSheet(onDismissRequest = { showSheet = false }) {
                    Column(
                        Modifier
                            .padding(top = 16.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(pendingFileName, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                showSheet = false
                                kotlinx.coroutines.runBlocking {
                                    FontSettingsDataStore.setLastSelectedFile(context, pendingFileName)
                                    FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
                                }
                                when (bottomNavIndex) {
                                    0 -> onStartWrongBookQuiz(pendingFileName)
                                    1 -> onStartFavoriteQuiz(pendingFileName)
                                    else -> onStartQuiz(pendingFileName)
                                }
                            },
                            Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) { Text("开始练习") }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                showSheet = false
                                kotlinx.coroutines.runBlocking {
                                    FontSettingsDataStore.setLastSelectedFile(context, pendingFileName)
                                    FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)
                                }
                                when (bottomNavIndex) {
                                    0 -> onStartWrongBookExam(pendingFileName)
                                    1 -> onStartFavoriteExam(pendingFileName)
                                    else -> onStartExam(pendingFileName)
                                }
                            },
                            Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) { Text("开始考试") }
                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = { showSheet = false },
                            Modifier.fillMaxWidth()
                        ) { Text("取消") }
                    }
                }
            }

            // 删除确认对话框
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            viewModel.deleteFileAndData(fileToDelete)
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
                    },
                    text = { Text("确定删除 $fileToDelete 及其相关数据吗？") }
                )
            }

            // 新增文件夹对话框
            if (showAddFolderDialog) {
                AlertDialog(
                    onDismissRequest = { showAddFolderDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            folderViewModel.addFolder(newFolderName)
                            newFolderName = ""
                            showAddFolderDialog = false
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddFolderDialog = false }) { Text("取消") }
                    },
                    text = {
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            label = { Text("文件夹名") }
                        )
                    }
                )
            }
            if (renameFolderTarget != null) {
                AlertDialog(
                    onDismissRequest = { renameFolderTarget = null },
                    confirmButton = {
                        TextButton(onClick = {
                            renameFolderTarget?.let { folderViewModel.renameFolder(it, renameFolderName) }
                            renameFolderTarget = null
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { renameFolderTarget = null }) { Text("取消") }
                    },
                    text = {
                        OutlinedTextField(
                            value = renameFolderName,
                            onValueChange = { renameFolderName = it },
                            label = { Text("重命名") }
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
                            folderToDelete?.let { folderViewModel.deleteFolder(it) }
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteFolderDialog = false }) { Text("取消") }
                    },
                    text = { Text("确定删除 $folderToDelete 吗？") }
                )
            }
        }
    }
}
