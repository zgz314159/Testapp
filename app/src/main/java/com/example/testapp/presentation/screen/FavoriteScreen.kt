package com.example.testapp.presentation.screen

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.basicMarquee
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.rememberDismissState
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.screen.FileFolderViewModel
import com.example.testapp.presentation.screen.DragDropViewModel
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun FavoriteScreen(
    fileName: String? = null,
    navController: NavController? = null,
    viewModel: FavoriteViewModel = hiltViewModel(),
    folderViewModel: FileFolderViewModel = hiltViewModel(),
    dragViewModel: DragDropViewModel = hiltViewModel()
) {
    val favorites = viewModel.favoriteQuestions.collectAsState()
    val fileNames = viewModel.fileNames.collectAsState()
    val folders = folderViewModel.folders.collectAsState()
    val folderNames = folderViewModel.folderNames.collectAsState()
    var currentFolder by remember { mutableStateOf<String?>(null) }
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
    val filteredFavorites = if (fileName.isNullOrEmpty()) favorites.value else favorites.value.filter { it.question.fileName == fileName }
    val displayFileNames = remember(fileNames.value, folders.value, currentFolder) {
        fileNames.value.filter { name ->
            val folder = folders.value[name]
            if (currentFolder == null) folder == null else folder == currentFolder
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures(onLongPress = { showAddFolderDialog = true }) }
    ) {
        Text(
            "收藏夹",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (currentFolder != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (folderNames.value.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                folderNames.value.forEach { folder ->
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
                                        if (hoverFolder == folder || currentFolder == folder) MaterialTheme.colorScheme.secondaryContainer
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
            }
            IconButton(onClick = { showAddFolderDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "新增文件夹")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (fileName.isNullOrEmpty()) {
            if (displayFileNames.isEmpty()) {
                Text(
                    "暂无收藏题目",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(displayFileNames, key = { it }) { name ->
                        val count = favorites.value.count { it.question.fileName == name }
                        val dismissState = rememberDismissState()
                        if (dismissState.currentValue == DismissValue.DismissedToStart) {
                            viewModel.removeByFileName(name)
                        }
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer { if (draggingFile == name) { scaleX = 0.9f; scaleY = 0.9f } }
                                        .onGloballyPositioned { itemCoords = it }
                                        .pointerInput(name) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { offset ->
                                                    val pos = itemCoords?.localToRoot(offset) ?: Offset.Zero
                                                    val size = itemCoords?.size ?: IntSize.Zero
                                                    Log.d("FavoriteScreen", "start drag $name at $pos size=$size")
                                                    dragViewModel.startDragging(name, pos, size, offset)
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
                                                    Log.d("FavoriteScreen", "end drag $name -> $target")
                                                    if (target != null) {
                                                        folderViewModel.moveFile(name, target)
                                                    }
                                                    dragViewModel.endDragging()
                                                },
                                                onDragCancel = {
                                                    Log.d("FavoriteScreen", "drag cancel $name")
                                                    dragViewModel.endDragging()
                                                }
                                            )
                                        }
                                        .combinedClickable(
                                            onClick = {
                                                val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                                                navController?.navigate("practice_favorite/$encoded")
                                            }
                                        )
                                        .padding(vertical = 4.dp)
                                ) {
                                    val displayName = folders.value[name]?.let { "$it/$name" } ?: name
                                    Text(
                                        buildAnnotatedString {
                                            append("$displayName ")
                                            withStyle(SpanStyle(color = Color.Blue)) { append("(${count})") }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .basicMarquee(),
                                        fontSize = LocalFontSize.current,
                                        fontFamily = LocalFontFamily.current
                                    )

                                }
                            }
                        )
                    }
                }
            }
        } else {
            if (filteredFavorites.isEmpty()) {
                Text(
                    "暂无收藏题目",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            } else {
                filteredFavorites.forEachIndexed { idx, q ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) {
                            Text(
                                "${idx + 1}. ${q.question.content}",
                                modifier = Modifier.weight(1f),
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.removeFavorite(q.question.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                Text(
                                    "移除",
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = LocalFontSize.current,
                                    fontFamily = LocalFontFamily.current
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                    navController?.navigate("practice_favorite/$encoded")
                }) {
                    Text(
                        "练习本文件收藏题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }
        draggingFile?.let { file ->
            val widthDp = with(LocalDensity.current) { dragItemSize.width.toDp() }
            val heightDp = with(LocalDensity.current) { dragItemSize.height.toDp() }
            val displayName = folders.value[file]?.let { "$it/$file" } ?: file
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
                    .graphicsLayer { scaleX = 0.9f; scaleY = 0.9f },
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
                        buildAnnotatedString {
                            append("$displayName ")
                            withStyle(SpanStyle(color = Color.Blue)) { append("(${favorites.value.count { it.question.fileName == file }})") }
                        },
                        modifier = Modifier.weight(1f),
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
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
