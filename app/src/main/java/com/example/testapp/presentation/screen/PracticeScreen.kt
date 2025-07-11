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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.PracticeViewModel
import com.example.testapp.presentation.screen.SettingsViewModel
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.example.testapp.util.answerLetterToIndex



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
    val practiceCount by settingsViewModel.practiceQuestionCount.collectAsState()
    LaunchedEffect(quizId, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName, randomPractice, practiceCount) {
        viewModel.setRandomPractice(randomPractice)
        if (isWrongBookMode && wrongBookFileName != null) {
            // 使用独立的进度 id，避免与普通练习冲突，并跳过题库加载
            viewModel.setProgressId(
                id = "wrongbook_${wrongBookFileName}",
                questionsId = wrongBookFileName,
                loadQuestions = false
            )
            viewModel.loadWrongQuestions(wrongBookFileName)
        } else if (isFavoriteMode && favoriteFileName != null) {
            viewModel.setProgressId(
                id = "favorite_${favoriteFileName}",
                questionsId = favoriteFileName,
                loadQuestions = false
            )
            viewModel.loadFavoriteQuestions(favoriteFileName)
        } else {
            // 普通练习模式使用 "practice_" 前缀，避免与其他模式的进度混淆
            viewModel.setProgressId(id = quizId, questionsId = quizId, questionCount = practiceCount)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val wrongBookViewModel: WrongBookViewModel = hiltViewModel()
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
    android.util.Log.d(
        "PracticeScreen-question",
        "currentIndex=$currentIndex, question=$question"
    )
    val selectedOption = selectedOptions.getOrNull(currentIndex) ?: emptyList<Int>()
    val showResult = showResultList.getOrNull(currentIndex) ?: false
    android.util.Log.d(
        "PracticeScreen-selected",
        "currentIndex=$currentIndex, selectedOption=$selectedOption, showResult=$showResult"
    )
    val isFavorite = remember(question, favoriteQuestions) {
        question != null && favoriteQuestions.any { it.question.id == question.id }
    }
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(currentIndex) {
        elapsed = 0
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsed += 1
        }
    }
    var showList by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var questionFontSize by remember(fontSize) { mutableStateOf(fontSize) }
    var autoJob by remember { mutableStateOf<Job?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedOption, showResult, currentIndex, answeredList, selectedOptions, showResultList, progressLoaded) {
        android.util.Log.d(
            "PracticeScreen-LaunchedEffect",
            "currentIndex=$currentIndex, selectedOption=$selectedOption, showResult=$showResult, answeredList=$answeredList, selectedOptions=$selectedOptions, showResultList=$showResultList, progressLoaded=$progressLoaded"
        )
    }
    BackHandler {
        if (answeredList.size >= questions.size) {
            autoJob?.cancel()
            onQuizEnd(score, questions.size)
        } else {
            showExitDialog = true
        }
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
                           // viewModel.updateShowResult(currentIndex, showResult)
                            viewModel.prevQuestion()
                        } else if (dragAmount < -100f) {
                            autoJob?.cancel()
                           // viewModel.updateShowResult(currentIndex, showResult)
                            if (currentIndex < questions.size - 1) {
                                viewModel.nextQuestion()
                            } else {
                                if (answeredList.size >= questions.size) {
                                    autoJob?.cancel()
                                    onQuizEnd(score, questions.size)
                                } else {
                                    showExitDialog = true
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
                    modifier = Modifier.size(24.dp),
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
                        // val answered = answeredList.contains(idx)

                        // 取出该题是否已显示解析 & 用户选项 & 正确答案
                        val resultShown = showResultList.getOrNull(idx) == true
                        val selected = selectedOptions.getOrNull(idx) ?: emptyList<Int>()
                        val q = questions.getOrNull(idx)
                        val correctIdx = q?.let { answerLetterToIndex(it.answer) }

                        // 决定背景色：绿色=答对，红色=答错，灰=已答未看解析，透明=未答
                        val bgColor = when {
                            resultShown && selected.singleOrNull() == correctIdx ->
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

                            resultShown && selected.isNotEmpty() && selected.singleOrNull() != correctIdx ->
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)

                            idx in answeredList ->
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)

                            else ->
                                Color.Transparent
                        }


                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(bgColor)
//                                    if (answered) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
//                                    else Color.Transparent
//                                )
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

        // 2. 计算正确答案下标列表
        val correctIndices: List<Int> = question.answer
            .filter { it.isLetter() }
            .mapNotNull { answerLetterToIndex(it.toString()) }

        // Layer 3: question and options
        Column(modifier = Modifier.weight(1f)) {
            // 题干
            Text(
                text = question.content,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = questionFontSize.sp,
                    lineHeight = (questionFontSize * 1.3f).sp,
                    fontFamily = LocalFontFamily.current
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1. 声明 handleSelect
            val handleSelect: (Int) -> Unit = { idx ->
                if (question.type == "单选题" || question.type == "判断题") {
                    // 单选/判断题逻辑不变
                    viewModel.answerQuestion(idx)
                    val correctIdx = answerLetterToIndex(question.answer)
                    val correct = idx == correctIdx
                    if (!correct) {
                        coroutineScope.launch {
                            wrongBookViewModel.addWrongQuestion(
                                com.example.testapp.domain.model.WrongQuestion(
                                    question,
                                    listOf(idx)
                                )
                            )
                        }
                    }
                    onSubmit(correct)
                    autoJob?.cancel()
                    autoJob = coroutineScope.launch {
                        val d = if (correct) correctDelay else wrongDelay
                        if (d > 0) kotlinx.coroutines.delay(d * 1000L)
                        viewModel.updateShowResult(currentIndex, true)
                        if (currentIndex < questions.size - 1) viewModel.nextQuestion()
                        else if (answeredList.size >= questions.size) onQuizEnd(
                            score,
                            questions.size
                        )
                        else showExitDialog = true
                    }
                } else {
                    // 多选题：切换选项状态
                    viewModel.toggleOption(idx)
                }
            }


            // 3. 渲染所有选项（单次循环）
            question.options.forEachIndexed { idx, option ->
                val isSelected = selectedOption.contains(idx)
                val isCorrect = showResult && correctIndices.contains(idx)
                val isWrong = showResult && isSelected && !isCorrect
                val bgColor = when {
                    isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    isWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(bgColor)
                        .clickable(enabled = !showResult) { handleSelect(idx) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (question.type == "多选题") {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { handleSelect(idx) },
                            enabled = !showResult
                        )
                    } else {
                        RadioButton(
                            selected = isSelected,
                            onClick = { handleSelect(idx) },
                            enabled = !showResult
                        )
                    }
                    Text(
                        text = option,
                        fontSize = questionFontSize.sp,
                        lineHeight = (questionFontSize * 1.3f).sp,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }

            // 4. 解析区
            if (showResult) {
                val correctText = correctIndices.joinToString("、") { question.options[it] }
                val allCorrect = selectedOption.toSet() == correctIndices.toSet()
                // ---- 答题结果显示（用 primaryContainer，偏蓝/主色调）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD0E8FF)) // 明显蓝色
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (allCorrect) "回答正确！" else "回答错误，正确答案：$correctText",
                        color = if (allCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
                if (question.explanation.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF5C0)) // 明显黄色
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "解析：" + if (question.explanation.isNotBlank()) question.explanation else "本题暂无解析",
                            color = Color(0xFF835C00), // 深点的黄棕色，看着和底色区分开
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Layer 4: 多选题的提交按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (question.type == "多选题") {
                Button(
                    onClick = {
                        autoJob?.cancel()
                        viewModel.updateShowResult(currentIndex, true)
                        val allCorrect = selectedOption.toSet() == correctIndices.toSet()
                        if (!allCorrect && selectedOption.isNotEmpty()) {
                            coroutineScope.launch {
                                wrongBookViewModel.addWrongQuestion(
                                    com.example.testapp.domain.model.WrongQuestion(
                                        question, selectedOption
                                    )
                                )
                            }
                        }
                        if (allCorrect) score++
                        onSubmit(allCorrect)
                        autoJob = coroutineScope.launch {
                            val d = if (allCorrect) correctDelay else wrongDelay
                            if (d > 0) kotlinx.coroutines.delay(d * 1000L)
                            if (currentIndex < questions.size - 1) viewModel.nextQuestion()
                            else if (answeredList.size >= questions.size) onQuizEnd(
                                score,
                                questions.size
                            )
                            else showExitDialog = true
                        }
                    },
                    enabled = selectedOption.isNotEmpty() && !showResult
                ) {
                    Text(
                        "提交答案",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }
    }



    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    autoJob?.cancel()
                    showExitDialog = false
                    onQuizEnd(score, questions.size)
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("取消") }
            },
            text = {
                Text(if (answeredList.size < questions.size) "还有未答题目，是否交卷？" else "确定交卷？")
            }
        )
    }
}
