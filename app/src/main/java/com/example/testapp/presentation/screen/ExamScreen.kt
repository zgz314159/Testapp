package com.example.testapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.screen.FavoriteViewModel
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.Job
import androidx.activity.compose.BackHandler
import com.example.testapp.util.answerLetterToIndex


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
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val showResultList by viewModel.showResultList.collectAsState()
    val finished by viewModel.finished.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val examDelay by settingsViewModel.examDelay.collectAsState()
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
    var questionFontSize by remember(fontSize) { mutableStateOf(fontSize) }
    var dragAmount by remember { mutableStateOf(0f) }
    var autoJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var containerWidth by remember { mutableStateOf(0f) }
    var dragStartX by remember { mutableStateOf(0f) }
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler {
        if (selectedOptions.any { it == -1 }) {
            showExitDialog = true
        } else {
            coroutineScope.launch {
                val score = viewModel.gradeExam()
                onExamEnd(score, questions.size)
            }
        }
    }
    val selectedOption = selectedOptions.getOrElse(currentIndex) { -1 }
    val showResult = showResultList.getOrNull(currentIndex) ?: false
    LaunchedEffect(selectedOption, showResult) {
        android.util.Log.d(
            "ExamScreen",
            "state current=$currentIndex selected=$selectedOption showResult=$showResult"
        )
    }
    if (question == null || !progressLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "暂无题目或正在加载进度…",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .onSizeChanged { containerWidth = it.width.toFloat() }
            .pointerInput(currentIndex, containerWidth) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                    },
                    onHorizontalDrag = { _, amount -> dragAmount += amount },
                    onDragEnd = {
                        if ((dragStartX < 20f && dragAmount > 100f) ||
                            (dragStartX > containerWidth - 20f && dragAmount < -100f)
                        ) {
                            if (selectedOptions.any { it == -1 }) {
                                showExitDialog = true
                            } else {
                                coroutineScope.launch {
                                    val score = viewModel.gradeExam()
                                    onExamEnd(score, questions.size)
                                }
                            }
                        } else {
                            if (dragAmount > 100f && currentIndex > 0) {
                                viewModel.prevQuestion()
                            } else if (dragAmount < -100f && currentIndex < questions.size - 1) {
                                viewModel.nextQuestion()
                            } else if (dragAmount < -100f) {
                                if (selectedOptions.any { it == -1 }) {
                                    showExitDialog = true
                                } else {
                                    coroutineScope.launch {
                                        val score = viewModel.gradeExam()
                                        onExamEnd(score, questions.size)
                                    }
                                }
                            }
                        }
                        dragAmount = 0f
                    },
                    onDragCancel = { dragAmount = 0f }
                )
            }
    ) {
        // Layer 1: timer, question list card and settings menu
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "时间：%02d:%02d".format(elapsed / 60, elapsed % 60),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (isFavorite) {
                    favoriteViewModel.removeFavorite(question.id)
                } else {
                    favoriteViewModel.addFavorite(question)
                }
            }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏"
                )
            }
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
                    questionFontSize = (questionFontSize + 2).coerceAtMost(32f)
                    settingsViewModel.setFontSize(context, questionFontSize)
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("缩小字体") }, onClick = {
                    questionFontSize = (questionFontSize - 2).coerceAtLeast(14f)
                    settingsViewModel.setFontSize(context, questionFontSize)
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("清除进度") }, onClick = {
                    viewModel.clearProgress()
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
                        val answered = selectedOptions.getOrElse(idx) { -1 } != -1
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    if (answered) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    else Color.Transparent
                                )
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
        }

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
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            item {
                Text(
                    text = question.content,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = questionFontSize.sp,
                        lineHeight = (questionFontSize * 1.3f).sp,
                        fontFamily = LocalFontFamily.current
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            val handleSelect: (Int) -> Unit = { idx ->
                android.util.Log.d("ExamScreen", "handleSelect index=$idx current=$currentIndex")
                viewModel.selectOption(idx)
                if (question.type == "单选题" || question.type == "判断题") {
                    autoJob?.cancel()
                    autoJob = coroutineScope.launch {
                        if (examDelay > 0) kotlinx.coroutines.delay(examDelay * 1000L)
                        if (currentIndex < questions.size - 1) {
                            viewModel.nextQuestion()
                        } else {
                            if (selectedOptions.any { it == -1 }) {
                                showExitDialog = true
                            } else {
                                val score = viewModel.gradeExam()
                                onExamEnd(score, questions.size)
                            }
                        }
                    }
                }
            }

            itemsIndexed(question.options) { idx, option ->
                val selectedOption = selectedOptions.getOrElse(currentIndex) { -1 }
                val correctIndex = answerLetterToIndex(question.answer)
                val isSelected = selectedOption == idx
                val isCorrect = showResult && correctIndex != null && idx == correctIndex
                val isWrong = showResult && isSelected && !isCorrect
                val backgroundColor = when {
                    isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    isWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(backgroundColor)
                        .clickable(enabled = !showResult) { handleSelect(idx) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { handleSelect(idx) },
                        enabled = !showResult
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        modifier = Modifier.weight(1f),
                        fontSize = questionFontSize.sp,
                        lineHeight = (questionFontSize * 1.3f).sp,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }

            if (showResult) {
                item {
                    val correctIndex = answerLetterToIndex(question.answer)
                    val correct = selectedOption == correctIndex
                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(8.dp)
                    ) {

                        Text(
                            if (correct) "回答正确！" else "回答错误，正确答案：${if (correctIndex != null && correctIndex in question.options.indices) question.options[correctIndex] else question.answer}",
                            color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                    if (question.explanation.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "解析：${question.explanation}",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

        }


        Spacer(modifier = Modifier.height(8.dp))
    }


if (showExitDialog) {
    AlertDialog(
        onDismissRequest = { showExitDialog = false },
        confirmButton = {
            TextButton(onClick = {
                coroutineScope.launch {
                    val score = viewModel.gradeExam()
                    showExitDialog = false
                    onExamEnd(score, questions.size)
                }
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = { showExitDialog = false }) { Text("取消") }
        },
        text = { Text(if (selectedOptions.any { it == -1 }) "还未答完题，是否交卷？" else "确定交卷？") }
    )
}}