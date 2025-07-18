package com.example.testapp.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.AnswerCardGrid
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.util.answerLettersToIndices
import com.example.testapp.presentation.screen.SparkViewModel
import kotlinx.coroutines.launch

@Composable
fun ExamScreen(
    quizId: String,
    viewModel: ExamViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    aiViewModel: DeepSeekViewModel = hiltViewModel(),
    sparkViewModel: SparkViewModel = hiltViewModel(),
    onExamEnd: (score: Int, total: Int) -> Unit = { _, _ -> },
    onExitWithoutAnswer: () -> Unit = {},
    onViewDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewSpark: (String, Int, Int) -> Unit = { _, _, _ -> }
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
    val noteList by viewModel.noteList.collectAsState()
    val analysisPair by aiViewModel.analysis.collectAsState()
    val sparkPair by sparkViewModel.analysis.collectAsState()
    val analysisList by viewModel.analysisList.collectAsState()
    val sparkAnalysisList by viewModel.sparkAnalysisList.collectAsState()
    val analysisText = if (analysisPair?.first == currentIndex) analysisPair?.second else analysisList.getOrNull(currentIndex)
    val hasDeepSeekAnalysis = analysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val sparkText = if (sparkPair?.first == currentIndex) sparkPair?.second else sparkAnalysisList.getOrNull(currentIndex)
    val hasSparkAnalysis = sparkAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val hasNote = noteList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    val favoriteQuestions by favoriteViewModel.favoriteQuestions.collectAsState()
    val isFavorite = remember(question, favoriteQuestions) {
        question != null && favoriteQuestions.any { it.question.id == question.id }
    }
    var elapsed by remember { mutableStateOf(0) }
    // Accumulate elapsed time across all questions
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsed += 1
        }
    }
    LaunchedEffect(currentIndex) {
        sparkViewModel.clear()
    }
    LaunchedEffect(question) {
        if (question != null) {
            val saved = aiViewModel.getSavedAnalysis(question.id) ?: ""
            if (saved.isNotBlank()) {
                viewModel.updateAnalysis(currentIndex, saved)
            }
            val sparkSaved = sparkViewModel.getSavedAnalysis(question.id) ?: ""
            if (sparkSaved.isNotBlank()) {
                viewModel.updateSparkAnalysis(currentIndex, sparkSaved)
            }
        }
    }
    LaunchedEffect(analysisPair) {
        val pair = analysisPair
        if (pair != null && pair.second != "解析中...") {
            viewModel.updateAnalysis(pair.first, pair.second)
        }
    }
    LaunchedEffect(sparkPair) {
        val pair = sparkPair
        if (pair != null && pair.second != "解析中...") {
            viewModel.updateSparkAnalysis(pair.first, pair.second)
        }
    }
    LaunchedEffect(quizId, examCount, randomExam, progressLoaded) {
        if (!progressLoaded) {
            viewModel.loadQuestions(quizId, examCount, randomExam)
        }
    }

    var showList by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val storedExamFontSize by FontSettingsDataStore
        .getExamFontSize(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    var questionFontSize by remember { mutableStateOf(fontSize) }
    var fontLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(storedExamFontSize) {
        if (!storedExamFontSize.isNaN()) {
            questionFontSize = storedExamFontSize
            fontLoaded = true
        }
    }
    LaunchedEffect(questionFontSize, fontLoaded) {
        if (fontLoaded) {
            FontSettingsDataStore.setExamFontSize(context, questionFontSize)
        }
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

    if (question == null ||
        !progressLoaded ||
        showResultList.size != questions.size) {
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
                    onDragStart = { offset -> dragStartX = offset.x },
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
        // Top bar (timer, favorite, ai, note, settings...)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "%02d:%02d".format(elapsed / 60, elapsed % 60),
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
            IconButton(onClick = {
                if (!showResult) {
                    answeredThisSession = true
                    viewModel.updateShowResult(currentIndex, true)
                }
                if (hasDeepSeekAnalysis) {
                    onViewDeepSeek(analysisText ?: "", question.id, currentIndex)
                } else {
                    aiViewModel.analyze(currentIndex, question)
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI 解析",
                    tint = if (hasDeepSeekAnalysis) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = {
                if (!showResult) {
                    answeredThisSession = true
                    viewModel.updateShowResult(currentIndex, true)
                }
                if (hasSparkAnalysis) {
                    onViewSpark(sparkText ?: "", question.id, currentIndex)
                } else {
                    sparkViewModel.analyze(currentIndex, question)
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "Spark AI",
                    tint = if (hasSparkAnalysis) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    noteText = viewModel.getNote(question.id) ?: ""
                    showNoteDialog = true
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "笔记",
                    tint = if (hasNote) MaterialTheme.colorScheme.primary else LocalContentColor.current
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
                })
            }
        }

        if (showList) {
            AlertDialog(onDismissRequest = { showList = false }, confirmButton = {}, text = {
                val singleIndices = remember(questions) { questions.mapIndexedNotNull { i, q -> if (q.type == "单选题") i else null } }
                val multiIndices = remember(questions) { questions.mapIndexedNotNull { i, q -> if (q.type == "多选题") i else null } }
                val judgeIndices = remember(questions) { questions.mapIndexedNotNull { i, q -> if (q.type == "判断题") i else null } }

                Column(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (singleIndices.isNotEmpty()) {
                        Text("单选题", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                        AnswerCardGrid(
                            indices = singleIndices,
                            questions = questions,
                            selectedOptions = selectedOptions,
                            showResultList = showResultList,
                            onClick = {
                                viewModel.goToQuestion(it)
                                showList = false
                            }
                        )
                    }
                    if (multiIndices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("多选题", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                        AnswerCardGrid(
                            indices = multiIndices,
                            questions = questions,
                            selectedOptions = selectedOptions,
                            showResultList = showResultList,
                            onClick = {
                                viewModel.goToQuestion(it)
                                showList = false
                            }
                        )
                    }
                    if (judgeIndices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("判断题", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                        AnswerCardGrid(
                            indices = judgeIndices,
                            questions = questions,
                            selectedOptions = selectedOptions,
                            showResultList = showResultList,
                            onClick = {
                                viewModel.goToQuestion(it)
                                showList = false
                            }
                        )
                    }
                }
            })
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Type & progress bar
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

        // 选项
        question.options.forEachIndexed { idx, option ->
            val correctIndices = answerLettersToIndices(question.answer)
            val isSelected = selectedOption.contains(idx)
            val isCorrect = showResult && correctIndices.contains(idx)
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
                    .clickable(enabled = !showResult) {
                        answeredThisSession = true
                        viewModel.selectOption(idx)
                        if (question.type == "单选题" || question.type == "判断题") {
                            viewModel.updateShowResult(currentIndex, true) // 新增这一行！
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
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 给左右按钮都用Box，保证空间一致
                Box(
                    modifier = Modifier.size(40.dp), // 与Checkbox高度/宽度一致
                    contentAlignment = Alignment.Center
                ) {
                if (question.type == "多选题") {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            answeredThisSession = true
                            viewModel.selectOption(idx)
                        },
                        enabled = !showResult
                    )
                } else {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        enabled = !showResult
                    )
                }}
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

        // 答案解析/解析区
        if (showResult) {
            val correctIndices = answerLettersToIndices(question.answer)
            val correct = selectedOption.sorted() == correctIndices.sorted()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD0E8FF))
                    .padding(8.dp)
            ) {
                val answerText = if (correctIndices.all { it in question.options.indices }) {
                    correctIndices.joinToString("，") { question.options[it] }
                } else question.answer

                Text(
                    if (correct) "回答正确！" else "回答错误，正确答案：$answerText",
                    color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = questionFontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            }
            if (question.explanation.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0xFFFFF5C0))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "解析：" + question.explanation,
                        color = Color(0xFF835C00),
                        fontSize = questionFontSize.sp,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
            val note = noteList.getOrNull(currentIndex)
            if (!note.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0xFFE0FFE0))
                        .padding(8.dp)
                        .pointerInput(note) {
                            detectTapGestures(
                                onDoubleTap = {
                                    noteText = note
                                    showNoteDialog = true
                                },
                                onLongPress = { showDeleteNoteDialog = true }
                            )
                        }
                ) {
                    Text(
                        text = "笔记：$note",
                        color = Color(0xFF004B00),
                        fontSize = questionFontSize.sp,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
            if (!analysisText.isNullOrBlank() || !sparkText.isNullOrBlank()) {
                if (!analysisText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .background(Color(0xFFE8F6FF))
                            .padding(8.dp)
                            .pointerInput(analysisText) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        onViewDeepSeek(analysisText!!, question.id, currentIndex)
                                    },
                                    onLongPress = { showDeleteDialog = true }
                                )
                            }
                    ) {
                        Text(
                            text = analysisText ?: "",
                            color = Color(0xFF004B6B),
                            fontSize = questionFontSize.sp,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
                if (!sparkText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .background(Color(0xFFEDE7FF))
                            .padding(8.dp)
                            .pointerInput(sparkText) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        onViewSpark(sparkText!!, question.id, currentIndex)
                                    },
                                    onLongPress = { showDeleteDialog = true }
                                )
                            }
                    ) {
                        Text(
                            text = sparkText ?: "",
                            color = Color(0xFF3A006A),
                            fontSize = questionFontSize.sp,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // “提交答案”按钮
        if (question.type == "多选题" && !showResult) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        answeredThisSession = true
                        viewModel.updateShowResult(currentIndex, true)
                        // 你可以加分、统计等逻辑
                    },
                    enabled = selectedOption.isNotEmpty()
                ) {
                    Text(
                        "提交答案",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (noteText.isNotBlank()) viewModel.saveNote(question.id, currentIndex, noteText)
                showNoteDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    if (noteText.isNotBlank()) viewModel.saveNote(question.id, currentIndex, noteText)
                    showNoteDialog = false
                }) { Text("完成") }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("取消") }
            },
            text = { TextField(value = noteText, onValueChange = { noteText = it }) }
        )
    }
    if (showDeleteNoteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteNoteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveNote(question.id, currentIndex, "")
                    showDeleteNoteDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteNoteDialog = false }) { Text("取消") }
            },
            text = { Text("确定删除笔记吗？") }
        )
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    aiViewModel.clear()
                    sparkViewModel.clear()
                    viewModel.updateAnalysis(currentIndex, "")
                    viewModel.updateSparkAnalysis(currentIndex, "")
                    showDeleteDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            },
            text = { Text("确定删除解析吗？") }
        )
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        val score = viewModel.gradeExam()
                        onExamEnd(score, questions.size)
                    }
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("取消") }
            },
            text = { Text(if (selectedOptions.any { it.isEmpty() }) "还未答完题，是否交卷？" else "确定交卷？") }
        )
    }
}
