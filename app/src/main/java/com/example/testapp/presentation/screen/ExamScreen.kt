package com.example.testapp.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.AnswerCardGrid
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.util.answerLettersToIndices
import com.example.testapp.util.formatQuestionWithOptions
import com.example.testapp.presentation.screen.SparkViewModel
import kotlinx.coroutines.launch

@Composable
fun ExamScreen(
    quizId: String,
    isWrongBookMode: Boolean = false,
    wrongBookFileName: String? = null,
    isFavoriteMode: Boolean = false,
    favoriteFileName: String? = null,
    viewModel: ExamViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    aiViewModel: DeepSeekViewModel = hiltViewModel(),
    sparkViewModel: SparkViewModel = hiltViewModel(),
    onExamEnd: (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?, cumulativeExamCount: Int?) -> Unit = { _, _, _, _, _, _ -> },
    onExitWithoutAnswer: () -> Unit = {},
    onViewDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onEditNote: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    val examCount by settingsViewModel.examQuestionCount.collectAsState()
    val randomExam by settingsViewModel.randomExam.collectAsState()
    
    // 添加关键生命周期日志
    LaunchedEffect(quizId, examCount, randomExam, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName) {

        // 🚀 设置随机考试模式
        viewModel.setRandomExam(randomExam)
        
        when {
            isWrongBookMode && wrongBookFileName != null -> {
                
                viewModel.loadWrongQuestions(wrongBookFileName, examCount, randomExam)
            }
            isFavoriteMode && favoriteFileName != null -> {
                
                viewModel.loadFavoriteQuestions(favoriteFileName, examCount, randomExam)
            }
            else -> {
                
                viewModel.loadQuestions(quizId, examCount, randomExam)
            }
        }
    }
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val showResultList by viewModel.showResultList.collectAsState()
    val finished by viewModel.finished.collectAsState()
    val cumulativeCorrect by viewModel.cumulativeCorrect.collectAsState()
    val cumulativeAnswered by viewModel.cumulativeAnswered.collectAsState()
    val cumulativeExamCount by viewModel.cumulativeExamCount.collectAsState()
    var hasGradedExam by remember { mutableStateOf(false) } // 🎯 添加标志防止重复评分

    // 🎯 添加调试：监控考试次数变化
    LaunchedEffect(cumulativeExamCount) {
        
    }

    // 监控重要状态变化
    LaunchedEffect(questions.size) {
        
        if (questions.isNotEmpty()) {
            
        }
    }
    
    LaunchedEffect(progressLoaded) {
        
    }
    
    LaunchedEffect(selectedOptions.size) {
        val answeredCount = selectedOptions.count { it.isNotEmpty() }
        
    }

    DisposableEffect(Unit) {
        onDispose {
            if (
                progressLoaded &&
                !finished &&
                !hasGradedExam && // 🎯 防止重复评分
                selectedOptions.any { it.isNotEmpty() }
            ) {
                kotlinx.coroutines.runBlocking {
                    
                    hasGradedExam = true
                    viewModel.gradeExam()
                }
            } else {
                
            }
        }
    }
// ChatGPT 解析弹窗相关状态
    var showChatGptDialog by remember { mutableStateOf(false) }
    val baiduQianfanViewModel: com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel = hiltViewModel()
    val chatGptResult by baiduQianfanViewModel.analysisResult.collectAsState()
    val chatGptLoading by baiduQianfanViewModel.loading.collectAsState()

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
    val baiduAnalysisList by viewModel.baiduAnalysisList.collectAsState()
    val analysisText = if (analysisPair?.first == currentIndex) analysisPair?.second else analysisList.getOrNull(currentIndex)
    val hasDeepSeekAnalysis = analysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val sparkText = if (sparkPair?.first == currentIndex) sparkPair?.second else sparkAnalysisList.getOrNull(currentIndex)
    val hasSparkAnalysis = sparkAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val baiduText = if (chatGptResult?.first == currentIndex) chatGptResult?.second else baiduAnalysisList.getOrNull(currentIndex)
    val hasBaiduAnalysis = baiduAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
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
            val baiduSaved = baiduQianfanViewModel.getSavedAnalysis(question.id) ?: ""
            if (baiduSaved.isNotBlank()) {
                viewModel.updateBaiduAnalysis(currentIndex, baiduSaved)
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
    LaunchedEffect(chatGptResult) {
        val pair = chatGptResult
        if (pair != null && pair.second != "解析中...") {
            viewModel.updateBaiduAnalysis(pair.first, pair.second)
        }
    }
    LaunchedEffect(quizId, examCount, randomExam, progressLoaded, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName) {
        if (!progressLoaded) {
            when {
                isWrongBookMode && wrongBookFileName != null -> viewModel.loadWrongQuestions(wrongBookFileName, examCount, randomExam)
                isFavoriteMode && favoriteFileName != null -> viewModel.loadFavoriteQuestions(favoriteFileName, examCount, randomExam)
                else -> viewModel.loadQuestions(quizId, examCount, randomExam)
            }
        }
    }

    var showList by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf("") } // 记录要删除的AI类型
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var aiMenuExpanded by remember { mutableStateOf(false) }
    var askMenuExpanded by remember { mutableStateOf(false) }
    var expandedSection by remember(currentIndex) { mutableStateOf(-1) }
    val explanationScroll = rememberScrollState()
    val noteScroll = rememberScrollState()
    val deepSeekScroll = rememberScrollState()
    val sparkScroll = rememberScrollState()
    val baiduScroll = rememberScrollState()
    val mainScrollState = rememberScrollState()
    LaunchedEffect(currentIndex) { expandedSection = -1 }
    LaunchedEffect(mainScrollState.isScrollInProgress) {
        if (mainScrollState.isScrollInProgress) expandedSection = -1
    }
    LaunchedEffect(explanationScroll.isScrollInProgress) {
        if (explanationScroll.isScrollInProgress) expandedSection = 0
    }
    LaunchedEffect(noteScroll.isScrollInProgress) {
        if (noteScroll.isScrollInProgress) expandedSection = 1
    }
    LaunchedEffect(deepSeekScroll.isScrollInProgress) {
        if (deepSeekScroll.isScrollInProgress) expandedSection = 2
    }
    LaunchedEffect(sparkScroll.isScrollInProgress) {
        if (sparkScroll.isScrollInProgress) expandedSection = 3
    }
    LaunchedEffect(baiduScroll.isScrollInProgress) {
        if (baiduScroll.isScrollInProgress) expandedSection = 4
    }
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
    var initialAnsweredCount by remember { mutableStateOf(0) }

    // 修复：考试界面使用ExamViewModel自己的计算属性（与练习界面逻辑保持一致）
    // 本次会话实际答题数：只计算本次session新增的答题，不包含历史数据
    val sessionActualAnswered = remember(selectedOptions, initialAnsweredCount) {
        // 本次session新增的答题数 = 当前总答题数 - 进入时的历史答题数
        val currentAnswered = selectedOptions.count { it.isNotEmpty() }
        val newAnswered = (currentAnswered - initialAnsweredCount).coerceAtLeast(0)
        
        newAnswered
    }
    
    // 本次会话答对数：只计算本次session新增的答对题，不包含历史答对
    val sessionScore = remember(selectedOptions, questions, initialAnsweredCount) {
        var totalAnswered = 0  // 总已答题数
        var sessionCorrect = 0  // 本次session答对数
        
        for (i in questions.indices) {
            val selectedOption = selectedOptions.getOrNull(i) ?: emptyList()
            if (selectedOption.isNotEmpty()) {
                totalAnswered++
                // 如果这是本次session新增的答题（超出初始已答数量的部分）
                if (totalAnswered > initialAnsweredCount) {
                    val correctIndices = answerLettersToIndices(questions[i].answer)
                    if (selectedOption.sorted() == correctIndices.sorted()) {
                        sessionCorrect++
                    }
                }
            }
        }
        
        sessionCorrect
    }
    
    // 计算剩余未答题数（与练习界面保持一致）
    val sessionUnanswered = questions.size - selectedOptions.count { it.isNotEmpty() }
    
    LaunchedEffect(progressLoaded) {
        if (progressLoaded) {
            // 修复：answeredThisSession应该跟踪本次session是否答题，初始为false
            // 不要根据历史答题记录来设置，而是在用户实际选择答案时才设置为true
            answeredThisSession = false
            hasGradedExam = false // 🎯 重置评分标志
            // 记录进入页面时已答题数（用于计算本次session的增量）
            initialAnsweredCount = selectedOptions.count { it.isNotEmpty() }
            
        }
    }

    BackHandler {
        when {
            !answeredThisSession -> {
                // 未答题时直接退出
                onExitWithoutAnswer()
            }
            selectedOptions.count { it.isNotEmpty() } >= questions.size -> {
                // 已完成所有题目时直接跳转到结果页面
                coroutineScope.launch {
                    
                    if (!hasGradedExam) {
                        hasGradedExam = true
                        val totalScore = viewModel.gradeExam() // gradeExam内部已处理历史记录
                        
                        // 对于考试模式，已完成的考试未答数应该为0（参照练习界面逻辑）
                        val examUnanswered = 0
                        
                        // 添加详细调试信息（参照练习界面）

                        // 修复：考试模式下不传递累计数据，避免与当前考试数据混淆

                        // 🎯 修复：直接从gradeExam返回的最新考试次数

                        // 获取gradeExam执行后的最新考试次数
                        val latestExamCount = viewModel.cumulativeExamCount.value

                        onExamEnd(sessionScore, sessionActualAnswered, examUnanswered, cumulativeCorrect, cumulativeAnswered, latestExamCount)
                    } else {
                        
                        // 如果已经评分过，直接使用当前状态退出
                        val latestExamCount = viewModel.cumulativeExamCount.value
                        onExamEnd(sessionScore, sessionActualAnswered, 0, cumulativeCorrect, cumulativeAnswered, latestExamCount)
                    }
                }
            }
            else -> {
                // 其他情况弹出交卷确认窗口
                showExitDialog = true
            }
        }
    }
    val selectedOption = selectedOptions.getOrElse(currentIndex) { emptyList<Int>() }
    val showResult = showResultList.getOrNull(currentIndex) ?: false

    // 添加状态调试日志
    LaunchedEffect(currentIndex, selectedOption, showResult) {

        if (showResultList.isNotEmpty() && currentIndex < showResultList.size) {
            
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(mainScrollState)
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
                                else -> showExitDialog = true  // 有答题就弹出交卷确认窗口
                            }
                        } else {
                            if (dragAmount > 100f && currentIndex > 0) {
                                viewModel.prevQuestion()
                            } else if (dragAmount < -100f && currentIndex < questions.size - 1) {
                                viewModel.nextQuestion()
                            } else if (dragAmount < -100f) {
                                when {
                                    !answeredThisSession -> onExitWithoutAnswer()
                                    else -> showExitDialog = true  // 有答题就弹出交卷确认窗口
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
            IconButton(onClick = { aiMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI 解析",
                    tint = if (hasDeepSeekAnalysis || hasSparkAnalysis || hasBaiduAnalysis) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            DropdownMenu(expanded = aiMenuExpanded, onDismissRequest = { aiMenuExpanded = false }) {
                DropdownMenuItem(text = { Text("DeepSeek AI") }, onClick = {
                    aiMenuExpanded = false
                    // 考试模式：不显示结果，直接查看AI解析
                    if (hasDeepSeekAnalysis) {
                        onViewDeepSeek(analysisText ?: "", question.id, currentIndex)
                    } else {
                        aiViewModel.analyze(currentIndex, question)
                    }
                })
                DropdownMenuItem(text = { Text("Spark AI") }, onClick = {
                    aiMenuExpanded = false
                    // 考试模式：不显示结果，直接查看AI解析
                    if (hasSparkAnalysis) {
                        onViewSpark(sparkText ?: "", question.id, currentIndex)
                    } else {
                        sparkViewModel.analyze(currentIndex, question)
                    }
                })
                DropdownMenuItem(text = { Text("百度AI") }, onClick = {
                    aiMenuExpanded = false
                    // 考试模式：不显示结果，直接查看AI解析
                    if (hasBaiduAnalysis) {
                        onViewBaidu(baiduText ?: "", question.id, currentIndex)
                    } else {
                        baiduQianfanViewModel.analyze(currentIndex, question)
                    }
                })
            }

            IconButton(onClick = { askMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "AI 提问"
                )
            }
            DropdownMenu(expanded = askMenuExpanded, onDismissRequest = { askMenuExpanded = false }) {
                DropdownMenuItem(text = { Text("DeepSeek AI") }, onClick = {
                    askMenuExpanded = false
                    onAskDeepSeek(
                        formatQuestionWithOptions(question.content, question.options),
                        question.id,
                        currentIndex
                    )
                })
                DropdownMenuItem(text = { Text("Spark AI") }, onClick = {
                    askMenuExpanded = false
                    onAskSpark(
                        formatQuestionWithOptions(question.content, question.options),
                        question.id,
                        currentIndex
                    )
                })
                DropdownMenuItem(text = { Text("百度AI") }, onClick = {
                    askMenuExpanded = false
                    onAskBaidu(
                        formatQuestionWithOptions(question.content, question.options),
                        question.id,
                        currentIndex
                    )
                })
            }

            IconButton(onClick = {
                val note = noteList.getOrNull(currentIndex).orEmpty()
                onEditNote(note, question.id, currentIndex)
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

                Column(modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
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
            
            // 添加更详细的调试信息
            if (currentIndex == 0 && idx == 0) { // 只为第一个选项打印，避免日志过多

            }
            
            val backgroundColor = when {
                isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                isWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                isSelected -> {
                    
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                }
                else -> MaterialTheme.colorScheme.surface
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)  // 增加垂直内边距
                    .background(backgroundColor)
                    .clickable(enabled = !showResult) {
                        answeredThisSession = true
                        viewModel.selectOption(idx)
                        // 考试模式：只记录答案，不立即显示结果
                        // 移除原来的立即显示结果逻辑
                        
                        if (question.type == "单选题" || question.type == "判断题") {
                            // 考试模式：单选题和判断题答题后自动进入下一题，但不显示结果
                            autoJob?.cancel()
                            autoJob = coroutineScope.launch {
                                if (examDelay > 0) kotlinx.coroutines.delay(examDelay * 1000L)
                                if (currentIndex < questions.size - 1) {
                                    viewModel.nextQuestion()
                                } else {
                                    // 单选题答完最后一题，自动完成考试
                                    
                                    if (!hasGradedExam) {
                                        hasGradedExam = true
                                        val totalScore = viewModel.gradeExam()
                                        
                                        // 对于自动完成的考试，未答数应该为0
                                        val examUnanswered = 0
                                        
                                        // 添加详细调试信息

                                        // 修复：考试模式下不传递累计数据，避免与当前考试数据混淆

                                        // 🎯 修复：直接从ViewModel获取最新的考试次数，避免使用旧快照
                                        val currentExamCount = viewModel.cumulativeExamCount.value

                                        onExamEnd(sessionScore, sessionActualAnswered, examUnanswered, cumulativeCorrect, cumulativeAnswered, currentExamCount)
                                    } else {
                                        
                                        // 如果已经评分过，直接使用当前状态退出
                                        val currentExamCount = viewModel.cumulativeExamCount.value
                                        onExamEnd(sessionScore, sessionActualAnswered, 0, cumulativeCorrect, cumulativeAnswered, currentExamCount)
                                    }
                                }
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 增大选择圆圈区域和图标直径
                Box(
                    modifier = Modifier
                        .size(60.dp)  // 提供更大的点击区域
                        .padding(4.dp), // 添加内边距，让圆圈居中
                    contentAlignment = Alignment.Center
                ) {
                if (question.type == "多选题") {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            answeredThisSession = true
                            viewModel.selectOption(idx)
                            // 考试模式：多选题答题只记录答案，不显示结果
                            
                        },
                        enabled = !showResult,
                        modifier = Modifier.scale(1.5f)  // 将复选框放大1.5倍
                    )
                } else {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        enabled = !showResult,
                        modifier = Modifier.scale(1.5f)  // 将单选按钮放大1.5倍
                    )
                }}
                Spacer(modifier = Modifier.width(12.dp))  // 增加间距
                Text(
                    text = option,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),  // 增加文本的垂直内边距
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
                val collapsed = expandedSection != 0
                val lineHeight = with(LocalDensity.current) { (questionFontSize * 1.3f).sp.toDp() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!collapsed) {
                                // 修复：展开状态下增加最大高度限制，避免与主滚动冲突
                                Modifier.heightIn(max = 400.dp).verticalScroll(explanationScroll)
                            } else {
                                Modifier.heightIn(max = lineHeight + 16.dp)
                            }
                        )
                        .background(Color(0xFFFFF5C0))
                        .padding(8.dp)
                        .animateContentSize()
                        .pointerInput(collapsed) {
                            detectTapGestures(onTap = {
                                expandedSection = if (collapsed) 0 else -1
                            })
                        }
                ) {
                    Text(
                        text = "解析：" + question.explanation,
                        color = Color(0xFF835C00),
                        fontSize = questionFontSize.sp,
                        fontFamily = LocalFontFamily.current,
                        maxLines = if (collapsed) 1 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            val note = noteList.getOrNull(currentIndex)
            if (!note.isNullOrBlank()) {
                val collapsed = expandedSection != 1
                val lineHeight = with(LocalDensity.current) { (questionFontSize * 1.3f).sp.toDp() }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!collapsed) {
                                // 修复：展开状态下增加最大高度限制，避免与主滚动冲突
                                Modifier.heightIn(max = 400.dp).verticalScroll(noteScroll)
                            } else {
                                Modifier.heightIn(max = lineHeight + 16.dp)
                            }
                        )
                        .background(Color(0xFFE0FFE0))
                        .padding(8.dp)
                        .animateContentSize()
                        .pointerInput(note) {
                            detectTapGestures(
                                onTap = { expandedSection = if (collapsed) 1 else -1 },
                                onDoubleTap = {
                                    onEditNote(note, question.id, currentIndex)
                                },
                                onLongPress = { showDeleteNoteDialog = true }
                            )
                        }
                ) {
                    Text(
                        text = "笔记：$note",
                        color = Color(0xFF004B00),
                        fontSize = questionFontSize.sp,
                        fontFamily = LocalFontFamily.current,
                        maxLines = if (collapsed) 1 else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!analysisText.isNullOrBlank() || !sparkText.isNullOrBlank() || !baiduText.isNullOrBlank()) {
                if (!analysisText.isNullOrBlank()) {
                    val collapsed = expandedSection != 2
                    val lineHeight = with(LocalDensity.current) { (questionFontSize * 1.3f).sp.toDp() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (!collapsed) {
                                    // 修复：展开状态下增加最大高度限制，避免与主滚动冲突
                                    Modifier.heightIn(max = 400.dp).verticalScroll(deepSeekScroll)
                                } else {
                                    Modifier.heightIn(max = lineHeight + 16.dp)
                                }
                            )
                            .background(Color(0xFFE8F6FF))
                            .padding(8.dp)
                            .animateContentSize()
                            .pointerInput(analysisText) {
                                detectTapGestures(
                                    onTap = { expandedSection = if (collapsed) 2 else -1 },
                                    onDoubleTap = {
                                        onViewDeepSeek(analysisText!!, question.id, currentIndex)
                                    },
                                    onLongPress = { 
                                        deleteTarget = "deepseek"
                                        showDeleteDialog = true 
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = analysisText ?: "",
                            color = Color(0xFF004B6B),
                            fontSize = questionFontSize.sp,
                            fontFamily = LocalFontFamily.current,
                            maxLines = if (collapsed) 1 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (!sparkText.isNullOrBlank()) {
                    val collapsed = expandedSection != 3
                    val lineHeight = with(LocalDensity.current) { (questionFontSize * 1.3f).sp.toDp() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (!collapsed) {
                                    // 修复：展开状态下增加最大高度限制，避免与主滚动冲突
                                    Modifier.heightIn(max = 400.dp).verticalScroll(sparkScroll)
                                } else {
                                    Modifier.heightIn(max = lineHeight + 16.dp)
                                }
                            )
                            .background(Color(0xFFEDE7FF))
                            .padding(8.dp)
                            .animateContentSize()
                            .pointerInput(sparkText) {
                                detectTapGestures(
                                    onTap = { expandedSection = if (collapsed) 3 else -1 },
                                    onDoubleTap = {
                                        onViewSpark(sparkText!!, question.id, currentIndex)
                                    },
                                    onLongPress = { 
                                        deleteTarget = "spark"
                                        showDeleteDialog = true 
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = sparkText ?: "",
                            color = Color(0xFF3A006A),
                            fontSize = questionFontSize.sp,
                            fontFamily = LocalFontFamily.current,
                            maxLines = if (collapsed) 1 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (!baiduText.isNullOrBlank()) {
                    val collapsed = expandedSection != 4
                    val lineHeight = with(LocalDensity.current) { (questionFontSize * 1.3f).sp.toDp() }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (!collapsed) {
                                    // 修复：展开状态下增加最大高度限制，避免与主滚动冲突
                                    Modifier.heightIn(max = 400.dp).verticalScroll(baiduScroll)
                                } else {
                                    Modifier.heightIn(max = lineHeight + 16.dp)
                                }
                            )
                            .background(Color(0xFFF0F8E7))
                            .padding(8.dp)
                            .animateContentSize()
                            .pointerInput(baiduText) {
                                detectTapGestures(
                                    onTap = { expandedSection = if (collapsed) 4 else -1 },
                                    onDoubleTap = {
                                        onViewBaidu(baiduText!!, question.id, currentIndex)
                                    },
                                    onLongPress = { 
                                        deleteTarget = "baidu"
                                        showDeleteDialog = true 
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = baiduText ?: "",
                            color = Color(0xFF3B6E0A),
                            fontSize = questionFontSize.sp,
                            fontFamily = LocalFontFamily.current,
                            maxLines = if (collapsed) 1 else Int.MAX_VALUE,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                // 修复：移除weight修饰符，使用固定高度的Spacer以支持滚动
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            // 修复：移除weight修饰符，使用固定高度的Spacer以支持滚动
            Spacer(modifier = Modifier.height(16.dp))
        }

        // “提交答案”按钮
        // 底部导航按钮 - 只在多选题时显示，且在结果显示状态时隐藏
        if (question.type == "多选题" && !showResult) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
            // 上一题按钮
            Button(
                onClick = {
                    if (selectedOption.isNotEmpty()) {
                        answeredThisSession = true
                    }
                    viewModel.prevQuestion()
                },
                enabled = currentIndex > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "上一题",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 下一题按钮
            Button(
                onClick = {
                    if (selectedOption.isNotEmpty()) {
                        answeredThisSession = true
                    }
                    viewModel.nextQuestion()
                },
                enabled = currentIndex < questions.size - 1,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "下一题",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
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
                    when (deleteTarget) {
                        "deepseek" -> {
                            aiViewModel.clear()
                            viewModel.updateAnalysis(currentIndex, "")
                        }
                        "spark" -> {
                            sparkViewModel.clear()
                            viewModel.updateSparkAnalysis(currentIndex, "")
                        }
                        "baidu" -> {
                            baiduQianfanViewModel.clearResult()
                            viewModel.updateBaiduAnalysis(currentIndex, "")
                        }
                    }
                    showDeleteDialog = false
                    deleteTarget = ""
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    deleteTarget = ""
                }) { Text("取消") }
            },
            text = { Text("确定删除${when(deleteTarget) {
                "deepseek" -> "DeepSeek"
                "spark" -> "Spark"
                "baidu" -> "百度"
                else -> ""
            }}解析吗？") }
        )
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        
                        if (!hasGradedExam) {
                            hasGradedExam = true
                            val totalScore = viewModel.gradeExam() // gradeExam内部已处理历史记录
                            
                            // 对于手动交卷，未答数就是实际剩余未答数  
                            val examUnanswered = sessionUnanswered
                            
                            // 添加详细调试信息（参照练习界面）

                            // 修复：考试模式下不传递累计数据，避免与当前考试数据混淆

                            // 🎯 修复：直接从ViewModel获取最新的考试次数，避免使用旧快照
                            val currentExamCount = viewModel.cumulativeExamCount.value

                            // 交卷后直接退出到结果页面 - gradeExam内部已处理历史记录
                            onExamEnd(sessionScore, sessionActualAnswered, examUnanswered, cumulativeCorrect, cumulativeAnswered, currentExamCount)
                        } else {
                            
                            // 如果已经评分过，直接使用当前状态退出
                            val currentExamCount = viewModel.cumulativeExamCount.value
                            onExamEnd(sessionScore, sessionActualAnswered, sessionUnanswered, cumulativeCorrect, cumulativeAnswered, currentExamCount)
                        }
                        showExitDialog = false
                    }
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("取消") }
            },
            text = { Text(if (selectedOptions.any { it.isEmpty() }) "还未答完题，是否交卷？" else "确定交卷？") }
        )
    }
    if (showChatGptDialog) {
        AlertDialog(
            onDismissRequest = { showChatGptDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (chatGptResult?.first == currentIndex && !chatGptResult?.second.isNullOrBlank()) {
                        viewModel.updateAnalysis(currentIndex, chatGptResult?.second ?: "")
                    }
                    showChatGptDialog = false
                }) { Text("保存到解析") }
            },
            dismissButton = {
                TextButton(onClick = { showChatGptDialog = false }) { Text("关闭") }
            },
            text = {
                if (chatGptLoading) {
                    Text("百度AI解析中...", fontSize = LocalFontSize.current)
                } else {
                    Text(chatGptResult?.second ?: "无解析结果", fontSize = LocalFontSize.current)
                }
            }
        )
    }
}
