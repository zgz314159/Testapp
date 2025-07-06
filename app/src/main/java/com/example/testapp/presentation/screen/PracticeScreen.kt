package com.example.testapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.PracticeViewModel
import com.example.testapp.presentation.screen.SettingsViewModel
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun PracticeScreen(
    quizId: String = "default",
    isWrongBookMode: Boolean = false,
    wrongBookFileName: String? = null,
    isFavoriteMode: Boolean = false,
    favoriteFileName: String? = null,
    viewModel: PracticeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onQuizEnd: (score: Int, total: Int) -> Unit = { _, _ -> },
    onSubmit: (Boolean) -> Unit = {}
) {
    val randomPractice by settingsViewModel.randomPractice.collectAsState()
    LaunchedEffect(quizId, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName, randomPractice) {
        viewModel.setRandomPractice(randomPractice)
        if (isWrongBookMode && wrongBookFileName != null) {
            // 使用独立的进度 id，避免与普通练习冲突，并跳过题库加载
            viewModel.setProgressId("wrongbook_${wrongBookFileName}", loadQuestions = false)
            viewModel.loadWrongQuestions(wrongBookFileName)
        } else if (isFavoriteMode && favoriteFileName != null) {
            viewModel.setProgressId("favorite_${favoriteFileName}", loadQuestions = false)
            viewModel.loadFavoriteQuestions(favoriteFileName)
        } else {
            viewModel.setProgressId(quizId)
        }
    }
    val questions by viewModel.questions.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val fontStyle by settingsViewModel.fontStyle.collectAsState()
    val correctDelay by settingsViewModel.correctDelay.collectAsState()
    val wrongDelay by settingsViewModel.wrongDelay.collectAsState()
    val context = LocalContext.current
    val currentIndex by viewModel.currentIndex.collectAsState()
    val answeredList by viewModel.answeredList.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val showResultList by viewModel.showResultList.collectAsState()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    val favoriteQuestions by favoriteViewModel.favoriteQuestions.collectAsState()
    val question = questions.getOrNull(currentIndex)
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
    var score by remember { mutableStateOf(0) }
    var questionFontSize by remember { mutableStateOf(fontSize) }
    var autoJob by remember { mutableStateOf<Job?>(null) }

    val selectedOption = selectedOptions.getOrNull(currentIndex) ?: -1
    val showResult = showResultList.getOrNull(currentIndex) ?: false
    val wrongBookViewModel: WrongBookViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

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

    var dragAmount by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount -> dragAmount += amount },
                    onDragEnd = {
                        if (dragAmount > 100f && currentIndex > 0) {
                            autoJob?.cancel()
                            viewModel.updateShowResult(currentIndex, showResult)
                            viewModel.prevQuestion()
                        } else if (dragAmount < -100f) {
                            autoJob?.cancel()
                            viewModel.updateShowResult(currentIndex, showResult)
                            if (currentIndex < questions.size - 1) {
                                viewModel.nextQuestion()
                            } else {
                                onQuizEnd(score, questions.size)
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
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("缩小字体") }, onClick = {
                    questionFontSize = (questionFontSize - 2).coerceAtLeast(14f)
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("清除进度") }, onClick = {
                    viewModel.clearProgress()
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Layer 2: type and progress
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
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
                    fontSize = questionFontSize.sp,
                    lineHeight = (questionFontSize * 1.3f).sp,
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
            val handleSelect: (Int) -> Unit = { idx ->
                viewModel.answerQuestion(idx)
                if (!showResult && (question.type == "单选题" || question.type == "判断题")) {
                    viewModel.updateShowResult(currentIndex, true)
                    val correctIndex = answerLetterToIndex(question.answer)
                    val correct = idx == correctIndex
                    if (!correct) {
                        coroutineScope.launch {
                            try {
                                wrongBookViewModel.addWrongQuestion(
                                    com.example.testapp.domain.model.WrongQuestion(
                                        question,
                                        idx
                                    )
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("PracticeScreen", "保存错题失败:${'$'}{e.message}")
                            }
                        }
                    } else {
                        score++
                    }
                    onSubmit(correct)
                    autoJob?.cancel()
                    autoJob = coroutineScope.launch {
                        val d = if (correct) correctDelay else wrongDelay
                        if (d > 0) kotlinx.coroutines.delay(d * 1000L)
                        if (viewModel.currentIndex.value < questions.size - 1) {
                            viewModel.updateShowResult(viewModel.currentIndex.value, true)
                            viewModel.nextQuestion()
                        } else {
                            viewModel.updateShowResult(viewModel.currentIndex.value, true)
                            onQuizEnd(score, questions.size)
                        }
                    }
                }
            }
            question.options.forEachIndexed { idx, option ->
                val correctIndex = answerLetterToIndex(question.answer)
                val isCorrect = showResult && correctIndex != null && idx == correctIndex
                val isSelected = selectedOption == idx
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
                    Text(
                        option,
                        fontSize = questionFontSize.sp,
                        lineHeight = (questionFontSize * 1.3f).sp,
                        fontFamily = LocalFontFamily.current
                    )
                }

            }


            if (showResult) {
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

            // Layer 4: answer buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                if (question.type != "单选题" && question.type != "判断题") {
                    Button(
                        onClick = {
                            autoJob?.cancel()
                            viewModel.updateShowResult(currentIndex, true)
                            val correctIndex = answerLetterToIndex(question.answer)
                            val correct = selectedOption == correctIndex
                            if (!correct && selectedOption != -1) {
                                coroutineScope.launch {
                                    try {
                                        wrongBookViewModel.addWrongQuestion(
                                            com.example.testapp.domain.model.WrongQuestion(
                                                question,
                                                selectedOption
                                            )
                                        )
                                    } catch (e: Exception) {
                                        android.util.Log.e(
                                            "PracticeScreen",
                                            "保存错题失败:${'$'}{e.message}"
                                        )
                                    }
                                }
                            }
                            if (correct) score++
                            onSubmit(correct)
                            autoJob = coroutineScope.launch {
                                val d = if (correct) correctDelay else wrongDelay
                                if (d > 0) kotlinx.coroutines.delay(d * 1000L)
                                if (viewModel.currentIndex.value < questions.size - 1) {
                                    viewModel.updateShowResult(viewModel.currentIndex.value, true)
                                    viewModel.nextQuestion()
                                } else {
                                    viewModel.updateShowResult(viewModel.currentIndex.value, true)
                                    onQuizEnd(score, questions.size)
                                }
                            }
                        },
                        enabled = selectedOption != -1 && !showResult,
                    ) {
                        Text(
                            "提交答案",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
                if (currentIndex == questions.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        autoJob?.cancel()
                        onQuizEnd(score, questions.size)
                    }) {
                        Text(
                            "交卷",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
            }


        }
    }}

// 工具函数：将字母答案转为索引
private fun answerLetterToIndex(answer: String): Int? {
    return answer.trim().uppercase().firstOrNull()?.let { it - 'A' }
}

