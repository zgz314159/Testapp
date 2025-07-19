package com.example.testapp.presentation.screen

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import kotlin.math.roundToInt

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
    settingsViewModel: SettingsViewModel
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val folderViewModel: FileFolderViewModel = hiltViewModel()
    val dragViewModel: DragDropViewModel = hiltViewModel()

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
    val questionCounts = remember(questions) {
        questions.groupBy { it.fileName ?: "" }.mapValues { it.value.size }
    }

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

    var bottomNavIndex by remember { mutableStateOf(3) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                bottomNavIndex = 3
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
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
    val folderBounds = remember { mutableStateMapOf<String, Rect>() }
    val dragPosition by dragViewModel.dragPosition.collectAsState()
    val draggingFile by dragViewModel.draggingFile.collectAsState()
    val dragItemSize by dragViewModel.dragItemSize.collectAsState()
    val dragOffset by dragViewModel.offsetWithinItem.collectAsState()
    val hoverFolder by dragViewModel.hoverFolder.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("题库主页", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置字体")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = bottomNavIndex == 0,
                    onClick = { bottomNavIndex = 0 },
                    icon = { Icon(Icons.Filled.Warning, "错题库") },
                    label = { Text("错题库", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 1,
                    onClick = { bottomNavIndex = 1 },
                    icon = { Icon(Icons.Filled.Favorite, "收藏库") },
                    label = { Text("收藏库", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 2,
                    onClick = { bottomNavIndex = 2 },
                    icon = { Icon(Icons.Filled.FactCheck, "答题记录") },
                    label = { Text("答题记录", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 3,
                    onClick = { bottomNavIndex = 3 },
                    icon = { Icon(Icons.Filled.Settings, "题库主页") },
                    label = { Text("题库主页", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current) }
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

                // 文件列表
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                ) {
                    items(displayFileNames, key = { it }) { name ->
                        val dismissState = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissValue.DismissedToStart) {
                                    fileToDelete = name
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
                                var itemCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                                val itemModifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .onGloballyPositioned { coords -> itemCoords = coords }
                                    // 隐藏正在拖拽的原卡片
                                    .alpha(if (draggingFile == name) 0f else 1f)

                                Card(
                                    modifier = itemModifier
                                        .combinedClickable(
                                            onClick = {
                                                if (bottomNavIndex == 3) {
                                                    if (selectedFileName.value == name) {
                                                        pendingFileName = name
                                                        showSheet = true
                                                    } else {
                                                        selectedFileName.value = name
                                                    }
                                                } else {
                                                    selectedFileName.value = name
                                                    when (bottomNavIndex) {
                                                        0 -> onWrongBook(name)
                                                        1 -> onFavoriteBook(name)
                                                        2 -> onViewResult(name)
                                                    }
                                                }
                                            },
                                            onLongClick = { /* 空实现 */ },
                                            onDoubleClick = {
                                                selectedFileName.value = name
                                                onViewQuestionDetail(name)
                                            }
                                        )
                                        .pointerInput(name) {
                                            var startedLocally = false
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { offsetWithinCard ->
                                                    Log.d("HomeScreen", "onDragStart for $name at $offsetWithinCard")
                                                    startedLocally = true
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    if (startedLocally) {
                                                        startedLocally = false
                                                        val startPos = itemCoords?.localToRoot(change.position)
                                                            ?: Offset.Zero
                                                        val size = itemCoords?.size ?: IntSize.Zero
                                                        Log.d("HomeScreen", "startDragging for $name")
                                                        dragViewModel.startDragging(
                                                            name, startPos, size, change.position
                                                        )
                                                    }
                                                    val pos = itemCoords?.localToRoot(change.position)
                                                        ?: dragViewModel.dragPosition.value
                                                    dragViewModel.updatePosition(pos)
                                                    dragViewModel.setHoverFolder(
                                                        folderBounds.entries.find { it.value.contains(pos) }?.key
                                                    )
                                                    Log.d("HomeScreen", ">>> onDrag for $name, pos=$pos")
                                                },
                                                onDragEnd = {
                                                    Log.d("HomeScreen", "onDragEnd for $name")
                                                    val target = folderBounds.entries
                                                        .find { it.value.contains(dragViewModel.dragPosition.value) }
                                                        ?.key
                                                    if (target != null) {
                                                        folderViewModel.moveFile(name, target)
                                                    }
                                                    dragViewModel.endDragging()
                                                },
                                                onDragCancel = {
                                                    Log.d("HomeScreen", "onDragCancel for $name")
                                                    dragViewModel.endDragging()
                                                }
                                            )
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = if (selectedFileName.value == name)
                                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                    else CardDefaults.cardColors(),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = if (selectedFileName.value == name) 6.dp else 2.dp
                                    )
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val displayName = folders[name]?.let { "$it/$name" } ?: name
                                        Text(
                                            text = displayName,
                                            fontSize = LocalFontSize.current,
                                            fontFamily = LocalFontFamily.current,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${questionCounts[name] ?: 0}题",
                                            fontSize = LocalFontSize.current * 0.95f,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
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
                val displayName = folders[file]?.let { "$it/$file" } ?: file
                Card(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (dragPosition.x - dragOffset.x).roundToInt(),
                                (dragPosition.y - dragOffset.y).roundToInt()
                            )
                        }
                        .width(widthDp)
                        .height(heightDp)
                        .graphicsLayer { scaleX = 0.75f; scaleY = 0.75f },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayName,
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${questionCounts[file] ?: 0}题",
                            fontSize = LocalFontSize.current * 0.95f,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                                onStartQuiz(pendingFileName)
                            },
                            Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) { Text("开始练习") }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                showSheet = false
                                onStartExam(pendingFileName)
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
