package com.example.testapp.presentation.screen.practice

import android.util.Log
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import com.example.testapp.uicommon.component.cancelAutoAdvanceOnTouch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import com.example.testapp.R
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.uicommon.component.AnswerCardGrid
import com.example.testapp.uicommon.component.CollapsibleAnswerCardSection
import com.example.testapp.uicommon.component.AnswerCardStateBuilder
import com.example.testapp.uicommon.component.QuestionEditDialog
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.presentation.screen.practice.components.ExamTopBar
import com.example.testapp.presentation.screen.practice.components.ExamAnalysisSection
import com.example.testapp.presentation.screen.exam.components.PracticeSubmitControls
import com.example.testapp.core.util.answerToOptionIndex
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.resolveDisplayOptions
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.presentation.screen.practice.PracticeQuestionContent
import com.example.testapp.presentation.screen.practice.PracticeResultSection
import com.example.testapp.util.rememberSoundEffects
import com.example.testapp.core.util.formatQuestionWithOptions
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.screen.favorite.FavoriteViewModel
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.screen.wrongbook.WrongBookViewModel
import com.example.testapp.presentation.util.localizedQuestionTypeLabel
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.uicommon.util.buildPracticeAnswerResult
import com.example.testapp.uicommon.util.formatQuestionForCopy
import com.example.testapp.uicommon.util.normalizeEditableFillAnswers
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.testapp.domain.model.WrongQuestion

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
    onViewExplanation: (String) -> Unit = {},
    onEditNote: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    val randomPractice by settingsViewModel.randomPractice.collectAsState()
    val practiceCount by settingsViewModel.practiceQuestionCount.collectAsState()
    LaunchedEffect(quizId, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName) {
        settingsViewModel.settingsReady.first { it }
        val count = settingsViewModel.practiceQuestionCount.value
        val random = settingsViewModel.randomPractice.value
        Log.d("PracticeScreen", "[INIT] randomPractice=$random, isWrongBookMode=$isWrongBookMode, wrongBookFileName=$wrongBookFileName, isFavoriteMode=$isFavoriteMode, favoriteFileName=$favoriteFileName, quizId=$quizId, practiceCount=$count")
        val targetProgressId = when {
            isWrongBookMode && wrongBookFileName != null -> "practice_wrongbook_${wrongBookFileName}"
            isFavoriteMode && favoriteFileName != null -> "practice_favorite_${favoriteFileName}"
            else -> "practice_${quizId}"
        }
        if (targetProgressId == viewModel.currentProgressId) {
            Log.d("PracticeScreen", "[INIT] progressId already matches, skip re-init to preserve memory state")
            return@LaunchedEffect
        }
        viewModel.setRandomPractice(random)
        if (isWrongBookMode && wrongBookFileName != null) {
            viewModel.setProgressId(id = targetProgressId, questionsId = wrongBookFileName, loadQuestions = false, random = random)
            viewModel.loadWrongQuestions(wrongBookFileName)
        } else if (isFavoriteMode && favoriteFileName != null) {
            viewModel.setProgressId(id = targetProgressId, questionsId = favoriteFileName, loadQuestions = false, random = random)
            viewModel.loadFavoriteQuestions(favoriteFileName)
        } else {
            viewModel.setProgressId(id = quizId, questionsId = quizId, questionCount = count, random = random)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
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
    val questionTextForAi = remember(question) { question?.let(::formatQuestionForCopy).orEmpty() }
    val analysisText = if (analysisPair?.first == currentIndex) analysisPair?.second else analysisList.getOrNull(currentIndex)
    val sparkText = if (sparkPair?.first == currentIndex) sparkPair?.second else sparkAnalysisList.getOrNull(currentIndex)
    val baiduText = if (baiduPair?.first == currentIndex) baiduPair?.second else baiduAnalysisList.getOrNull(currentIndex)
    val noteList by viewModel.noteList.collectAsState()
    val textAnswers by viewModel.textAnswers.collectAsState()
    val editableQuestion by viewModel.editableQuestion.collectAsState()
    val textAnswer = textAnswers.getOrNull(currentIndex).orEmpty()
    val resolvedFillAnswer = remember(question) { question?.let { resolveFillCorrectAnswer(it) }.orEmpty() }
    val parsingText = stringResource(R.string.parsing)
    val hasDeepSeekAnalysis = analysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val hasSparkAnalysis = sparkAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val hasBaiduAnalysis = baiduAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val hasNote = noteList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val selectedOption = selectedOptions.getOrNull(currentIndex) ?: emptyList<Int>()
    val showResult = showResultList.getOrNull(currentIndex) ?: false
    val displayOptions = remember(question) { question?.let(::resolveDisplayOptions).orEmpty() }
    val correctIndices = remember(question) { question?.let(::answerToOptionIndices).orEmpty() }
    val isFavorite = remember(question, favoriteQuestions) {
        question != null && favoriteQuestions.any { it.question.id == question.id }
    }
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); elapsed += 1 } }
    LaunchedEffect(currentIndex) { aiViewModel.clear(); sparkViewModel.clear(); baiduQianfanViewModel.clearResult() }
    LaunchedEffect(question) {
        question?.let {
            val saved = aiViewModel.getSavedAnalysis(it.id) ?: ""
            if (saved.isNotBlank()) viewModel.updateAnalysis(currentIndex, saved)
            val sparkSaved = sparkViewModel.getSavedAnalysis(it.id) ?: ""
            if (sparkSaved.isNotBlank()) viewModel.updateSparkAnalysis(currentIndex, sparkSaved)
            val baiduSaved = baiduQianfanViewModel.getSavedAnalysis(it.id) ?: ""
            if (baiduSaved.isNotBlank()) viewModel.updateBaiduAnalysis(currentIndex, baiduSaved)
        }
    }
    var showList by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditQuestionDialog by remember { mutableStateOf(false) }
    var editedQuestionContent by remember { mutableStateOf("") }
    var editedQuestionAnswer by remember { mutableStateOf("") }
    var editedAnswerParts by remember { mutableStateOf(listOf<String>()) }

    val sessionState by viewModel.sessionState.collectAsState()
    val sessionAnsweredCount = sessionState.sessionAnsweredCount
    val sessionScore = sessionState.sessionCorrectCount
    var aiMenuExpanded by remember { mutableStateOf(false) }

    val storedPracticeFontSize by FontSettingsDataStore.getPracticeFontSize(context, Float.NaN).collectAsState(initial = Float.NaN)
    var questionFontSize by remember { mutableStateOf(fontSize) }
    var questionLineSpacing by remember { mutableStateOf(1.3f) }
    var questionLetterSpacing by remember { mutableStateOf(0f) }
    var fontLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(storedPracticeFontSize) {
        if (!storedPracticeFontSize.isNaN()) { questionFontSize = storedPracticeFontSize; fontLoaded = true }
    }
    LaunchedEffect(questionFontSize, fontLoaded) {
        if (fontLoaded) {
            FontSettingsDataStore.setPracticeFontSize(context, questionFontSize)
            FontSettingsDataStore.setPracticeLineSpacing(context, questionLineSpacing)
            FontSettingsDataStore.setPracticeLetterSpacing(context, questionLetterSpacing)
        }
    }
    var showChatGptDialog by remember { mutableStateOf(false) }
    val chatGptResult by baiduQianfanViewModel.analysisResult.collectAsState()
    val chatGptLoading by baiduQianfanViewModel.loading.collectAsState()
    val autoAdvance = remember { PracticeAutoAdvanceController() }
    var answeredThisSession by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            Log.d("JUMP_DEBUG", "[LIFECYCLE] event=$event answeredThisSession=$answeredThisSession sessionAnsweredCount=$sessionAnsweredCount currentIndex=$currentIndex")
            when (event) {
                Lifecycle.Event.ON_RESUME -> autoAdvance.resume()
                Lifecycle.Event.ON_PAUSE -> autoAdvance.cancel()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    var showExitDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf("") }
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var showDeleteExplanationDialog by remember { mutableStateOf(false) }
    var showExplanationFull by remember { mutableStateOf(false) }
    val mainScrollState = rememberScrollState()
    LaunchedEffect(mainScrollState.isScrollInProgress) {
        if (mainScrollState.isScrollInProgress) autoAdvance.cancel()
    }

    LaunchedEffect(editableQuestion?.id, editableQuestion?.content, editableQuestion?.answer) {
        val q = editableQuestion
        editedQuestionContent = q?.content.orEmpty()
        editedQuestionAnswer = q?.answer.orEmpty()
        editedAnswerParts = if (q?.let { QuestionTypes.isInlineBlank(it.type) } == true) {
            normalizeEditableFillAnswers(editedQuestionContent, editedQuestionAnswer)
        } else {
            listOf(editedQuestionAnswer)
        }
    }

    LaunchedEffect(progressLoaded) { if (progressLoaded) answeredThisSession = false }
    LaunchedEffect(analysisPair) { val p = analysisPair; if (p != null && p.second != parsingText) viewModel.updateAnalysis(p.first, p.second) }
    LaunchedEffect(sparkPair) { val p = sparkPair; if (p != null && p.second != parsingText) viewModel.updateSparkAnalysis(p.first, p.second) }
    LaunchedEffect(baiduPair) { val p = baiduPair; if (p != null && p.second != parsingText) viewModel.updateBaiduAnalysis(p.first, p.second) }

    BackHandler {
        when {
            !answeredThisSession -> { autoAdvance.cancel(); onExitWithoutAnswer() }
            sessionAnsweredCount >= viewModel.totalCount -> {
                autoAdvance.cancel()
                val realUnanswered = viewModel.totalCount - viewModel.answeredCount
                if (sessionAnsweredCount > 0) viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, realUnanswered)
                onQuizEnd(sessionScore, sessionAnsweredCount, realUnanswered, viewModel.correctCount, viewModel.answeredCount)
            }
            else -> showExitDialog = true
        }
    }

    if (question == null || !progressLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_questions_loading), fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
        }
        return
    }

    var dragAmount by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(mainScrollState).padding(16.dp)
            .cancelAutoAdvanceOnTouch { autoAdvance.cancel() }
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount -> dragAmount += amount },
                    onDragEnd = {
                        if (dragAmount > 100f) {
                            autoAdvance.cancel()
                            focusManager.clearFocus(force = true)
                            viewModel.prevQuestion()
                        } else if (dragAmount < -100f) {
                            autoAdvance.cancel()
                            focusManager.clearFocus(force = true)
                            viewModel.nextQuestion()
                        }
                        dragAmount = 0f
                    },
                    onDragCancel = { dragAmount = 0f }
                )
            }
    ) {
        ExamTopBar(
            elapsed = elapsed, isFavorite = isFavorite,
            onToggleFavorite = { if (isFavorite) favoriteViewModel.removeFavorite(question?.id ?: 0) else question?.let { favoriteViewModel.addFavorite(it) } },
            aiMenuExpanded = aiMenuExpanded, onAiMenuToggle = { aiMenuExpanded = true }, onAiMenuDismiss = { aiMenuExpanded = false },
            onOpenAiMenu = { autoAdvance.cancel(); onAskDeepSeek(questionTextForAi, question.id, currentIndex) },
            onOpenAskMenu = { autoAdvance.cancel(); onAskSpark(questionTextForAi, question.id, currentIndex) },
            onEditNote = { autoAdvance.cancel(); val note = noteList.getOrNull(currentIndex)?.takeIf { it.isNotBlank() } ?: " "; onEditNote(note, question.id, currentIndex) },
            onShowList = { showList = true },
            settingsMenuExpanded = menuExpanded, onMenuToggle = { menuExpanded = true }, onMenuDismiss = { menuExpanded = false },
            questionsSize = questions.size, hasAnyAnalysis = hasDeepSeekAnalysis || hasSparkAnalysis || hasBaiduAnalysis, hasNote = hasNote,
            settingsMenuContent = {
                DropdownMenuItem(text = { Text(stringResource(R.string.increase_font)) }, onClick = {
                    val newSize = (questionFontSize + 2f).coerceAtMost(42f)
                    questionFontSize = newSize
                    coroutineScope.launch { FontSettingsDataStore.setPracticeFontSize(context, newSize) }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_font)) }, onClick = {
                    val newSize = (questionFontSize - 2f).coerceAtLeast(12f)
                    questionFontSize = newSize
                    coroutineScope.launch { FontSettingsDataStore.setPracticeFontSize(context, newSize) }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.increase_line_spacing)) }, onClick = {
                    questionLineSpacing = (questionLineSpacing + 0.1f).coerceAtMost(2.2f); menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_line_spacing)) }, onClick = {
                    questionLineSpacing = (questionLineSpacing - 0.1f).coerceAtLeast(1.0f); menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.increase_letter_spacing)) }, onClick = {
                    questionLetterSpacing = (questionLetterSpacing + 0.1f).coerceAtMost(2.0f); menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_letter_spacing)) }, onClick = {
                    questionLetterSpacing = (questionLetterSpacing - 0.1f).coerceAtLeast(0f); menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.edit_current_question)) }, onClick = {
                    menuExpanded = false
                    viewModel.clearEditableQuestion()
                    viewModel.prepareEditableQuestion(question.id)
                    showEditQuestionDialog = true
                })
            }
        )
        if (showList) {
            var sectionCollapsed by remember { mutableStateOf(emptySet<String>()) }
            fun toggleSection(name: String) {
                sectionCollapsed = if (name in sectionCollapsed) sectionCollapsed - name else sectionCollapsed + name
            }
            AlertDialog(onDismissRequest = { showList = false }, confirmButton = {}, text = {
                Column(modifier = Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState())) {
                    val singleIndices = remember(questions) { questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isSingle(q.type)) i else null } }
                    val multiIndices = remember(questions) { questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isMulti(q.type)) i else null } }
                    val judgeIndices = remember(questions) { questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isJudge(q.type)) i else null } }
                    val fillIndices = remember(questions) { questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isFill(q.type) || QuestionTypes.isInlineBlank(q.type)) i else null } }
                    val textIndices = remember(questions) { questions.mapIndexedNotNull { i, q -> if (QuestionTypes.isTextResponse(q.type)) i else null } }

                    val singleItems = remember(singleIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex) {
                        AnswerCardStateBuilder.build(singleIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex = currentIndex)
                    }
                    val multiItems = remember(multiIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex) {
                        AnswerCardStateBuilder.build(multiIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex = currentIndex)
                    }
                    val judgeItems = remember(judgeIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex) {
                        AnswerCardStateBuilder.build(judgeIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex = currentIndex)
                    }
                    val fillItems = remember(fillIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex) {
                        AnswerCardStateBuilder.build(fillIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex = currentIndex)
                    }
                    val textItems = remember(textIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex) {
                        AnswerCardStateBuilder.build(textIndices, questions, selectedOptions, textAnswers, showResultList, currentIndex = currentIndex)
                    }

                    if (singleItems.isNotEmpty()) {
                        CollapsibleAnswerCardSection(
                            label = stringResource(R.string.single_choice),
                            collapsed = "single" in sectionCollapsed,
                            onToggle = { toggleSection("single") },
                            items = singleItems,
                            onClick = { viewModel.goToQuestion(it); showList = false }
                        )
                    }
                    if (multiItems.isNotEmpty()) {
                        CollapsibleAnswerCardSection(
                            label = stringResource(R.string.multi_choice),
                            collapsed = "multi" in sectionCollapsed,
                            onToggle = { toggleSection("multi") },
                            items = multiItems,
                            onClick = { viewModel.goToQuestion(it); showList = false }
                        )
                    }
                    if (judgeItems.isNotEmpty()) {
                        CollapsibleAnswerCardSection(
                            label = stringResource(R.string.judge_choice),
                            collapsed = "judge" in sectionCollapsed,
                            onToggle = { toggleSection("judge") },
                            items = judgeItems,
                            onClick = { viewModel.goToQuestion(it); showList = false }
                        )
                    }
                    if (fillItems.isNotEmpty()) {
                        CollapsibleAnswerCardSection(
                            label = stringResource(R.string.fill_blank),
                            collapsed = "fill" in sectionCollapsed,
                            onToggle = { toggleSection("fill") },
                            items = fillItems,
                            onClick = { viewModel.goToQuestion(it); showList = false }
                        )
                    }
                    if (textItems.isNotEmpty()) {
                        CollapsibleAnswerCardSection(
                            label = stringResource(R.string.short_answer),
                            collapsed = "text" in sectionCollapsed,
                            onToggle = { toggleSection("text") },
                            items = textItems,
                            onClick = { viewModel.goToQuestion(it); showList = false }
                        )
                    }
                }
            })
        }

        if (showEditQuestionDialog) {
            QuestionEditDialog(
                editableQuestion = editableQuestion,
                initialQuestionContent = editedQuestionContent,
                initialQuestionAnswer = editedQuestionAnswer,
                initialAnswerParts = editedAnswerParts,
                onConfirm = { newContent, newOptions, finalAnswer ->
                    coroutineScope.launch {
                        viewModel.updateQuestionAllFields(currentIndex, newContent, newOptions, finalAnswer, question.explanation)
                        showEditQuestionDialog = false
                        viewModel.clearEditableQuestion()
                    }
                },
                onDismiss = {
                    showEditQuestionDialog = false
                    viewModel.clearEditableQuestion()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.question_type_prefix) + localizedQuestionTypeLabel(question.type), fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            Spacer(modifier = Modifier.weight(1f))
            Text("${currentIndex + 1}/${questions.size}", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
        }
        LinearProgressIndicator(
            progress = if (questions.size > 0) (currentIndex + 1f) / questions.size else 0f,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Question content
        PracticeQuestionContent(
            question = question,
            textAnswer = textAnswer,
            showResult = showResult,
            selectedOption = selectedOption,
            displayOptions = displayOptions,
            resolvedFillAnswer = resolvedFillAnswer,
            questionFontSize = questionFontSize,
            questionLineSpacing = questionLineSpacing,
            questionLetterSpacing = questionLetterSpacing,
            onAnswerChange = { viewModel.updateTextAnswer(it) },
            onOptionClick = { idx ->
                answeredThisSession = true
                if (QuestionTypes.isMulti(question.type)) viewModel.toggleOption(idx)
            },
            submitCurrentAnswer = { idx ->
                if (idx != null) {
                    answeredThisSession = true
                    val answeredIndex = currentIndex
                    viewModel.answerQuestion(idx)
                    val correct = idx == answerToOptionIndex(question)
                    if (soundEnabled) { if (correct) soundEffects.playCorrect() else soundEffects.playWrong() }
                    if (!correct) coroutineScope.launch { wrongBookViewModel.addWrongQuestion(WrongQuestion(question, listOf(idx))) }
                    onSubmit(correct)
                    autoAdvance.schedule(coroutineScope, answeredIndex, delaySec = if (correct) correctDelay else wrongDelay, revealResultFirst = true, showResult = viewModel::updateShowResult, onAdvance = {
    val total = questions.size
    if (answeredIndex >= total - 1) {
        if (sessionAnsweredCount >= viewModel.totalCount) {
            viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, viewModel.totalCount - viewModel.answeredCount)
            onQuizEnd(sessionScore, sessionAnsweredCount, viewModel.totalCount - viewModel.answeredCount, viewModel.correctCount, viewModel.answeredCount)
        } else {
            showExitDialog = true
        }
    } else if (randomPractice) {
        viewModel.nextQuestion()
    } else {
        viewModel.goToQuestion(answeredIndex + 1)
    }
})
                }
            }
        )

        if (showResult) {
            val answerResult = buildPracticeAnswerResult(
                question = question,
                textAnswer = textAnswer,
                selectedOption = selectedOption,
                resolvedFillAnswer = resolvedFillAnswer,
                displayOptions = displayOptions,
                correctIndices = correctIndices
            )
            val answerResultText = if (answerResult.allCorrect) {
                stringResource(R.string.answer_correct)
            } else {
                stringResource(R.string.answer_wrong_format, answerResult.correctText)
            }
            PracticeResultSection(
                question = question,
                showResult = showResult,
                textAnswer = textAnswer,
                resolvedFillAnswer = resolvedFillAnswer,
                correctIndices = correctIndices,
                displayOptions = displayOptions,
                selectedOption = selectedOption,
                questionFontSize = questionFontSize,
                questionLineSpacing = questionLineSpacing,
                questionLetterSpacing = questionLetterSpacing,
                allCorrect = answerResult.allCorrect,
                correctText = answerResult.correctText,
                answerResultText = answerResultText,
            retryLabel = stringResource(R.string.retry_current_question),
            retryWrongLabel = stringResource(R.string.retry_wrong_blanks),
                onRetry = {
                    answeredThisSession = false
                    viewModel.updateShowResult(currentIndex, false)
                },
                onRetryWrongBlanks = if (QuestionTypes.isInlineBlank(question.type)) {
                    {
                        answeredThisSession = false
                        viewModel.updateShowResult(currentIndex, false)
                    }
                } else null
            )
            val analysisPrefix = stringResource(R.string.analysis_prefix)
            ExamAnalysisSection(
                text = question.explanation.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFFFF5C0),
                label = analysisPrefix,
                fontSize = questionFontSize,
                lineHeight = questionLineSpacing,
                letterSpacing = questionLetterSpacing,
                onDoubleTap = { onViewExplanation(analysisPrefix + question.explanation) },
                onLongPress = { showDeleteExplanationDialog = true }
            )
            val note = noteList.getOrNull(currentIndex)
            ExamAnalysisSection(
                text = note?.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFE0FFE0),
                label = stringResource(R.string.note_prefix),
                fontSize = questionFontSize,
                lineHeight = questionLineSpacing,
                letterSpacing = questionLetterSpacing,
                onDoubleTap = { question?.let { onEditNote(note!!, it.id, currentIndex) } },
                onLongPress = { showDeleteNoteDialog = true }
            )
            ExamAnalysisSection(
                text = analysisText?.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFE8F6FF),
                label = stringResource(R.string.export_header_deepseek),
                fontSize = questionFontSize,
                lineHeight = questionLineSpacing,
                letterSpacing = questionLetterSpacing,
                onDoubleTap = { question?.let { onViewDeepSeek(analysisText ?: "", it.id, currentIndex) } },
                onLongPress = { deleteTarget = "deepseek"; showDeleteDialog = true }
            )
            ExamAnalysisSection(
                text = sparkText?.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFEDE7FF),
                label = stringResource(R.string.export_header_spark),
                fontSize = questionFontSize,
                lineHeight = questionLineSpacing,
                letterSpacing = questionLetterSpacing,
                onDoubleTap = { question?.let { onViewSpark(sparkText ?: "", it.id, currentIndex) } },
                onLongPress = { deleteTarget = "spark"; showDeleteDialog = true }
            )
            ExamAnalysisSection(
                text = baiduText?.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFF0F8E7),
                label = stringResource(R.string.export_header_baidu),
                fontSize = questionFontSize,
                lineHeight = questionLineSpacing,
                letterSpacing = questionLetterSpacing,
                onDoubleTap = { question?.let { onViewBaidu(baiduText ?: "", it.id, currentIndex) } },
                onLongPress = { deleteTarget = "baidu"; showDeleteDialog = true }
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!showResult) {
            val submitManualAnswer = {
                autoAdvance.cancel()
                focusManager.clearFocus(force = true)
                answeredThisSession = true
                val answeredIndex = currentIndex
                val allCorrect = if (
                    QuestionTypes.isFill(question.type) ||
                    QuestionTypes.isTextResponse(question.type) ||
                    QuestionTypes.isInlineBlank(question.type)
                ) {
                    val correct = question.answer.split("|").map { it.trim() }
                    val userAnswer = textAnswers.getOrNull(answeredIndex).orEmpty()
                    val userParts = userAnswer.split("|").map { it.trim() }
                    correct.zip(userParts).all { (c, u) -> c == u } ||
                        correct.size == 1 && userAnswer.trim() == correct.first()
                } else {
                    selectedOption.toSet() == correctIndices.toSet()
                }
                if (soundEnabled) {
                    if (allCorrect) soundEffects.playCorrect() else soundEffects.playWrong()
                }
                if (!allCorrect && (selectedOption.isNotEmpty() || textAnswer.isNotBlank())) {
                    coroutineScope.launch {
                        wrongBookViewModel.addWrongQuestion(WrongQuestion(question, selectedOption))
                    }
                }
                onSubmit(allCorrect)
                autoAdvance.schedule(coroutineScope, answeredIndex, delaySec = if (allCorrect) correctDelay else wrongDelay, revealResultFirst = true, showResult = viewModel::updateShowResult, onAdvance = {
                    val total = questions.size
                    if (answeredIndex >= total - 1) {
                        if (sessionAnsweredCount >= viewModel.totalCount) {
                            viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, viewModel.totalCount - viewModel.answeredCount)
                            onQuizEnd(sessionScore, sessionAnsweredCount, viewModel.totalCount - viewModel.answeredCount, viewModel.correctCount, viewModel.answeredCount)
                        } else {
                            showExitDialog = true
                        }
                    } else if (randomPractice) {
                        viewModel.nextQuestion()
                    } else {
                        viewModel.goToQuestion(answeredIndex + 1)
                    }
                })
            }
            PracticeSubmitControls(
                enabled = true,
                label = stringResource(R.string.submit_answer),
                modifier = Modifier.padding(top = 16.dp),
                swapPrimaryAndLeading = true,
                leadingContent = {
                    Button(
                        onClick = {
                            autoAdvance.cancel()
                            focusManager.clearFocus(force = true)
                            viewModel.nextQuestion()
                        },
                        enabled = currentIndex < questions.size - 1
                    ) {
                        Text(
                            text = stringResource(R.string.next_question),
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                },
                onSubmitClick = submitManualAnswer
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showExplanationFull) {
        Dialog(
            onDismissRequest = { showExplanationFull = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.88f)) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.analysis_prefix) + question.explanation,
                        fontSize = questionFontSize.sp,
                        fontFamily = LocalFontFamily.current,
                        lineHeight = (questionFontSize * questionLineSpacing).sp,
                        letterSpacing = questionLetterSpacing.sp
                    )
                }
            }
        }
    }
    if (showDeleteExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteExplanationDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearExplanation(currentIndex, question)
                    showDeleteExplanationDialog = false
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteExplanationDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
            text = { Text("确定要删除该解析内容吗？") }
        )
    }
    DialogsHost(
        showDeleteNoteDialog = showDeleteNoteDialog, onDismissDeleteNote = { showDeleteNoteDialog = false },
        onConfirmDeleteNote = { viewModel.saveNote(question.id, currentIndex, ""); showDeleteNoteDialog = false },
        showDeleteDialog = showDeleteDialog, deleteTarget = deleteTarget,
        onDismissDelete = { showDeleteDialog = false; deleteTarget = "" },
        onConfirmDelete = {
            when (deleteTarget) {
                "deepseek" -> { aiViewModel.clear(); viewModel.updateAnalysis(currentIndex, "") }
                "spark" -> { sparkViewModel.clear(); viewModel.updateSparkAnalysis(currentIndex, "") }
                "baidu" -> { baiduQianfanViewModel.clearResult(); viewModel.updateBaiduAnalysis(currentIndex, "") }
            }
            showDeleteDialog = false; deleteTarget = ""
        },
        showExitDialog = showExitDialog, sessionAnsweredCount = sessionAnsweredCount, totalCount = viewModel.totalCount,
        onDismissExit = { showExitDialog = false },
        onConfirmExit = {
            autoAdvance.cancel(); showExitDialog = false
            val realUnanswered = viewModel.totalCount - viewModel.answeredCount
            if (sessionAnsweredCount > 0) viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, realUnanswered)
            onQuizEnd(sessionScore, sessionAnsweredCount, realUnanswered, viewModel.correctCount, viewModel.answeredCount)
        },
        showChatGptDialog = showChatGptDialog, onDismissChatGpt = { showChatGptDialog = false },
        chatGptLoading = chatGptLoading,
        chatGptResult = chatGptResult?.let { Pair(it.first, com.example.testapp.core.common.LocalizedResult(it.second ?: "", emptyList())) },
        currentIndex = currentIndex, onSaveChatGptToAnalysis = { viewModel.updateAnalysis(currentIndex, it) },
        deepseekLabel = "DeepSeek", sparkLabel = "Spark", baiduLabel = "Baidu"
    )
}

@Composable
private fun DialogsHost(
    showDeleteNoteDialog: Boolean,
    onDismissDeleteNote: () -> Unit,
    onConfirmDeleteNote: () -> Unit,
    showDeleteDialog: Boolean,
    deleteTarget: String,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    showExitDialog: Boolean,
    sessionAnsweredCount: Int,
    totalCount: Int,
    onDismissExit: () -> Unit,
    onConfirmExit: () -> Unit,
    showChatGptDialog: Boolean,
    onDismissChatGpt: () -> Unit,
    chatGptLoading: Boolean,
    chatGptResult: Pair<Int, com.example.testapp.core.common.LocalizedResult>?,
    currentIndex: Int,
    onSaveChatGptToAnalysis: (String) -> Unit,
    deepseekLabel: String,
    sparkLabel: String,
    baiduLabel: String
) {
    if (showDeleteNoteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteNote,
            confirmButton = { TextButton(onClick = onConfirmDeleteNote) { Text(stringResource(R.string.confirm)) } },
            dismissButton = { TextButton(onClick = onDismissDeleteNote) { Text(stringResource(R.string.cancel)) } },
            text = { Text(stringResource(R.string.confirm_delete_note)) }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            confirmButton = { TextButton(onClick = onConfirmDelete) { Text(stringResource(R.string.confirm)) } },
            dismissButton = { TextButton(onClick = onDismissDelete) { Text(stringResource(R.string.cancel)) } },
            text = { Text(stringResource(R.string.confirm_delete_analysis, deleteTarget)) }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = onDismissExit,
            confirmButton = { TextButton(onClick = onConfirmExit) { Text(stringResource(R.string.confirm)) } },
            dismissButton = { TextButton(onClick = onDismissExit) { Text(stringResource(R.string.cancel)) } },
            text = { Text("已答 $sessionAnsweredCount / $totalCount，确定退出练习吗？") }
        )
    }

    if (showChatGptDialog) {
        AlertDialog(
            onDismissRequest = onDismissChatGpt,
            confirmButton = {
                TextButton(
                    onClick = {
                        chatGptResult?.second?.key?.let(onSaveChatGptToAnalysis)
                        onDismissChatGpt()
                    },
                    enabled = chatGptResult?.second?.key?.isNotBlank() == true
                ) { Text(stringResource(R.string.save_to_analysis)) }
            },
            dismissButton = { TextButton(onClick = onDismissChatGpt) { Text(stringResource(R.string.cancel)) } },
            title = { Text(listOf(deepseekLabel, sparkLabel, baiduLabel).joinToString(" / ")) },
            text = {
                if (chatGptLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(chatGptResult?.second?.key.orEmpty())
                }
            }
        )
    }
}
