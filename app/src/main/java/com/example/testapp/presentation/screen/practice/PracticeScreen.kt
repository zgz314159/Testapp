package com.example.testapp.presentation.screen.practice

import android.util.Log
import android.widget.Toast
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
import androidx.compose.runtime.key
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
import com.example.testapp.uicommon.component.FillAnswerRoundLabel
import com.example.testapp.uicommon.component.QuestionNavigationControls
import com.example.testapp.uicommon.component.QuestionEditDialog
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.presentation.screen.practice.components.ExamTopBar
import com.example.testapp.presentation.screen.practice.components.ExamAnalysisSection
import com.example.testapp.presentation.screen.practice.components.PracticeQuestionListDialog
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryBackwardResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryForwardResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import com.example.testapp.presentation.screen.practice.SkipUnansweredSourceResult
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
    isReviewMode: Boolean = false,
    reviewProgressId: String? = null,
    onReviewBack: () -> Unit = {},
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
    val fillQuestionGenerationMode by settingsViewModel.fillQuestionGenerationMode.collectAsState()
    val fillBlankCount by settingsViewModel.fillBlankCount.collectAsState()
    val fillFullAnswerRandomOrder by settingsViewModel.fillFullAnswerRandomOrder.collectAsState()
    val fillFullAnswerRequireCorrect by settingsViewModel.fillFullAnswerRequireCorrect.collectAsState()
    val fillAnswerScoreMin by settingsViewModel.fillAnswerScoreMin.collectAsState()
    val fillAnswerScoreMax by settingsViewModel.fillAnswerScoreMax.collectAsState()
    val fillAnswerTagFilter by settingsViewModel.fillAnswerTagFilter.collectAsState()
    val fillConfigVersion = listOf(
        fillQuestionGenerationMode.storageValue,
        fillBlankCount,
        fillFullAnswerRandomOrder,
        fillFullAnswerRequireCorrect,
        fillAnswerScoreMin,
        fillAnswerScoreMax,
        fillAnswerTagFilter
    ).joinToString("|")
    LaunchedEffect(isReviewMode, reviewProgressId) {
        if (isReviewMode && !reviewProgressId.isNullOrBlank()) {
            viewModel.enterReviewSession(reviewProgressId)
        }
    }
    LaunchedEffect(
        quizId,
        isWrongBookMode,
        wrongBookFileName,
        isFavoriteMode,
        favoriteFileName,
        fillConfigVersion,
        practiceCount,
        randomPractice
    ) {
        if (isReviewMode) return@LaunchedEffect
        settingsViewModel.settingsReady.first { it }
        val count = practiceCount
        val random = randomPractice
        Log.d("PracticeScreen", "[INIT] randomPractice=$random, isWrongBookMode=$isWrongBookMode, wrongBookFileName=$wrongBookFileName, isFavoriteMode=$isFavoriteMode, favoriteFileName=$favoriteFileName, quizId=$quizId, practiceCount=$count, fillConfig=$fillConfigVersion")
        val targetProgressId = when {
            isWrongBookMode && wrongBookFileName != null -> "practice_wrongbook_${wrongBookFileName}"
            isFavoriteMode && favoriteFileName != null -> "practice_favorite_${favoriteFileName}"
            else -> "practice_${quizId}"
        }
        viewModel.setRandomPractice(random)
        if (targetProgressId == viewModel.currentProgressId && viewModel.currentProgressId.isNotBlank()) {
            viewModel.reloadForFillConfig(count)
            return@LaunchedEffect
        }
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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val wrongBookViewModel: WrongBookViewModel = hiltViewModel()
    val questions by viewModel.questions.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val correctDelay by settingsViewModel.correctDelay.collectAsState()
    val wrongDelay by settingsViewModel.wrongDelay.collectAsState()
    val soundEffects = rememberSoundEffects()
    val soundEnabled by settingsViewModel.soundEnabled.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val currentQuestionUi by viewModel.currentQuestionUi.collectAsState()
    val answeredList by viewModel.answeredList.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val reviewReady by viewModel.reviewReady.collectAsState()
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
    val analysisText = if (analysisPair?.first == currentIndex) analysisPair?.second
        else currentQuestionUi?.analysis ?: analysisList.getOrNull(currentIndex)
    val sparkText = if (sparkPair?.first == currentIndex) sparkPair?.second
        else currentQuestionUi?.sparkAnalysis ?: sparkAnalysisList.getOrNull(currentIndex)
    val baiduText = if (baiduPair?.first == currentIndex) baiduPair?.second
        else currentQuestionUi?.baiduAnalysis ?: baiduAnalysisList.getOrNull(currentIndex)
    val noteList by viewModel.noteList.collectAsState()
    val textAnswers by viewModel.textAnswers.collectAsState()
    val editableQuestion by viewModel.editableQuestion.collectAsState()
    val textAnswer = currentQuestionUi?.textAnswer ?: textAnswers.getOrNull(currentIndex).orEmpty()
    val resolvedFillAnswer = remember(question) { question?.let { resolveFillCorrectAnswer(it) }.orEmpty() }
    val parsingText = stringResource(R.string.parsing)
    val hasDeepSeekAnalysis = (currentQuestionUi?.analysis ?: analysisList.getOrNull(currentIndex)).orEmpty().isNotBlank()
    val hasSparkAnalysis = (currentQuestionUi?.sparkAnalysis ?: sparkAnalysisList.getOrNull(currentIndex)).orEmpty().isNotBlank()
    val hasBaiduAnalysis = (currentQuestionUi?.baiduAnalysis ?: baiduAnalysisList.getOrNull(currentIndex)).orEmpty().isNotBlank()
    val hasNote = (currentQuestionUi?.note ?: noteList.getOrNull(currentIndex)).orEmpty().isNotBlank()
    val selectedOption = currentQuestionUi?.selectedOptions ?: selectedOptions.getOrNull(currentIndex) ?: emptyList<Int>()
    val showResult = currentQuestionUi?.showResult ?: showResultList.getOrNull(currentIndex) ?: false
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

    val sessionAnsweredCount by viewModel.sessionAnsweredCountFlow.collectAsState()
    val sessionScore by viewModel.sessionCorrectCountFlow.collectAsState()
    var aiMenuExpanded by remember { mutableStateOf(false) }

    val fc = remember { PracticeFontController(fontSize, 1.3f, viewModel.fontSettingsRepository) }
    LaunchedEffect(Unit) { fc.loadFromStore() }
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
    LaunchedEffect(currentIndex) { mainScrollState.scrollTo(0) }
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
        if (isReviewMode) {
            onReviewBack()
            return@BackHandler
        }
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

    if (isReviewMode && !reviewReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (question == null || !progressLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_questions_loading), fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
        }
        return
    }

    var dragAmount by remember { mutableStateOf(0f) }
    val answeredHistoryAtLatestText = stringResource(R.string.answered_history_at_latest)
    val answeredHistoryAtOldestText = stringResource(R.string.answered_history_at_oldest)
    val fullAnswerNoPrevUnansweredSourceText = stringResource(R.string.full_answer_no_prev_unanswered_source)
    val fullAnswerNoNextUnansweredSourceText = stringResource(R.string.full_answer_no_next_unanswered_source)
    val unansweredNavAtFirstText = stringResource(R.string.unanswered_nav_at_first)
    val unansweredNavAtLastText = stringResource(R.string.unanswered_nav_at_last)
    val inAnsweredHistory = viewModel.isInAnsweredHistory()

    val postAnswerAdvance: suspend () -> Unit = {
        when (PracticePostAnswerAdvancePipeline.resolve(viewModel.hasPendingQuestions())) {
            PracticePostAnswerAdvancePipeline.Action.Advance -> viewModel.nextQuestion()
            PracticePostAnswerAdvancePipeline.Action.FinishOrPromptExit -> {
                if (sessionAnsweredCount >= viewModel.totalCount) {
                    viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, viewModel.totalCount - viewModel.answeredCount)
                    onQuizEnd(
                        sessionScore,
                        sessionAnsweredCount,
                        viewModel.totalCount - viewModel.answeredCount,
                        viewModel.correctCount,
                        viewModel.answeredCount
                    )
                } else {
                    showExitDialog = true
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(mainScrollState).padding(16.dp)
            .cancelAutoAdvanceOnTouch { autoAdvance.cancel() }
            .pointerInput(currentIndex) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, amount ->
                        if (amount != 0f) autoAdvance.cancel()
                        dragAmount += amount
                    },
                    onDragEnd = {
                        autoAdvance.cancel()
                        focusManager.clearFocus(force = true)
                        when {
                            dragAmount > 100f -> {
                                val result = viewModel.browseAnsweredHistoryOlder()
                                Log.d(
                                    "PracticeHistorySwipe",
                                    "UI.swipeRight | idx=$currentIndex | showResult=$showResult | result=$result"
                                )
                                when (result) {
                                    AnsweredHistoryBackwardResult.AtOldestAnswered,
                                    AnsweredHistoryBackwardResult.NoMoreHistory -> {
                                        if (showResult || inAnsweredHistory) {
                                            Toast.makeText(context, answeredHistoryAtOldestText, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else -> Unit
                                }
                            }
                            dragAmount < -100f -> {
                                val result = viewModel.browseAnsweredHistoryNewer()
                                Log.d(
                                    "PracticeHistorySwipe",
                                    "UI.swipeLeft | idx=$currentIndex | showResult=$showResult | inHistory=$inAnsweredHistory | result=$result"
                                )
                                when (result) {
                                    AnsweredHistoryForwardResult.AtLatestAnswered -> {
                                        Toast.makeText(context, answeredHistoryAtLatestText, Toast.LENGTH_SHORT).show()
                                    }
                                    else -> Unit
                                }
                            }
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
                    fc.increaseFont(coroutineScope)
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_font)) }, onClick = {
                    fc.decreaseFont(coroutineScope)
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.increase_line_spacing)) }, onClick = {
                    fc.increaseSpacing(coroutineScope); menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_line_spacing)) }, onClick = {
                    fc.decreaseSpacing(coroutineScope); menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.increase_letter_spacing)) }, onClick = {
                    fc.increaseLetterSpacing(coroutineScope); menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_letter_spacing)) }, onClick = {
                    fc.decreaseLetterSpacing(coroutineScope); menuExpanded = false
                })
                DropdownMenuItem(text = { Text(stringResource(R.string.edit_current_question)) }, onClick = {
                    menuExpanded = false
                    viewModel.clearEditableQuestion()
                    viewModel.prepareEditableQuestion(question.id)
                    showEditQuestionDialog = true
                })
            }
        )
        PracticeQuestionListDialog(
            show = showList,
            onDismiss = { showList = false },
            questions = questions,
            selectedOptions = selectedOptions,
            textAnswers = textAnswers,
            showResultList = showResultList,
            displayInfoByQuestionId = remember(questions) { viewModel.buildAnswerCardDisplayInfo(questions) },
            entryGrouped = remember(questions) { viewModel.answerCardEntryGrouped(questions) },
            currentIndex = currentIndex,
            onSelect = { viewModel.goToQuestion(it) }
        )

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
        FillAnswerRoundLabel(
            questionId = question.id,
            sessionQuestionIds = questions.map { it.id },
            modifier = Modifier.padding(bottom = 8.dp)
        ) { round, total ->
            Text(
                text = stringResource(R.string.fill_full_answer_round_template, round, total),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (LocalFontSize.current.value - 1f).coerceAtLeast(12f).sp,
                    fontFamily = LocalFontFamily.current,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Question content
        key(currentIndex) {
        PracticeQuestionContent(
            question = question,
            textAnswer = textAnswer,
            showResult = showResult,
            selectedOption = selectedOption,
            displayOptions = displayOptions,
            resolvedFillAnswer = resolvedFillAnswer,
            questionFontSize = fc.questionFontSize,
            questionLineSpacing = fc.questionLineSpacing,
            questionLetterSpacing = fc.questionLetterSpacing,
            onAnswerChange = if (isReviewMode) { {} } else { { viewModel.updateTextAnswer(it) } },
            onOptionClick = if (isReviewMode) { {} } else { { idx ->
                answeredThisSession = true
                if (QuestionTypes.isMulti(question.type)) viewModel.toggleOption(idx)
            } },
            submitCurrentAnswer = if (isReviewMode) { {} } else { { idx ->
                if (idx != null) {
                    answeredThisSession = true
                    val answeredIndex = currentIndex
                    val allCorrect = PracticeAnswerCorrectnessPipeline.isAllCorrect(
                        question = question,
                        textAnswer = textAnswer,
                        selectedOptions = listOf(idx),
                        resolvedFillAnswer = resolvedFillAnswer,
                        correctIndices = correctIndices
                    )
                    viewModel.answerQuestion(idx)
                    PracticeSubmitRevealPipeline.revealImmediately(answeredIndex, viewModel::revealShowResult)
                    autoAdvance.schedule(
                        coroutineScope,
                        answeredIndex,
                        delaySec = if (allCorrect) correctDelay else wrongDelay,
                        revealResultFirst = false,
                        showResult = viewModel::updateShowResult,
                        onAdvance = postAnswerAdvance,
                        advanceOnly = true
                    )
                    coroutineScope.launch {
                        PracticeSubmitSideEffectsPipeline.apply(
                            allCorrect = allCorrect,
                            soundEnabled = soundEnabled,
                            playCorrect = soundEffects::playCorrect,
                            playWrong = soundEffects::playWrong,
                            onSubmit = onSubmit,
                            onWrongAnswer = {
                                wrongBookViewModel.addWrongQuestion(WrongQuestion(question, listOf(idx)))
                            }
                        )
                    }
                }
            } }
        )
        }

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
                questionFontSize = fc.questionFontSize,
                questionLineSpacing = fc.questionLineSpacing,
                questionLetterSpacing = fc.questionLetterSpacing,
                allCorrect = answerResult.allCorrect,
                correctText = answerResult.correctText,
                answerResultText = answerResultText,
            retryLabel = if (isReviewMode) "" else stringResource(R.string.retry_current_question),
            retryWrongLabel = if (isReviewMode) "" else stringResource(R.string.retry_wrong_blanks),
                onInteraction = { autoAdvance.cancel() },
                onRetry = if (isReviewMode) {
                    {}
                } else {
                    {
                        autoAdvance.cancel()
                        answeredThisSession = false
                        viewModel.retryCurrentQuestion(currentIndex)
                    }
                },
                onRetryWrongBlanks = if (isReviewMode || !QuestionTypes.isInlineBlank(question.type)) {
                    null
                } else {
                    {
                        autoAdvance.cancel()
                        answeredThisSession = false
                        viewModel.retryWrongBlanks(currentIndex)
                    }
                }
            )
            val analysisPrefix = stringResource(R.string.analysis_prefix)
            val cancelAutoAdvance = { autoAdvance.cancel() }
            ExamAnalysisSection(
                text = question.explanation.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFFFF5C0),
                label = analysisPrefix,
                fontSize = fc.questionFontSize,
                lineHeight = fc.questionLineSpacing,
                letterSpacing = fc.questionLetterSpacing,
                onInteraction = cancelAutoAdvance,
                onDoubleTap = { onViewExplanation(analysisPrefix + question.explanation) },
                onLongPress = { showDeleteExplanationDialog = true }
            )
            val note = noteList.getOrNull(currentIndex)
            ExamAnalysisSection(
                text = note?.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFE0FFE0),
                label = stringResource(R.string.note_prefix),
                fontSize = fc.questionFontSize,
                lineHeight = fc.questionLineSpacing,
                letterSpacing = fc.questionLetterSpacing,
                onInteraction = cancelAutoAdvance,
                onDoubleTap = { question?.let { onEditNote(note!!, it.id, currentIndex) } },
                onLongPress = { showDeleteNoteDialog = true }
            )
            ExamAnalysisSection(
                text = analysisText?.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFE8F6FF),
                label = stringResource(R.string.export_header_deepseek),
                fontSize = fc.questionFontSize,
                lineHeight = fc.questionLineSpacing,
                letterSpacing = fc.questionLetterSpacing,
                onInteraction = cancelAutoAdvance,
                onDoubleTap = { question?.let { onViewDeepSeek(analysisText ?: "", it.id, currentIndex) } },
                onLongPress = { deleteTarget = "deepseek"; showDeleteDialog = true }
            )
            ExamAnalysisSection(
                text = sparkText?.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFEDE7FF),
                label = stringResource(R.string.export_header_spark),
                fontSize = fc.questionFontSize,
                lineHeight = fc.questionLineSpacing,
                letterSpacing = fc.questionLetterSpacing,
                onInteraction = cancelAutoAdvance,
                onDoubleTap = { question?.let { onViewSpark(sparkText ?: "", it.id, currentIndex) } },
                onLongPress = { deleteTarget = "spark"; showDeleteDialog = true }
            )
            ExamAnalysisSection(
                text = baiduText?.takeIf { it.isNotBlank() },
                backgroundColor = Color(0xFFF0F8E7),
                label = stringResource(R.string.export_header_baidu),
                fontSize = fc.questionFontSize,
                lineHeight = fc.questionLineSpacing,
                letterSpacing = fc.questionLetterSpacing,
                onInteraction = cancelAutoAdvance,
                onDoubleTap = { question?.let { onViewBaidu(baiduText ?: "", it.id, currentIndex) } },
                onLongPress = { deleteTarget = "baidu"; showDeleteDialog = true }
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!isReviewMode) {
            fun requestPracticeSubmitDialog() {
                autoAdvance.cancel()
                focusManager.clearFocus(force = true)
                when (PracticeSubmitFlow.resolve(answeredThisSession)) {
                    PracticeSubmitFlow.Action.ExitWithoutAnswer -> onExitWithoutAnswer()
                    PracticeSubmitFlow.Action.ShowSubmitDialog -> showExitDialog = true
                }
            }
            val submitManualAnswer: (() -> Unit)? = if (!showResult && !inAnsweredHistory) {
                {
                autoAdvance.cancel()
                answeredThisSession = true
                val answeredIndex = currentIndex
                val allCorrect = PracticeAnswerCorrectnessPipeline.isAllCorrect(
                    question = question,
                    textAnswer = textAnswer,
                    selectedOptions = selectedOption,
                    resolvedFillAnswer = resolvedFillAnswer,
                    correctIndices = correctIndices
                )
                PracticeSubmitRevealPipeline.revealImmediately(answeredIndex, viewModel::revealShowResult)
                autoAdvance.schedule(
                    coroutineScope,
                    answeredIndex,
                    delaySec = if (allCorrect) correctDelay else wrongDelay,
                    revealResultFirst = false,
                    showResult = viewModel::updateShowResult,
                    onAdvance = postAnswerAdvance,
                    advanceOnly = true
                )
                coroutineScope.launch {
                    focusManager.clearFocus(force = true)
                    PracticeSubmitSideEffectsPipeline.apply(
                        allCorrect = allCorrect,
                        soundEnabled = soundEnabled,
                        playCorrect = soundEffects::playCorrect,
                        playWrong = soundEffects::playWrong,
                        onSubmit = onSubmit,
                        onWrongAnswer = {
                            if (selectedOption.isNotEmpty() || textAnswer.isNotBlank()) {
                                wrongBookViewModel.addWrongQuestion(WrongQuestion(question, selectedOption))
                            }
                        }
                    )
                }
                }
            } else null
            val fullAnswerSkipEnabled = viewModel.isFullAnswerMode && !isReviewMode
            val canSkipPrevSource = fullAnswerSkipEnabled && viewModel.canSkipToUnansweredSource(forward = false)
            val canSkipNextSource = fullAnswerSkipEnabled && viewModel.canSkipToUnansweredSource(forward = true)
            QuestionNavigationControls(
                visible = true,
                onPrev = {
                    autoAdvance.cancel()
                    focusManager.clearFocus(force = true)
                    when (viewModel.prevQuestionViaIcon()) {
                        UnansweredNavResult.AtFirstUnanswered -> {
                            Toast.makeText(context, unansweredNavAtFirstText, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                },
                onNext = {
                    autoAdvance.cancel()
                    focusManager.clearFocus(force = true)
                    when (viewModel.nextQuestionViaIcon()) {
                        UnansweredNavResult.AtLastUnanswered -> {
                            Toast.makeText(context, unansweredNavAtLastText, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                },
                onPrevDoubleClick = if (fullAnswerSkipEnabled) {
                    {
                        autoAdvance.cancel()
                        focusManager.clearFocus(force = true)
                        when (viewModel.skipToUnansweredSource(forward = false)) {
                            SkipUnansweredSourceResult.NoPrevSource -> {
                                Toast.makeText(context, fullAnswerNoPrevUnansweredSourceText, Toast.LENGTH_SHORT).show()
                            }
                            else -> Unit
                        }
                    }
                } else null,
                onNextDoubleClick = if (fullAnswerSkipEnabled) {
                    {
                        autoAdvance.cancel()
                        focusManager.clearFocus(force = true)
                        when (viewModel.skipToUnansweredSource(forward = true)) {
                            SkipUnansweredSourceResult.NoNextSource -> {
                                Toast.makeText(context, fullAnswerNoNextUnansweredSourceText, Toast.LENGTH_SHORT).show()
                            }
                            else -> Unit
                        }
                    }
                } else null,
                onSubmit = submitManualAnswer,
                onSubmitDoubleClick = ::requestPracticeSubmitDialog,
                submitContentDescription = stringResource(R.string.submit_answer),
                enabledPrev = viewModel.canNavigateToPrevUnanswered() || canSkipPrevSource ||
                    showResult || inAnsweredHistory,
                enabledNext = viewModel.canNavigateToNextUnanswered() || canSkipNextSource ||
                    showResult || inAnsweredHistory
            )
        }
        if (isReviewMode) {
            QuestionNavigationControls(
                visible = true,
                onPrev = { viewModel.prevQuestion() },
                onNext = { viewModel.nextQuestion() },
                onSubmit = null,
                enabledPrev = viewModel.canReviewBrowseBack(),
                enabledNext = viewModel.canReviewBrowseForward()
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
                        fontSize = fc.questionFontSize.sp,
                        fontFamily = LocalFontFamily.current,
                        lineHeight = (fc.questionFontSize * fc.questionLineSpacing).sp,
                        letterSpacing = fc.questionLetterSpacing.sp
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
