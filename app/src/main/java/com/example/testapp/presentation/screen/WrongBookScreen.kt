package com.example.testapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.*
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.rememberDismissState
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Folder
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
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import androidx.navigation.NavController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.LayoutCoordinates
import com.example.testapp.presentation.screen.FileFolderViewModel
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WrongBookScreen(
    fileName: String? = null,
    viewModel: WrongBookViewModel = hiltViewModel(),
    navController: NavController? = null,
    folderViewModel: FileFolderViewModel = hiltViewModel()
) {
    val wrongList = viewModel.wrongQuestions.collectAsState()
    val fileNames = viewModel.fileNames.collectAsState()
    val folders = folderViewModel.folders.collectAsState()
    val folderNames = folderViewModel.folderNames.collectAsState()
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    val folderBounds = remember { mutableStateMapOf<String, Rect>() }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var draggingFile by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) { detectTapGestures(onLongPress = { showAddFolderDialog = true }) }
    ) {
        Text(
            "错题本",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
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
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
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
            // 显示错题按文件分类
            if (fileNames.value.isEmpty()) {
                Text(
                    "暂无错题",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(fileNames.value, key = { it }) { name ->
                        val count = wrongList.value.count { it.question.fileName == name }
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
                                            navController?.navigate("practice_wrongbook/$encoded")
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
                                }
                            }
                        )
                    }
                }
            }} else {
                val filteredList = wrongList.value.filter { it.question.fileName == fileName }
                if (filteredList.isEmpty()) {
                    Text(
                        "暂无错题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                } else {
                    filteredList.forEachIndexed { idx, wrong ->
                        val selectedOptions = wrong.selected.joinToString("，") { i ->
                            wrong.question.options.getOrNull(i) ?: ""
                        }
                        Text(
                            "${idx + 1}. ${wrong.question.content} (你的答案：$selectedOptions)",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                            navController?.navigate("practice_wrongbook/$encoded")
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            "重练错题",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
            }
        }
    
    draggingFile?.let { file ->
        Text(
            file,
            modifier = Modifier
                .offset { IntOffset(dragPosition.x.roundToInt(), dragPosition.y.roundToInt()) }
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
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

