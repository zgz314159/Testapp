package com.example.testapp.presentation.screen.practice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.R
import com.example.testapp.core.util.FavoriteSessionPipeline
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.resolveDisplayOptions
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.screen.favorite.FavoriteViewModel
import com.example.testapp.core.util.SessionAnalysisResolvePipeline
import com.example.testapp.presentation.screen.practice.components.PracticeScreenAnalysisSyncEffects
import com.example.testapp.presentation.screen.practice.components.PracticeScreenBottomBar
import com.example.testapp.presentation.screen.practice.components.PracticeScreenEditQuestionDraftEffect
import com.example.testapp.presentation.screen.practice.components.PracticeScreenIndexWatchEffect
import com.example.testapp.presentation.screen.practice.components.rememberPracticeOverlayNavigation
import com.example.testapp.presentation.screen.practice.components.PracticeScreenOverlays
import com.example.testapp.presentation.screen.practice.components.PracticeScreenQuestionScrollContent
import com.example.testapp.presentation.screen.practice.components.PracticeScreenQuizInitEffect
import com.example.testapp.presentation.screen.practice.components.PracticeScreenReviewInitEffect
import com.example.testapp.presentation.screen.practice.components.practiceHistorySwipe
import com.example.testapp.presentation.screen.practice.components.rememberPracticeAnsweredThisSession
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.screen.wrongbook.WrongBookViewModel
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
import com.example.testapp.uicommon.component.cancelAutoAdvanceOnTouch
import com.example.testapp.uicommon.design.AppEmptyState
import com.example.testapp.uicommon.design.AppLoadingContent
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.PracticeExamTopBar
import com.example.testapp.uicommon.design.QuestionSessionChromeLayout
import com.example.testapp.uicommon.layout.ScreenSafeScaffold
import com.example.testapp.uicommon.util.formatQuestionForAi
import com.example.testapp.uicommon.util.formatQuestionForCopy
import com.example.testapp.util.rememberSoundEffects
import kotlinx.coroutines.launch

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

    PracticeScreenReviewInitEffect(isReviewMode, reviewProgressId, viewModel)
    PracticeScreenQuizInitEffect(
        quizId, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName,
        fillConfigVersion, practiceCount, randomPractice, isReviewMode, settingsViewModel, viewModel
    )

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
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val reviewReady by viewModel.reviewReady.collectAsState()
    val showResultList by viewModel.showResultList.collectAsState()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    LaunchedEffect(Unit) { favoriteViewModel.ensureFullListLoaded() }
    val favoriteQuestions by favoriteViewModel.favoriteQuestions.collectAsState()
    val question = questions.getOrNull(currentIndex)
    val analysisPair by aiViewModel.analysis.collectAsState()
    val sparkPair by sparkViewModel.analysis.collectAsState()
    val baiduPair by baiduQianfanViewModel.analysisResult.collectAsState()
    val analysisList by viewModel.analysisList.collectAsState()
    val sparkAnalysisList by viewModel.sparkAnalysisList.collectAsState()
    val baiduAnalysisList by viewModel.baiduAnalysisList.collectAsState()
    val questionTextForAi = remember(question) { question?.let(::formatQuestionForAi).orEmpty() }
    val questionCopyText = remember(question) { question?.let(::formatQuestionForCopy).orEmpty() }
    val parsingText = stringResource(R.string.parsing)
    val parsingKeyword = parsingText.removeSuffix("...")
    val noteList by viewModel.noteList.collectAsState()
    val textAnswers by viewModel.textAnswers.collectAsState()
    val editableQuestion by viewModel.editableQuestion.collectAsState()
    val textAnswer = PracticeQuestionUiResolvePipeline.textAnswer(currentQuestionUi, currentIndex, textAnswers)
    val resolvedFillAnswer = remember(question) { question?.let { resolveFillCorrectAnswer(it) }.orEmpty() }
    val selectedOption = PracticeQuestionUiResolvePipeline.selectedOptions(currentQuestionUi, currentIndex, selectedOptions)
    val showResult = PracticeQuestionUiResolvePipeline.showResult(currentQuestionUi, currentIndex, showResultList)
    val analysisText = SessionAnalysisResolvePipeline.resolve(
        currentIndex = currentIndex,
        streamingPair = analysisPair,
        sessionValue = if (currentQuestionUi?.index == currentIndex) currentQuestionUi?.analysis else null,
        listValue = analysisList.getOrNull(currentIndex),
        parsingKeyword = parsingKeyword
    )
    val sparkText = SessionAnalysisResolvePipeline.resolve(
        currentIndex = currentIndex,
        streamingPair = sparkPair,
        sessionValue = if (currentQuestionUi?.index == currentIndex) currentQuestionUi?.sparkAnalysis else null,
        listValue = sparkAnalysisList.getOrNull(currentIndex),
        parsingKeyword = parsingKeyword
    )
    val baiduText = SessionAnalysisResolvePipeline.resolve(
        currentIndex = currentIndex,
        streamingPair = baiduPair,
        sessionValue = if (currentQuestionUi?.index == currentIndex) currentQuestionUi?.baiduAnalysis else null,
        listValue = baiduAnalysisList.getOrNull(currentIndex),
        parsingKeyword = parsingKeyword
    )
    val hasDeepSeekAnalysis = !analysisText.isNullOrBlank()
    val hasSparkAnalysis = !sparkText.isNullOrBlank()
    val hasBaiduAnalysis = !baiduText.isNullOrBlank()
    val hasNote = (currentQuestionUi?.note ?: noteList.getOrNull(currentIndex)).orEmpty().isNotBlank()
    val resultDisplayReady = rememberPracticeResultDisplayReady(showResult, currentIndex)
    val displayOptions = remember(question) { question?.let(::resolveDisplayOptions).orEmpty() }
    val correctIndices = remember(question) { question?.let(::answerToOptionIndices).orEmpty() }
    val isFavorite = remember(question?.id, favoriteQuestions) {
        question?.id?.let { FavoriteSessionPipeline.isFavorite(it, favoriteQuestions) } ?: false
    }
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); elapsed += 1 } }

    PracticeScreenAnalysisSyncEffects(
        question, currentIndex, showResult, resultDisplayReady, parsingText, analysisPair, sparkPair, baiduPair,
        aiViewModel, sparkViewModel, baiduQianfanViewModel, viewModel
    )

    var showList by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showTypographySheet by remember { mutableStateOf(false) }
    var showEditQuestionDialog by remember { mutableStateOf(false) }
    var editedQuestionContent by remember { mutableStateOf("") }
    var editedQuestionAnswer by remember { mutableStateOf("") }
    var editedAnswerParts by remember { mutableStateOf(listOf<String>()) }
    val sessionAnsweredCount by viewModel.sessionAnsweredCountFlow.collectAsState()
    val sessionScore by viewModel.sessionCorrectCountFlow.collectAsState()
    val hasSessionInput by viewModel.hasAnyInputInSessionFlow.collectAsState()
    val sessionInputCount by viewModel.sessionInputCountFlow.collectAsState()
    var aiMenuExpanded by remember { mutableStateOf(false) }
    val fc = remember { PracticeFontController(fontSize, 1.3f, viewModel.fontSettingsRepository) }
    LaunchedEffect(Unit) { fc.loadFromStore() }
    var showChatGptDialog by remember { mutableStateOf(false) }
    val chatGptResult by baiduQianfanViewModel.analysisResult.collectAsState()
    val chatGptLoading by baiduQianfanViewModel.loading.collectAsState()
    val autoAdvance = remember { PracticeAutoAdvanceController() }
    val overlayNav = rememberPracticeOverlayNavigation(autoAdvance, currentIndex, question?.id, viewModel)
    PracticeScreenIndexWatchEffect(currentIndex, question?.id, showResult)
    val (answeredThisSession, setAnsweredThisSession) = rememberPracticeAnsweredThisSession(progressLoaded)
    var showExitDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf("") }
    var showDeleteNoteDialog by remember { mutableStateOf(false) }
    var showDeleteExplanationDialog by remember { mutableStateOf(false) }
    var showExplanationFull by remember { mutableStateOf(false) }
    val mainScrollState = rememberScrollState()
    LaunchedEffect(currentIndex) { mainScrollState.scrollTo(0) }
    val answerCardDisplayInfo = remember(showList, questions) {
        if (showList) viewModel.buildAnswerCardDisplayInfo(questions) else emptyMap()
    }
    val answerCardEntryGrouped = remember(showList, questions) {
        if (showList) viewModel.answerCardEntryGrouped(questions) else false
    }
    PracticeScreenEditQuestionDraftEffect(editableQuestion) { content, answer, parts ->
        editedQuestionContent = content
        editedQuestionAnswer = answer
        editedAnswerParts = parts
    }

    val sessionExitLabel = if (isReviewMode) stringResource(R.string.review_back)
    else stringResource(R.string.practice_exit_session)
    val requestSessionExit: () -> Unit = {
        autoAdvance.cancel()
        when (
            val action = PracticeSessionExitPipeline.resolve(
                isReviewMode = isReviewMode,
                answeredThisSession = answeredThisSession,
                hasSessionInput = hasSessionInput,
                sessionAnsweredCount = sessionAnsweredCount,
                totalCount = viewModel.totalCount,
                sessionScore = sessionScore,
                realUnanswered = viewModel.totalCount - viewModel.answeredCount
            )
        ) {
            PracticeSessionExitPipeline.Action.ReviewBack -> onReviewBack()
            PracticeSessionExitPipeline.Action.ExitWithoutAnswer -> onExitWithoutAnswer()
            is PracticeSessionExitPipeline.Action.FinishDirect -> {
                if (sessionAnsweredCount > 0) {
                    viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, action.realUnanswered)
                }
                onQuizEnd(
                    action.sessionScore, action.sessionAnsweredCount, action.realUnanswered,
                    viewModel.correctCount, viewModel.answeredCount
                )
            }
            PracticeSessionExitPipeline.Action.ShowSubmitDialog -> showExitDialog = true
        }
    }
    BackHandler(onBack = requestSessionExit)

    ScreenSafeScaffold { contentModifier ->
        if (isReviewMode && !reviewReady) {
            AppLoadingContent(modifier = contentModifier)
            return@ScreenSafeScaffold
        }
        if (!progressLoaded) {
            AppLoadingContent(modifier = contentModifier, message = stringResource(R.string.no_questions_loading))
            return@ScreenSafeScaffold
        }
        if (question == null) {
            AppEmptyState(message = stringResource(R.string.no_questions), modifier = contentModifier)
            return@ScreenSafeScaffold
        }

        val inAnsweredHistory = viewModel.isInAnsweredHistory()
        val postAnswerAdvance: suspend () -> Unit = {
            when (val action = PracticePostAnswerAdvancePipeline.resolve(viewModel.hasPendingQuestions())) {
                PracticePostAnswerAdvancePipeline.Action.Advance -> {
                    com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog.postAnswerAdvance(
                        "Advance", currentIndex
                    )
                    viewModel.nextQuestion()
                }
                PracticePostAnswerAdvancePipeline.Action.FinishOrPromptExit -> {
                    com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog.postAnswerAdvance(
                        "FinishOrPromptExit", currentIndex
                    )
                    if (sessionAnsweredCount >= viewModel.totalCount) {
                        viewModel.addHistoryRecord(sessionScore, viewModel.totalCount, viewModel.totalCount - viewModel.answeredCount)
                        onQuizEnd(sessionScore, sessionAnsweredCount, viewModel.totalCount - viewModel.answeredCount,
                            viewModel.correctCount, viewModel.answeredCount)
                    } else showExitDialog = true
                }
            }
        }

        QuestionSessionChromeLayout(
            scrollState = mainScrollState,
            onScrollInProgress = { if (it) autoAdvance.cancel() },
            modifier = contentModifier.fillMaxSize().padding(AppSpacing.md)
                .cancelAutoAdvanceOnTouch { autoAdvance.cancel() }
                .practiceHistorySwipe(
                    currentIndex, showResult, inAnsweredHistory, viewModel,
                    { autoAdvance.cancel() }, { focusManager.clearFocus(force = true) }, context,
                    stringResource(R.string.answered_history_at_oldest),
                    stringResource(R.string.answered_history_at_latest)
                ),
            topBar = {
                PracticeExamTopBar(
                    elapsedSeconds = elapsed,
                    onRequestExit = requestSessionExit,
                    exitContentDescription = sessionExitLabel,
                    isFavorite = isFavorite,
                    favoriteAddLabel = stringResource(R.string.favorite_add),
                    favoriteRemoveLabel = stringResource(R.string.favorite_remove),
                    notesLabel = stringResource(R.string.notes),
                    onEditNote = {
                        overlayNav {
                            onEditNote(noteList.getOrNull(currentIndex)?.takeIf { it.isNotBlank() } ?: " ", question.id, currentIndex)
                        }
                    },
                    hasNote = hasNote,
                    aiParseLabel = stringResource(R.string.ai_parse),
                    deepSeekLabel = stringResource(R.string.ai_name_deepseek),
                    sparkLabel = stringResource(R.string.ai_name_spark),
                    aiMenuExpanded = aiMenuExpanded,
                    onAiMenuToggle = { aiMenuExpanded = true },
                    onAiMenuDismiss = { aiMenuExpanded = false },
                    onOpenAiMenu = { overlayNav { onAskDeepSeek(questionTextForAi, question.id, currentIndex) } },
                    onOpenAskMenu = { overlayNav { onAskSpark(questionTextForAi, question.id, currentIndex) } },
                    onToggleFavorite = {
                        if (isFavorite) favoriteViewModel.removeFavorite(question.id)
                        else favoriteViewModel.addFavorite(question)
                    },
                    onOpenTypography = { showTypographySheet = true },
                    onEditQuestion = {
                        viewModel.clearEditableQuestion()
                        viewModel.prepareEditableQuestion(question.id)
                        showEditQuestionDialog = true
                    },
                    settingsLabel = stringResource(R.string.settings),
                    settingsMenuExpanded = menuExpanded,
                    onMenuToggle = { menuExpanded = true },
                    onMenuDismiss = { menuExpanded = false },
                    hasAnyAnalysis = hasDeepSeekAnalysis || hasSparkAnalysis || hasBaiduAnalysis
                )
            },
            scrollContent = {
                PracticeScreenQuestionScrollContent(
                    questions, question, currentIndex, isReviewMode, textAnswer, showResult, resultDisplayReady,
                    selectedOption, displayOptions, resolvedFillAnswer, correctIndices, fc, questionCopyText,
                    analysisText, sparkText, baiduText, noteList, viewModel, autoAdvance,
                    onOpenQuestionList = { showList = true },
                    onRetryCurrent = { setAnsweredThisSession(false); viewModel.retryCurrentQuestion(currentIndex) },
                    onRetryWrongBlanks = { setAnsweredThisSession(false); viewModel.retryWrongBlanks(currentIndex) },
                    onViewExplanation = { overlayNav { onViewExplanation(it) } },
                    onEditNote = { note, id, idx -> overlayNav { onEditNote(note, id, idx) } },
                    onViewDeepSeek = { text, id, idx -> overlayNav { onViewDeepSeek(text, id, idx) } },
                    onViewSpark = { text, id, idx -> overlayNav { onViewSpark(text, id, idx) } },
                    onViewBaidu = { text, id, idx -> overlayNav { onViewBaidu(text, id, idx) } },
                    onDeleteExplanation = { showDeleteExplanationDialog = true },
                    onDeleteNote = { showDeleteNoteDialog = true },
                    onDeleteAnalysis = { deleteTarget = it; showDeleteDialog = true },
                    onOptionClick = { idx ->
                        setAnsweredThisSession(true)
                        if (QuestionTypes.isMulti(question.type)) viewModel.toggleOption(idx)
                    },
                    onSubmitOption = { idx ->
                        if (idx != null) {
                            setAnsweredThisSession(true)
                            val answeredIndex = currentIndex
                            val allCorrect = PracticeAnswerCorrectnessPipeline.isAllCorrect(
                                question, textAnswer, listOf(idx), resolvedFillAnswer, correctIndices
                            )
                            viewModel.answerQuestion(idx)
                            PracticeSubmitRevealPipeline.revealImmediately(answeredIndex, viewModel::revealShowResult)
                            autoAdvance.schedule(coroutineScope, answeredIndex,
                                if (allCorrect) correctDelay else wrongDelay, false,
                                viewModel::updateShowResult, postAnswerAdvance, true)
                            coroutineScope.launch {
                                PracticeSubmitSideEffectsPipeline.apply(allCorrect, soundEnabled,
                                    soundEffects::playCorrect, soundEffects::playWrong, onSubmit) {
                                    wrongBookViewModel.addWrongQuestion(WrongQuestion(question, listOf(idx)))
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                PracticeScreenBottomBar(
                    isReviewMode, showResult, inAnsweredHistory, answeredThisSession, hasSessionInput,
                    currentIndex, question, textAnswer, selectedOption, resolvedFillAnswer, correctIndices,
                    correctDelay, wrongDelay, soundEnabled, viewModel, wrongBookViewModel, autoAdvance,
                    coroutineScope, postAnswerAdvance, onSubmit, onExitWithoutAnswer,
                    { showExitDialog = true }, { setAnsweredThisSession(true) }
                )
            }
        )
    }

    val activeQuestion = question ?: return
    PracticeScreenOverlays(
        showTypographySheet, { showTypographySheet = false }, fc, coroutineScope,
        showList, { showList = false }, questions, selectedOptions, textAnswers, showResultList,
        answerCardDisplayInfo, answerCardEntryGrouped, currentIndex, {
            autoAdvance.cancel()
            viewModel.goToQuestion(it)
        },
        showEditQuestionDialog, editableQuestion, editedQuestionContent, editedQuestionAnswer, editedAnswerParts,
        onConfirmEdit = { newContent, newOptions, finalAnswer ->
            coroutineScope.launch {
                viewModel.updateQuestionAllFields(currentIndex, newContent, newOptions, finalAnswer, activeQuestion.explanation)
                showEditQuestionDialog = false
                viewModel.clearEditableQuestion()
            }
        },
        onDismissEdit = { showEditQuestionDialog = false; viewModel.clearEditableQuestion() },
        showExplanationFull, { showExplanationFull = false }, activeQuestion,
        showDeleteExplanationDialog, { showDeleteExplanationDialog = false },
        { viewModel.clearExplanation(currentIndex, activeQuestion); showDeleteExplanationDialog = false },
        showDeleteNoteDialog, { showDeleteNoteDialog = false },
        { viewModel.saveNote(activeQuestion.id, currentIndex, ""); showDeleteNoteDialog = false },
        showDeleteDialog, deleteTarget, { showDeleteDialog = false; deleteTarget = "" },
        {
            when (deleteTarget) {
                "deepseek" -> { aiViewModel.clear(); viewModel.updateAnalysis(currentIndex, "") }
                "spark" -> { sparkViewModel.clear(); viewModel.updateSparkAnalysis(currentIndex, "") }
                "baidu" -> { baiduQianfanViewModel.clearResult(); viewModel.updateBaiduAnalysis(currentIndex, "") }
            }
            showDeleteDialog = false; deleteTarget = ""
        },
        showExitDialog, sessionInputCount, viewModel.totalCount, { showExitDialog = false },
        autoAdvance, viewModel, onQuizEnd,
        showChatGptDialog, { showChatGptDialog = false }, chatGptLoading,
        chatGptResult?.let { Pair(it.first, com.example.testapp.core.common.LocalizedResult(it.second ?: "", emptyList())) },
        { viewModel.updateAnalysis(currentIndex, it) }
    )
}
