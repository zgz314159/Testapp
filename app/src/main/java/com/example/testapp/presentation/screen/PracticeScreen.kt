package com.example.testapp.presentation.screen

import android.util.Log
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.AnswerCardGrid
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.util.answerLetterToIndex
import com.example.testapp.util.rememberSoundEffects
import com.example.testapp.util.formatQuestionWithOptions
import com.example.testapp.presentation.screen.SparkViewModel
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
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
    baiduQianfanViewModel: BaiduQianfanViewModel = hiltViewModel(),
    onQuizEnd: (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?) -> Unit = { _, _, _, _, _ -> },
    onSubmit: (Boolean) -> Unit = {},
    onExitWithoutAnswer: () -> Unit = {},
    onViewDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onEditNote: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    // --- 各种状态和依赖 ---
    val randomPractice by settingsViewModel.randomPractice.collectAsState()
    val practiceCount by settingsViewModel.practiceQuestionCount.collectAsState()
    LaunchedEffect(quizId, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName, randomPractice, practiceCount) {
        Log.d("PracticeScreen", "[INIT] randomPractice=$randomPractice, isWrongBookMode=$isWrongBookMode, wrongBookFileName=$wrongBookFileName, isFavoriteMode=$isFavoriteMode, favoriteFileName=$favoriteFileName, quizId=$quizId, practiceCount=$practiceCount")
        viewModel.setRandomPractice(randomPractice)
        if (isWrongBookMode && wrongBookFileName != null) {
            Log.d("PracticeScreen", "[setProgressId] wrongbook mode, id=wrongbook_${wrongBookFileName}, questionsId=$wrongBookFileName, random=$randomPractice")
            viewModel.setProgressId(
                id = "wrongbook_${wrongBookFileName}",
                questionsId = wrongBookFileName,
                loadQuestions = false,
                random = randomPractice
            )
            viewModel.loadWrongQuestions(wrongBookFileName)
            Log.d("PracticeScreen", "[loadWrongQuestions] ${wrongBookFileName}, random=$randomPractice")
        } else if (isFavoriteMode && favoriteFileName != null) {
            Log.d("PracticeScreen", "[setProgressId] favorite mode, id=favorite_${favoriteFileName}, questionsId=$favoriteFileName, random=$randomPractice")
            viewModel.setProgressId(
                id = "favorite_${favoriteFileName}",
                questionsId = favoriteFileName,
                loadQuestions = false,
                random = randomPractice
            )
            viewModel.loadFavoriteQuestions(favoriteFileName)
            Log.d("PracticeScreen", "[loadFavoriteQuestions] ${favoriteFileName}, random=$randomPractice")
        } else {
            Log.d("PracticeScreen", "[setProgressId] normal mode, id=$quizId, questionsId=$quizId, questionCount=$practiceCount, random=$randomPractice")
            viewModel.setProgressId(id = quizId, questionsId = quizId, questionCount = practiceCount, random = randomPractice)
            Log.d("PracticeScreen", "[loadNormalQuestions] $quizId, random=$randomPractice")
        }
        // 打印当前题目顺序
        val questions = viewModel.questions.value
        Log.d("PracticeScreen", "[questions order] ${questions.map { it.id }}")
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
    val baiduPair by baiduQianfanViewModel.analysisResult.collectAsState()
    val analysisList by viewModel.analysisList.collectAsState()
    val sparkAnalysisList by viewModel.sparkAnalysisList.collectAsState()
    val baiduAnalysisList by viewModel.baiduAnalysisList.collectAsState()
    val analysisText = if (analysisPair?.first == currentIndex) analysisPair?.second else analysisList.getOrNull(currentIndex)
    val sparkText = if (sparkPair?.first == currentIndex) sparkPair?.second else sparkAnalysisList.getOrNull(currentIndex)
    val baiduText = if (baiduPair?.first == currentIndex) baiduPair?.second else baiduAnalysisList.getOrNull(currentIndex)
    val noteList by viewModel.noteList.collectAsState()
    val hasDeepSeekAnalysis = analysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val hasSparkAnalysis = sparkAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val hasBaiduAnalysis = baiduAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val hasNote = noteList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val selectedOption = selectedOptions.getOrNull(currentIndex) ?: emptyList<Int>()
    val showResult = showResultList.getOrNull(currentIndex) ?: false
    val isFavorite = remember(question, favoriteQuestions) {
        question != null && favoriteQuestions.any { it.question.id == question.id }
    }
    var elapsed by remember { mutableStateOf(0) }
    // Timer should accumulate across questions
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            elapsed += 1
        }
    }
    LaunchedEffect(currentIndex) {
        aiViewModel.clear()
        sparkViewModel.clear()
        baiduQianfanViewModel.clearResult()
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
            val baiduSaved = baiduQianfanViewModel.getSavedAnalysis(it.id) ?: ""
            if (baiduSaved.isNotBlank()) {
                viewModel.updateBaiduAnalysis(currentIndex, baiduSaved)
            }
        }
    }
    var showList by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val sessionState by viewModel.sessionState.collectAsState()
    val sessionAnsweredCount = sessionState.sessionAnsweredCount  // 本次会话已答题数
    val sessionActualAnswered = sessionState.sessionAnsweredCount  // 使用统一状态
    val sessionScore = sessionState.sessionCorrectCount  // 本次会话答对数
    var aiMenuExpanded by remember { mutableStateOf(false) }
    var askMenuExpanded by remember { mutableStateOf(false) }

   // var sessionAnsweredCount by remember { mutableStateOf(0) }
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
    var showChatGptDialog by remember { mutableStateOf(false) }
    val chatGptResult by baiduQianfanViewModel.analysisResult.collectAsState()
    val chatGptLoading by baiduQianfanViewModel.loading.collectAsState()
    var autoJob by remember { mutableStateOf<Job?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var answeredThisSession by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf("") } // 记录要删除的AI类型
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
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
    LaunchedEffect(progressLoaded) {
        if (progressLoaded) {
            answeredThisSession = false
            // sessionScore 现在从统一状态自动计算，无需手动重置
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
    LaunchedEffect(baiduPair) {
        val pair = baiduPair
        if (pair != null && pair.second != "解析中...") {
            viewModel.updateBaiduAnalysis(pair.first, pair.second)
        }
    }
    BackHandler {
        when {
            !answeredThisSession -> {
                autoJob?.cancel()
                onExitWithoutAnswer()
            }
            sessionAnsweredCount >= viewModel.totalCount -> {
                autoJob?.cancel()
                // 对于"本次练习"，混合使用session和总进度数据
                val realUnanswered = viewModel.totalCount - viewModel.answeredCount
                // 本次练习的未答数：对于已完成的练习，未答数为0
                val sessionUnanswered = 0
                
                // 详细调试信息

                // 修复：只在有实际答题时才记录历史
                if (sessionActualAnswered > 0) {
                    viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, realUnanswered)
                }
                onQuizEnd(sessionScore, sessionActualAnswered, sessionUnanswered, viewModel.correctCount, viewModel.answeredCount)
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
            .verticalScroll(mainScrollState)
            .padding(16.dp)
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount ->
                        dragAmount += amount
                        Log.d("PracticeScreen", "[DRAG] onHorizontalDrag: amount=$amount, dragAmount=$dragAmount, currentIndex=$currentIndex, randomPractice=${settingsViewModel.randomPractice.value}")
                    },
                    onDragEnd = {
                        Log.d("PracticeScreen", "[DRAG] onDragEnd: dragAmount=$dragAmount, currentIndex=$currentIndex, randomPractice=${settingsViewModel.randomPractice.value}")
                        if (dragAmount > 100f) {
                            autoJob?.cancel()
                            Log.d("PracticeScreen", "[DRAG] prevQuestion called, currentIndex(before)=$currentIndex")
                            viewModel.prevQuestion()
                        } else if (dragAmount < -100f) {
                            autoJob?.cancel()
                            Log.d("PracticeScreen", "[DRAG] nextQuestion called, currentIndex(before)=$currentIndex")
                            viewModel.nextQuestion()
                        }
                        dragAmount = 0f
                    },
                    onDragCancel = {
                        Log.d("PracticeScreen", "[DRAG] onDragCancel, dragAmount reset to 0")
                        dragAmount = 0f
                    }
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
                    favoriteViewModel.removeFavorite(question?.id ?: 0)
                } else {
                    question?.let { favoriteViewModel.addFavorite(it) }
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
                    if (!showResult) {
                        answeredThisSession = true
                        viewModel.updateShowResult(currentIndex, true)
                    }
                    if (hasDeepSeekAnalysis) {
                        onViewDeepSeek(analysisText ?: "", question?.id ?: 0, currentIndex)
                    } else {
                        question?.let { aiViewModel.analyze(currentIndex, it) }
                    }
                })
                DropdownMenuItem(text = { Text("Spark AI") }, onClick = {
                    aiMenuExpanded = false
                    if (!showResult) {
                        answeredThisSession = true
                        viewModel.updateShowResult(currentIndex, true)
                    }
                    if (hasSparkAnalysis) {
                        onViewSpark(sparkText ?: "", question?.id ?: 0, currentIndex)
                    } else {
                        question?.let { sparkViewModel.analyze(currentIndex, it) }
                    }
                })
                DropdownMenuItem(text = { Text("百度AI") }, onClick = {
                    aiMenuExpanded = false
                    if (!showResult) {
                        answeredThisSession = true
                        viewModel.updateShowResult(currentIndex, true)
                    }
                    if (hasBaiduAnalysis) {
                        onViewBaidu(baiduText ?: "", question?.id ?: 0, currentIndex)
                    } else {
                        question?.let { baiduQianfanViewModel.analyze(currentIndex, it) }
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
                    if (question != null) {
                        onAskDeepSeek(
                            formatQuestionWithOptions(question.content, question.options),
                            question.id,
                            currentIndex
                        )
                    }
                })
                DropdownMenuItem(text = { Text("Spark AI") }, onClick = {
                    askMenuExpanded = false
                    if (question != null) {
                        onAskSpark(
                            formatQuestionWithOptions(question.content, question.options),
                            question.id,
                            currentIndex
                        )
                    }
                })
                DropdownMenuItem(text = { Text("百度AI") }, onClick = {
                    askMenuExpanded = false
                    if (question != null) {
                        onAskBaidu(
                            formatQuestionWithOptions(question.content, question.options),
                            question.id,
                            currentIndex
                        )
                    }
                })
            }

            IconButton(onClick = {
                val note = noteList.getOrNull(currentIndex)?.takeIf { it.isNotBlank() } ?: " "
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
                    .padding(vertical = 8.dp)  // 增加垂直内边距
                    .background(bgColor)
                    .clickable(enabled = !showResult) {
                        answeredThisSession = true
                        if (question.type == "单选题" || question.type == "判断题") {
                            // 答题前记录状态

                            viewModel.answerQuestion(idx)
                            
                            // 答题后记录状态

                            val correctIdx = answerLetterToIndex(question.answer)
                            val correct = idx == correctIdx
                            // sessionScore 现在从统一状态自动计算，无需手动增加
                            
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
                                if (currentIndex < questions.size - 1) {
                                    viewModel.nextQuestion()
                                } else if (sessionAnsweredCount == 0) {
                                    onExitWithoutAnswer()
                                } else if (sessionAnsweredCount >= viewModel.totalCount) {
                                    // 对于"本次练习"，混合使用session和总进度数据
                                    val realUnanswered = viewModel.totalCount - viewModel.answeredCount
                                    
                                    // 详细调试信息

                                    viewModel.addHistoryRecord(
                                        sessionScore,
                                        viewModel.totalCount,
                                        realUnanswered
                                    )
                                    // 修复：只在有实际答题时才记录历史 - 这里已经检查了sessionAnsweredCount > 0，所以保持原样
                                    onQuizEnd(sessionScore, sessionActualAnswered, realUnanswered, viewModel.correctCount, viewModel.answeredCount)
                                } else {
                                    showExitDialog = true
                                }
                            }
                            } else {

                            viewModel.toggleOption(idx)

                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 增大选择圆圈区域和图标直径
                Box(
                    modifier = Modifier
                        .size(60.dp)  // 提供更大的点击区域
                        .padding(4.dp), // 调整内边距，让圆圈居中
                    contentAlignment = Alignment.Center
                ) {
                if (question.type == "多选题") {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            answeredThisSession = true

                            viewModel.toggleOption(idx)

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
                                    val noteText = note?.takeIf { it.isNotBlank() } ?: " "
                                    onEditNote(noteText, question.id, currentIndex)
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
                                        question?.let { q ->
                                            onViewDeepSeek(analysisText!!, q.id, currentIndex)
                                        }
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
                                        question?.let { q ->
                                            onViewSpark(sparkText!!, q.id, currentIndex)
                                        }
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
                                        question?.let { q ->
                                            onViewBaidu(baiduText!!, q.id, currentIndex)
                                        }
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

        // “提交答案”按钮（多选题且未提交才显示）
        if (question.type == "多选题" && !showResult) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        // 多选题提交前记录状态

                        autoJob?.cancel()
                        viewModel.updateShowResult(currentIndex, true)
                        
                        // 多选题提交后记录状态

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
                        // sessionScore 现在从统一状态自动计算，无需手动增加
                        onSubmit(allCorrect)
                        autoJob = coroutineScope.launch {
                            val d = if (allCorrect) correctDelay else wrongDelay
                            if (d > 0) kotlinx.coroutines.delay(d * 1000L)
                            if (currentIndex < questions.size - 1) {
                                viewModel.nextQuestion()
                            } else if (sessionAnsweredCount == 0) {
                                onExitWithoutAnswer()
                            } else if (sessionAnsweredCount >= viewModel.totalCount) {
                                // 对于"本次练习"，混合使用session和总进度数据
                                val realUnanswered = viewModel.totalCount - viewModel.answeredCount
                                
                                // 详细调试信息

                                viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, realUnanswered)
                                onQuizEnd(sessionScore, sessionActualAnswered, realUnanswered, viewModel.correctCount, viewModel.answeredCount)
                            } else {
                                showExitDialog = true
                            }
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
                    autoJob?.cancel()
                    showExitDialog = false
                    // 对于"本次练习"，混合使用session和总进度数据
                    val totalQuestions = viewModel.totalCount
                    val realUnanswered = totalQuestions - viewModel.answeredCount
                    
                    // 详细调试信息

                    // 修复：只在有实际答题时才记录历史
                    if (sessionActualAnswered > 0) {
                        viewModel.addHistoryRecord(sessionScore, totalQuestions, realUnanswered)
                    }
                    onQuizEnd(sessionScore, sessionActualAnswered, realUnanswered, viewModel.correctCount, viewModel.answeredCount)
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("取消") }
            },
            text = {
                Text(
                    if (sessionAnsweredCount < viewModel.totalCount)
                        "还有未答题目，是否交卷？"
                    else
                        "确定交卷？"
                )
            }
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
