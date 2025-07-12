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
import com.example.testapp.data.datastore.FontSettingsDataStore




@Composable
fun ExamScreen(
    quizId: String,
    viewModel: ExamViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onExamEnd: (score: Int, total: Int) -> Unit = { _, _ -> },
    onExitWithoutAnswer: () -> Unit = {}
) {
    val examCount by settingsViewModel.examQuestionCount.collectAsState()
    val randomExam by settingsViewModel.randomExam.collectAsState()
    LaunchedEffect(quizId, examCount, randomExam) { viewModel.loadQuestions(quizId, examCount, randomExam) }
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val showResultList by viewModel.showResultList.collectAsState()
    val finished by viewModel.finished.collectAsState()


    // 关键：DisposableEffect 用于退出界面时自动判卷保存
    DisposableEffect(Unit) {
        onDispose {
            if (
                progressLoaded &&
                !finished &&
                selectedOptions.any { it.isNotEmpty() }
            ) {
                kotlinx.coroutines.runBlocking {
                    viewModel.gradeExam()
                }
            }
        }
    }

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

    LaunchedEffect(currentIndex) {
        elapsed = 0
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsed += 1
        }
    }

    LaunchedEffect(quizId, examCount, randomExam, progressLoaded) {
        if (!progressLoaded) {
            viewModel.loadQuestions(quizId, examCount, randomExam)
        }
    }


    var showList by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val storedExamFontSize by FontSettingsDataStore
        .getExamFontSize(context)
        .collectAsState(initial = fontSize)
    android.util.Log.d(
        "ExamScreen-font",
        "storedExamFontSize=$storedExamFontSize globalFontSize=$fontSize"
    )
    var questionFontSize by remember { mutableStateOf(storedExamFontSize) }
    LaunchedEffect(storedExamFontSize) {
        android.util.Log.d(
            "ExamScreen-font",
            "loaded storedExamFontSize=$storedExamFontSize"
        )
        questionFontSize = storedExamFontSize
    }
    LaunchedEffect(questionFontSize) {
        android.util.Log.d(
            "ExamScreen-font",
            "save exam questionFontSize=$questionFontSize"
        )
        FontSettingsDataStore.setExamFontSize(context, questionFontSize)
    }
    var dragAmount by remember { mutableStateOf(0f) }
    var autoJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var containerWidth by remember { mutableStateOf(0f) }
    var dragStartX by remember { mutableStateOf(0f) }
    var showExitDialog by remember { mutableStateOf(false) }
    var answeredThisSession by remember { mutableStateOf(false) }

    LaunchedEffect(progressLoaded) {
        if (progressLoaded) answeredThisSession = false
    }

    BackHandler {
        val hasAnswered = answeredThisSession
        val hasUnanswered = selectedOptions.any { it.isEmpty() }
        when {
            !hasAnswered -> onExitWithoutAnswer()
            hasUnanswered -> showExitDialog = true
            else -> {
                coroutineScope.launch {
                    val score = viewModel.gradeExam()
                    onExamEnd(score, questions.size)
                }
            }
        }
    }
    val selectedOption = selectedOptions.getOrElse(currentIndex) { emptyList<Int>() }
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

                            when {
                                !answeredThisSession -> onExitWithoutAnswer()
                                selectedOptions.any { it.isEmpty() } -> showExitDialog = true
                                else -> {
                                    coroutineScope.launch {
                                        val score = viewModel.gradeExam()
                                        onExamEnd(score, questions.size)
                                    }
                                }
                            }
                        } else {
                            if (dragAmount > 100f && currentIndex > 0) {
                                viewModel.prevQuestion()
                            } else if (dragAmount < -100f && currentIndex < questions.size - 1) {
                                viewModel.nextQuestion()
                            } else if (dragAmount < -100f) {

                                when {
                                    !answeredThisSession -> onExitWithoutAnswer()
                                    selectedOptions.any { it.isEmpty() } -> showExitDialog = true
                                    else -> {
                                        coroutineScope.launch {
                                            val score = viewModel.gradeExam()
                                            onExamEnd(score, questions.size)
                                        }
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
                    coroutineScope.launch {
                        FontSettingsDataStore.setExamFontSize(context, questionFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("缩小字体") }, onClick = {
                    questionFontSize = (questionFontSize - 2).coerceAtLeast(14f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setExamFontSize(context, questionFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("清除进度") }, onClick = {
                    viewModel.clearProgress()
                    menuExpanded = false
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
                        val resultShown = showResultList.getOrNull(idx) == true
                        val selected = selectedOptions.getOrNull(idx) ?: emptyList<Int>()
                        val q = questions.getOrNull(idx)
                        val correctIdx = q?.let { answerLetterToIndex(it.answer) }

                        val bgColor = when {
                            resultShown && selected.singleOrNull() == correctIdx ->
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            resultShown && selected.isNotEmpty() && selected.singleOrNull() != correctIdx ->
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            selected.isNotEmpty() ->
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .background(bgColor)
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
                answeredThisSession = true
                viewModel.selectOption(idx)
                if (question.type == "单选题" || question.type == "判断题") {
                    //viewModel.updateShowResult(currentIndex, true)
                    autoJob?.cancel()
                    autoJob = coroutineScope.launch {
                        if (examDelay > 0) kotlinx.coroutines.delay(examDelay * 1000L)
                        if (currentIndex < questions.size - 1) {
                            viewModel.nextQuestion()
                        } else {

                            when {
                                !answeredThisSession -> onExitWithoutAnswer()
                                selectedOptions.any { it.isEmpty() } -> showExitDialog = true
                                else -> {
                                    coroutineScope.launch {
                                        val score = viewModel.gradeExam()
                                        onExamEnd(score, questions.size)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            itemsIndexed(question.options) { idx, option ->
                val selectedOption = selectedOptions.getOrElse(currentIndex) { emptyList<Int>() }
                val correctIndex = answerLetterToIndex(question.answer)
                val isSelected = selectedOption.contains(idx)
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
                    val correct = correctIndex != null && selectedOption.contains(correctIndex)
                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD0E8FF)) // 明显蓝色
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
                    //showExitDialog = false
                    onExamEnd(score, questions.size)
                }
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = { showExitDialog = false }) { Text("取消") }
        },
        text = { Text(if (selectedOptions.any { it.isEmpty() }) "还未答完题，是否交卷？" else "确定交卷？") }
    )
}}