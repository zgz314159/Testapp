package com.example.testapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.screen.FavoriteViewModel

@Composable
fun ExamScreen(
    quizId: String,
    viewModel: ExamViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onExamEnd: (score: Int, total: Int) -> Unit = { _, _ -> }
) {
    val examCount by settingsViewModel.examQuestionCount.collectAsState()
    LaunchedEffect(quizId, examCount) { viewModel.loadQuestions(quizId, examCount) }
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val context = LocalContext.current
    val question = questions.getOrNull(currentIndex)
    val coroutineScope = rememberCoroutineScope()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    val favoriteQuestions by favoriteViewModel.favoriteQuestions.collectAsState()
    val isFavorite = remember(question, favoriteQuestions) {
        question != null && favoriteQuestions.any { it.question.id == question.id }
    }
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsed += 1
        }
    }
    var showList by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    if (question == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "暂无题目...",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Layer 1: timer, question list card and settings menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "时间：%02d:%02d".format(elapsed / 60, elapsed % 60),
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
                Spacer(modifier = Modifier.weight(1f))
                Card(onClick = { showList = true }) {
                    Text(
                        "共${questions.size}题",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "设置")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("放大字体") }, onClick = {
                        settingsViewModel.setFontSize(context, (fontSize + 2).coerceAtMost(32f))
                        menuExpanded = false
                    })
                    DropdownMenuItem(text = { Text("缩小字体") }, onClick = {
                        settingsViewModel.setFontSize(context, (fontSize - 2).coerceAtLeast(14f))
                        menuExpanded = false
                    })
                    DropdownMenuItem(text = { Text("清除进度") }, onClick = {
                        viewModel.loadQuestions(quizId, examCount)
                        elapsed = 0
                        menuExpanded = false
                    })
                }
            }
            if (showList) {
                AlertDialog(onDismissRequest = { showList = false }, confirmButton = {}, text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(questions.size) { idx ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable {
                                        viewModel.goToQuestion(idx)
                                        showList = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = LocalFontSize.current,
                                    fontFamily = LocalFontFamily.current
                                )
                            }
                        }
                    }
                })
            }}

            Spacer(modifier = Modifier.height(8.dp))

            // Layer 2: type and progress
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "题型：${question.type}",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${currentIndex + 1}/${questions.size}",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
            LinearProgressIndicator(
                progress = if (questions.isNotEmpty()) (currentIndex + 1f) / questions.size else 0f,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            // Layer 3: question and options
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question.content,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        if (isFavorite) {
                            favoriteViewModel.removeFavorite(question.id)
                        } else {
                            favoriteViewModel.addFavorite(question)
                        }
                    }) {
                        Text(
                            if (isFavorite) "取消收藏" else "收藏",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                question.options.forEachIndexed { idx, option ->
                    val isSelected = selectedOptions.getOrElse(currentIndex) { -1 } == idx
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary.copy(
                                    alpha = 0.1f
                                ) else MaterialTheme.colorScheme.surface
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectOption(idx) }
                        )
                        Text(
                            option,
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Layer 4: answer buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (currentIndex > 0) {
                    Button(onClick = { viewModel.prevQuestion() }) {
                        Text(
                            "上一题",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
                if (currentIndex >= 1) {
                    Button(onClick = {
                        coroutineScope.launch {
                            val score = viewModel.gradeExam()
                            onExamEnd(score, questions.size)
                        }
                    }) {
                        Text(
                            "交卷",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
                if (currentIndex < questions.size - 1) {
                    Button(onClick = { viewModel.nextQuestion() }) {
                        Text(
                            "下一题",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
            }

        }
    }