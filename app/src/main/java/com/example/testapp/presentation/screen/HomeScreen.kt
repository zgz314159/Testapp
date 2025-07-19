package com.example.testapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.rememberDismissState
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.FileFolderViewModel
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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
    LaunchedEffect(storedFileName, fileNames) {
        val fromPrefs = storedFileName
        if (fromPrefs.isNotBlank() && fromPrefs in fileNames) {
            selectedFileName.value = fromPrefs
        } else if (fileNames.isNotEmpty()) {
            if (selectedFileName.value.isBlank() || selectedFileName.value !in fileNames) {
                selectedFileName.value = fileNames.first()

            }
        }
    }
    LaunchedEffect(selectedFileName.value) {
        if (selectedFileName.value.isNotBlank()) {
            FontSettingsDataStore.setLastSelectedFile(context, selectedFileName.value)
        }
    }
    var bottomNavIndex by remember { mutableStateOf(3) } // 3: 主界面
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                bottomNavIndex = 3
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    // BottomSheet 控制变量
    var showSheet by remember { mutableStateOf(false) }
    var pendingFileName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf("") }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    val folderBounds = remember { mutableStateMapOf<String, Rect>() }
    val dragPosition by dragViewModel.dragPosition.collectAsState()
    val draggingFile by dragViewModel.draggingFile.collectAsState()
    val dragItemSize by dragViewModel.dragItemSize.collectAsState()
    val hoverFolder by dragViewModel.hoverFolder.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "题库主页",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                },
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
                    icon = { Icon(Icons.Filled.Warning, contentDescription = "错题库") },
                    label = {
                        Text(
                            "错题库",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 1,
                    onClick = { bottomNavIndex = 1 },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "收藏库") },
                    label = {
                        Text(
                            "收藏库",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 2,
                    onClick = { bottomNavIndex = 2 },
                    icon = { Icon(Icons.Filled.FactCheck, contentDescription = "答题记录") },
                    label = {
                        Text(
                            "答题记录",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                )
                NavigationBarItem(
                    selected = bottomNavIndex == 3,
                    onClick = { bottomNavIndex = 3 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "题库主页") },
                    label = {
                        Text(
                            "题库主页",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) { detectTapGestures(onLongPress = { showAddFolderDialog = true }) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (folderNames.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        folderNames.forEach { folder ->
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .onGloballyPositioned { coords ->
                                        folderBounds[folder] = coords.boundsInRoot()
                                    }
                                    .background(
                                        if (hoverFolder == folder) MaterialTheme.colorScheme.secondaryContainer
                                        else MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (hoverFolder == folder) Icons.Filled.Folder else Icons.Outlined.Folder,
                                        contentDescription = "文件夹",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(folder, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                                }
                            }
                        }
                    }
                }

                // ========== 文件列表 ==========
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                ) {
                    items(fileNames, key = { it }) { name ->
                        val dismissState = rememberDismissState(confirmStateChange = {
                            if (it == DismissValue.DismissedToStart) {
                                fileToDelete = name
                                showDeleteDialog = true
                                false
                            } else {
                                true
                            }
                        })
                        SwipeToDismiss(
                            state = dismissState,
                            directions = setOf(DismissDirection.EndToStart),
                            dismissThresholds = { FractionalThreshold(0.2f) },
                            background = {
                                val showRed = dismissState.dismissDirection != null &&
                                        dismissState.targetValue != DismissValue.Default
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(if (showRed) MaterialTheme.colorScheme.error else Color.Transparent)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (showRed) {
                                        Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color.White)
                                    }
                                }
                            },
                            dismissContent = {
                                var itemCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                                val isDraggingItem = draggingFile == name
                                Card(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            if (isDraggingItem) {
                                                scaleX = 0.9f
                                                scaleY = 0.9f
                                            }
                                        }
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .onGloballyPositioned { itemCoords = it }
                                        .pointerInput(name) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { offset ->
                                                    val pos = itemCoords?.localToRoot(offset) ?: Offset.Zero
                                                    val size = itemCoords?.size ?: IntSize.Zero
                                                    android.util.Log.d("HomeScreen", "start drag $name at $pos size=$size")
                                                    dragViewModel.startDragging(name, pos, size)
                                                    dragViewModel.setHoverFolder(
                                                        folderBounds.entries.find { it.value.contains(pos) }?.key
                                                    )
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    val pos = itemCoords?.localToRoot(change.position)
                                                        ?: dragViewModel.dragPosition.value
                                                    dragViewModel.updatePosition(pos)
                                                    dragViewModel.setHoverFolder(
                                                        folderBounds.entries.find { it.value.contains(pos) }?.key
                                                    )
                                                },
                                                onDragEnd = {
                                                    val target = folderBounds.entries
                                                        .find { it.value.contains(dragViewModel.dragPosition.value) }?.key
                                                    android.util.Log.d("HomeScreen", "end drag $name -> $target")
                                                    if (target != null) {
                                                        folderViewModel.moveFile(name, target)
                                                    }
                                                    dragViewModel.endDragging()
                                                },
                                                onDragCancel = {
                                                    android.util.Log.d("HomeScreen", "drag cancel $name")
                                                    dragViewModel.endDragging()
                                                }
                                            )
                                        }
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
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
                                                onDoubleTap = {
                                                    selectedFileName.value = name
                                                    onViewQuestionDetail(name)
                                                }
                                            )
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = if (selectedFileName.value == name)
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    else
                                        CardDefaults.cardColors(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (selectedFileName.value == name) 6.dp else 2.dp)
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

                // ========== 加载中 ==========
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(progress = importProgress)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "正在处理，请稍候…",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                    }
                }
            }

            draggingFile?.let { file ->
                val widthDp = with(LocalDensity.current) { dragItemSize.width.toDp() }
                val heightDp = with(LocalDensity.current) { dragItemSize.height.toDp() }
                val displayName = folders[file]?.let { "$it/$file" } ?: file
                Card(
                    modifier = Modifier
                        .offset { IntOffset(dragPosition.x.roundToInt(), dragPosition.y.roundToInt()) }
                        .width(widthDp)
                        .height(heightDp)
                        .graphicsLayer {
                            scaleX = 0.9f
                            scaleY = 0.9f
                        },
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

            // ========== BottomSheet 弹出菜单 ==========
            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false }
                ) {
                    Column(
                        Modifier
                            .padding(top = 16.dp, bottom = 28.dp, start = 24.dp, end = 24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            pendingFileName,
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        Button(
                            onClick = {
                                showSheet = false
                                onStartQuiz(pendingFileName)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("开始练习")
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                showSheet = false
                                onStartExam(pendingFileName)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text("开始考试")
                        }

                        Spacer(Modifier.height(12.dp))
                        TextButton(
                            onClick = { showSheet = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("取消")
                        }
                    }
                }
            }
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
        }
    }
}
