package com.example.testapp.presentation.screen

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.basicMarquee
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.ui.input.pointer.pointerInput


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onStartQuiz: (quizId: String) -> Unit = {},
    onStartExam: (quizId: String) -> Unit = {},
    onSettings: () -> Unit = {},
    onViewQuestionDetail: (quizId: String) -> Unit = {},
    onWrongBook: (fileName: String) -> Unit = {},
    onFavoriteBook: (fileName: String) -> Unit = {},
    onViewResult: (fileName: String) -> Unit = {}
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val questions by viewModel.questions.collectAsState()
    val fileNames by viewModel.fileNames.collectAsState()
    val selectedFileName = remember { mutableStateOf("") }
    // 题目数量按文件统计
    val questionCounts = remember(questions) {
        questions.groupBy { it.fileName ?: "" }.mapValues { it.value.size }
    }
    LaunchedEffect(fileNames) {
        if (fileNames.isNotEmpty() && selectedFileName.value !in fileNames) {
            selectedFileName.value = fileNames.first()
        }
    }
    var bottomNavIndex by remember { mutableStateOf(0) }

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
                    onClick = {
                        bottomNavIndex = 0
                        onWrongBook("")
                    },
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
                    onClick = {
                        bottomNavIndex = 1
                        onFavoriteBook("")
                    },
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
                    onClick = {
                        bottomNavIndex = 2
                        onViewResult(selectedFileName.value)
                    },
                    icon = { Icon(Icons.Filled.FactCheck, contentDescription = "答题记录") },
                    label = {
                        Text(
                            "答题记录",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                    ) }
                )
            }
        }
    ) { innerPadding ->
        Log.d("HomeScreen", "[Compose] fileNames=$fileNames, selectedFileName=${selectedFileName.value}, questions.size=${questions.size}")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            android.util.Log.d("HomeScreen", "fontSize=${LocalFontSize.current}, fontFamily=${LocalFontFamily.current}, recomposed!")
            // 文件名列表
            if (fileNames.isNotEmpty()) {
                val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels / LocalContext.current.resources.displayMetrics.density
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // 让列表撑满剩余空间
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        items(fileNames, key = { it }) { name ->
                            val dismissState = rememberDismissState()
                            if (dismissState.currentValue == DismissValue.DismissedToStart) {
                                Log.d("HomeScreen", "[Delete] 删除文件: $name, 当��fileNames=$fileNames, 当前选中=${selectedFileName.value}")
                                viewModel.deleteFileAndData(name) {
                                    // 删除后回调，自动切换选中项
                                    val newList = fileNames.filter { it != name }
                                    selectedFileName.value = newList.firstOrNull() ?: ""
                                    Log.d("HomeScreen", "[Delete] 更新选中: ${selectedFileName.value}")
                                }
                            }
                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(DismissDirection.EndToStart),
                                dismissThresholds = { direction ->
                                    FractionalThreshold(0.2f)
                                },
                                background = {

                                    Log.d("HomeScreen", "[SwipeToDismiss-background] name=$name, dismissDirection=${dismissState.dismissDirection}, targetValue=${dismissState.targetValue}, currentValue=${dismissState.currentValue}")
                                    val showRed = dismissState.dismissDirection != null && dismissState.targetValue != DismissValue.Default
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if (showRed) MaterialTheme.colorScheme.error else Color.Transparent
                                            )
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if (showRed) {
                                            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = Color.White)
                                        } else {
                                            Log.d("HomeScreen", "[SwipeToDismiss-background] 显示透明 name=$name")
                                        }
                                    }
                                },
                                dismissContent = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 11.dp)
                                            .background(
                                                if (selectedFileName.value == name) {
                                                    Color(0xFFBBDEFB)
                                                } else {
                                                    MaterialTheme.colorScheme.surface
                                                }
                                            )
                                            .pointerInput(name) {
                                                detectTapGestures(
                                                    onTap = {
                                                        selectedFileName.value = name
                                                    },
                                                    onDoubleTap = {
                                                        selectedFileName.value = name
                                                        onViewQuestionDetail(name)
                                                    }
                                                )
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = buildAnnotatedString {
                                                append("$name ")
                                                withStyle(SpanStyle(color = Color.Blue)) {
                                                    append("(${questionCounts[name] ?: 0})")
                                                }
                                            },
                                            maxLines = 1,
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
                Spacer(modifier = Modifier.weight(1f))
            }
            if (questions.isEmpty()) {
                Text(
                    text = "加载中...",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedFileName.value.isNotEmpty() && fileNames.contains(selectedFileName.value)) {
                            onStartQuiz(selectedFileName.value)
                        }
                    },
                    enabled = selectedFileName.value.isNotEmpty() && fileNames.contains(selectedFileName.value),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        "开始练习",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
                Button(
                    onClick = {
                        if (selectedFileName.value.isNotEmpty() && fileNames.contains(selectedFileName.value)) {
                            onStartExam(selectedFileName.value)
                        }
                    },
                    enabled = selectedFileName.value.isNotEmpty() && fileNames.contains(selectedFileName.value),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        "开始考试",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }
    }

}