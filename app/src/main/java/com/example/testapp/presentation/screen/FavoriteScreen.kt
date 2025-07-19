package com.example.testapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.LayoutCoordinates

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FavoriteScreen(
    fileName: String? = null,
    navController: NavController? = null,
    viewModel: FavoriteViewModel = hiltViewModel(),
    folderViewModel: FileFolderViewModel = hiltViewModel()
) {
    val favorites = viewModel.favoriteQuestions.collectAsState()
    val fileNames = viewModel.fileNames.collectAsState()
    val folders = folderViewModel.folders.collectAsState()
    val folderNames = folderViewModel.folderNames.collectAsState()
    var showMoveDialog by remember { mutableStateOf(false) }
    var moveTargetFile by remember { mutableStateOf("") }
    var moveFolder by remember { mutableStateOf("") }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    val folderBounds = remember { mutableMapOf<String, Rect>() }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var draggingFile by remember { mutableStateOf<String?>(null) }
    val filteredFavorites = if (fileName.isNullOrEmpty()) favorites.value else favorites.value.filter { it.question.fileName == fileName }
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
        if (folderNames.value.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                folderNames.value.forEach { folder ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .onGloballyPositioned { coords ->
                                folderBounds[folder] = coords.boundsInRoot()
                            }
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Folder,
                                contentDescription = "文件夹",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(folder, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (fileName.isNullOrEmpty()) {
            if (fileNames.value.isEmpty()) {
                Text(
                    "暂无收藏题目",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(fileNames.value, key = { it }) { name ->
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
                                        .onGloballyPositioned { itemCoords = it }
                                        .pointerInput(name) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { offset ->
                                                    draggingFile = name
                                                    dragPosition = itemCoords?.localToRoot(offset) ?: Offset.Zero
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    dragPosition = itemCoords?.localToRoot(change.position) ?: dragPosition
                                                },
                                                onDragEnd = {
                                                    val target = folderBounds.entries.find { it.value.contains(dragPosition) }?.key
                                                    if (target != null) {
                                                        folderViewModel.moveFile(name, target)
                                                    }
                                                    draggingFile = null
                                                },
                                                onDragCancel = { draggingFile = null }
                                            )
                                        }
                                        .clickable {
                                            val encoded = java.net.URLEncoder.encode(name, "UTF-8")
                                            navController?.navigate("practice_favorite/$encoded")
                                        }
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
                                    IconButton(onClick = {
                                        moveTargetFile = name
                                        moveFolder = folders.value[name] ?: ""
                                        showMoveDialog = true
                                    }) { Icon(Icons.Outlined.Folder, contentDescription = "移动") }
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
        if (showMoveDialog) {
            AlertDialog(
                onDismissRequest = { showMoveDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        folderViewModel.moveFile(moveTargetFile, moveFolder)
                        showMoveDialog = false
                    }) { Text("确定") }
                },
                dismissButton = {
                    TextButton(onClick = { showMoveDialog = false }) { Text("取消") }
                },
                text = {
                    Column {
                        Text("移动 \$moveTargetFile 到文件夹")
                        OutlinedTextField(
                            value = moveFolder,
                            onValueChange = { moveFolder = it },
                            label = { Text("文件夹名") }
                        )
                    }
                }
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
