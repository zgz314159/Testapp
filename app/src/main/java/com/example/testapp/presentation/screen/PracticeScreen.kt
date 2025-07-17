package com.example.testapp.presentation.screen

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.AnswerCardGrid
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.util.answerLetterToIndex
import com.example.testapp.util.rememberSoundEffects
import com.example.testapp.presentation.screen.SparkViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    quizId: String = "default",
    isWrongBookMode: Boolean = false,
    wrongBookFileName: String? = null,
    isFavoriteMode: Boolean = false,
    favoriteFileName: String? = null,
    viewModel: PracticeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    aiViewModel: DeepSeekViewModel = hiltViewModel(),
    sparkViewModel: SparkViewModel = hiltViewModel(),
    onQuizEnd: (score: Int, total: Int) -> Unit = { _, _ -> },
    onSubmit: (Boolean) -> Unit = {},
    onExitWithoutAnswer: () -> Unit = {},
    onViewDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewSpark: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    // --- 各种状态和依赖 ---
    val randomPractice by settingsViewModel.randomPractice.collectAsState()
    val practiceCount by settingsViewModel.practiceQuestionCount.collectAsState()
    LaunchedEffect(quizId, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName, randomPractice, practiceCount) {
        viewModel.setRandomPractice(randomPractice)
        if (isWrongBookMode && wrongBookFileName != null) {
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
            viewModel.setProgressId(id = quizId, questionsId = quizId, questionCount = practiceCount)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val wrongBookViewModel: WrongBookViewModel = hiltViewModel()
    val questions by viewModel.questions.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val correctDelay by settingsViewModel.correctDelay.collectAsState()
    val wrongDelay by settingsViewModel.wrongDelay.collectAsState()
    val context = LocalContext.current
    val soundEffects = rememberSoundEffects()
    val soundEnabled by settingsViewModel.soundEnabled.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val answeredList by viewModel.answeredList.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val showResultList by viewModel.showResultList.collectAsState()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    val favoriteQuestions by favoriteViewModel.favoriteQuestions.collectAsState()
    val question = questions.getOrNull(currentIndex)
    val analysisPair by aiViewModel.analysis.collectAsState()
    val sparkPair by sparkViewModel.analysis.collectAsState()
    val analysisList by viewModel.analysisList.collectAsState()
    val sparkAnalysisList by viewModel.sparkAnalysisList.collectAsState()
    val analysisText = if (analysisPair?.first == currentIndex) analysisPair?.second else analysisList.getOrNull(currentIndex)
    val sparkText = if (sparkPair?.first == currentIndex) sparkPair?.second else sparkAnalysisList.getOrNull(currentIndex)
    val noteList by viewModel.noteList.collectAsState()
    val hasDeepSeekAnalysis = analysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val hasNote = noteList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val selectedOption = selectedOptions.getOrNull(currentIndex) ?: emptyList<Int>()
    val showResult = showResultList.getOrNull(currentIndex) ?: false
    val isFavorite = remember(question, favoriteQuestions) {
        question != null && favoriteQuestions.any { it.question.id == question.id }
    }
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(currentIndex) {
        aiViewModel.clear()
        sparkViewModel.clear()
        elapsed = 0
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsed += 1
        }
    }
    LaunchedEffect(question) {
        question?.let {
            val saved = aiViewModel.getSavedAnalysis(it.id) ?: ""
            if (saved.isNotBlank()) {
                viewModel.updateAnalysis(currentIndex, saved)
            }
            val sparkSaved = sparkViewModel.getSavedAnalysis(it.id) ?: ""
            if (sparkSaved.isNotBlank()) {
                viewModel.updateSparkAnalysis(currentIndex, sparkSaved)
            }
        }
    }
    var showList by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    val storedPracticeFontSize by FontSettingsDataStore
        .getPracticeFontSize(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    var questionFontSize by remember { mutableStateOf(fontSize) }
    var fontLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(storedPracticeFontSize) {
        if (!storedPracticeFontSize.isNaN()) {
            questionFontSize = storedPracticeFontSize
            fontLoaded = true
        }
    }
    LaunchedEffect(questionFontSize, fontLoaded) {
        if (fontLoaded) {
            FontSettingsDataStore.setPracticeFontSize(context, questionFontSize)
        }
    }
    var autoJob by remember { mutableStateOf<Job?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var answeredThisSession by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    LaunchedEffect(progressLoaded) {
        if (progressLoaded) answeredThisSession = false
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
    BackHandler {
        when {
            !answeredThisSession -> {
                autoJob?.cancel()
                onExitWithoutAnswer()
            }
            answeredList.size >= questions.size -> {
                autoJob?.cancel()
                viewModel.addHistoryRecord(score, questions.size)
                onQuizEnd(score, questions.size)
            }
            else -> showExitDialog = true
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
                            viewModel.prevQuestion()
                        } else if (dragAmount < -100f) {
                            autoJob?.cancel()
                            if (currentIndex < questions.size - 1) {
                                viewModel.nextQuestion()
                            } else {
                                when {
                                    !answeredThisSession -> {
                                        autoJob?.cancel()
                                        onExitWithoutAnswer()
                                    }
                                    answeredList.size >= questions.size -> {
                                        autoJob?.cancel()
                                        viewModel.addHistoryRecord(score, questions.size)
                                        onQuizEnd(score, questions.size)
                                    }
                                    else -> showExitDialog = true
                                }
                            }
                        }
                        dragAmount = 0f
                    },
                    onDragCancel = { dragAmount = 0f }
                )
            }
    ) {
        // Top bar
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
                if (question != null) {
                    if (!showResult) {
                        answeredThisSession = true
                        viewModel.updateShowResult(currentIndex, true)
                    }
                    if (hasDeepSeekAnalysis) {
                        onViewDeepSeek(analysisText ?: "", question.id, currentIndex)
                    } else {
                        aiViewModel.analyze(currentIndex, question)
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI 解析",
                    tint = if (hasDeepSeekAnalysis) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = {
                if (question != null) {
                    if (!showResult) {
                        answeredThisSession = true
                        viewModel.updateShowResult(currentIndex, true)
                    }
                    sparkViewModel.analyze(currentIndex, question)
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "Spark AI",
                    tint = MaterialTheme.colorScheme.secondary
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
                        FontSettingsDataStore.setPracticeFontSize(context, questionFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("缩小字体") }, onClick = {
                    questionFontSize = (questionFontSize - 2).coerceAtLeast(14f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setPracticeFontSize(context, questionFontSize)
                    }
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

        // 正确答案下标列表
        val correctIndices: List<Int> = question.answer
            .filter { it.isLetter() }
            .mapNotNull { answerLetterToIndex(it.toString()) }

        // 选项
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
                    .clickable(enabled = !showResult) {
                        answeredThisSession = true
                        if (question.type == "单选题" || question.type == "判断题") {
                            viewModel.answerQuestion(idx)
                            val correctIdx = answerLetterToIndex(question.answer)
                            val correct = idx == correctIdx
                            if (soundEnabled) {
                                if (correct) soundEffects.playCorrect() else soundEffects.playWrong()
                            }
                            if (!correct) {
                                coroutineScope.launch {
                                    wrongBookViewModel.addWrongQuestion(
                                        com.example.testapp.domain.model.WrongQuestion(
                                            question, listOf(idx)
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
                                else if (answeredList.isEmpty()) onExitWithoutAnswer()
                                else if (answeredList.size >= questions.size) {
                                    viewModel.addHistoryRecord(score, questions.size)
                                    onQuizEnd(score, questions.size)
                                }
                                else showExitDialog = true
                            }
                        } else {
                            viewModel.toggleOption(idx)
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
                            viewModel.toggleOption(idx)
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

        // 解析/结果区
        if (showResult) {
            val correctText = correctIndices.joinToString("、") { question.options[it] }
            val allCorrect = selectedOption.toSet() == correctIndices.toSet()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD0E8FF))
                    .padding(8.dp)
            ) {
                Text(
                    text = if (allCorrect) "回答正确！" else "回答错误，正确答案：$correctText",
                    color = if (allCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontSize = questionFontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            }
            if (question.explanation.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0FFE0))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "笔记：$note",
                        color = Color(0xFF004B00),
                        fontSize = questionFontSize.sp,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (!analysisText.isNullOrBlank() || !sparkText.isNullOrBlank()) {
                if (!analysisText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .verticalScroll(rememberScrollState())
                            .background(Color(0xFFE8F6FF))
                            .padding(8.dp)
                            .pointerInput(analysisText) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        question?.let { q ->
                                            onViewDeepSeek(analysisText!!, q.id, currentIndex)
                                        }
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .verticalScroll(rememberScrollState())
                            .background(Color(0xFFEDE7FF))
                            .padding(8.dp)
                            .pointerInput(sparkText) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        question?.let { q ->
                                            onViewSpark(sparkText!!, q.id, currentIndex)
                                        }
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
                Spacer(modifier = Modifier.weight(1f, fill = true))
            }
        } else {
            Spacer(modifier = Modifier.weight(1f, fill = true))
        }

        // “提交答案”按钮（多选题且未提交才显示）
        if (question.type == "多选题" && !showResult) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        autoJob?.cancel()
                        viewModel.updateShowResult(currentIndex, true)
                        val allCorrect = selectedOption.toSet() == correctIndices.toSet()
                        if (soundEnabled) {
                            if (allCorrect) soundEffects.playCorrect() else soundEffects.playWrong()
                        }
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
                            else if (answeredList.isEmpty()) onExitWithoutAnswer()
                            else if (answeredList.size >= questions.size) onQuizEnd(score, questions.size)
                            else showExitDialog = true
                        }
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
                    autoJob?.cancel()
                    showExitDialog = false
                    viewModel.addHistoryRecord(score, questions.size)
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
