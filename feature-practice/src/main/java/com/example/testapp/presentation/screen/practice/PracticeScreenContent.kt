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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.example.testapp.core.util.SessionAnalysisResolvePipeline
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.resolveDisplayOptions
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.practice.components.PracticeScreenBottomBar
import com.example.testapp.presentation.screen.practice.components.PracticeScreenEditQuestionDraftEffect
import com.example.testapp.presentation.screen.practice.components.PracticeScreenIndexWatchEffect
import com.example.testapp.presentation.screen.practice.components.PracticeScreenOptionSubmitHandlers
import com.example.testapp.presentation.screen.practice.components.PracticeScreenOverlays
import com.example.testapp.presentation.screen.practice.components.PracticeScreenQuestionScrollContent
import com.example.testapp.presentation.screen.practice.components.PracticeScreenQuizInitEffect
import com.example.testapp.presentation.screen.practice.components.PracticeScreenReviewInitEffect
import com.example.testapp.presentation.screen.practice.components.practiceHistorySwipe
import com.example.testapp.presentation.screen.practice.components.rememberPracticeAnsweredThisSession
import com.example.testapp.presentation.screen.practice.components.rememberPracticeOverlayNavigation
import com.example.testapp.presentation.session.practice.PracticeCommandOutcome
import com.example.testapp.presentation.session.practice.PracticeScreenBindings
import com.example.testapp.uicommon.component.cancelAutoAdvanceOnTouch
import com.example.testapp.uicommon.design.AppEmptyState
import com.example.testapp.uicommon.design.AppLoadingContent
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.PracticeExamTopBar
import com.example.testapp.uicommon.design.QuestionSessionChromeLayout
import com.example.testapp.uicommon.layout.ScreenSafeScaffold
import com.example.testapp.uicommon.util.formatQuestionForAi
import com.example.testapp.uicommon.util.formatQuestionForCopy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreenContent(
    quizId: String = "default",
    isWrongBookMode: Boolean = false,
    wrongBookFileName: String? = null,
    isFavoriteMode: Boolean = false,
    favoriteFileName: String? = null,
    isReviewMode: Boolean = false,
    reviewProgressId: String? = null,
    onReviewBack: () -> Unit = {},
    bindings: PracticeScreenBindings,
    dispatchCommand: (SessionCommand) -> PracticeCommandOutcome?,
    externalState: ExternalPracticeState = ExternalPracticeState(),
    sessionHosted: Boolean = false,
    persistentQuestionActionsEnabled: Boolean = true,
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
    onEditCorrectAnswer: (String, Int, Int) -> Unit = { _, _, _ -> },
    onEditNote: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    val randomPractice = externalState.randomPractice
    val practiceCount = externalState.practiceCount
    val fillConfigVersion = externalState.fillConfigVersion
    val sendCommand: (SessionCommand) -> Unit = { dispatchCommand(it) }

    if (!sessionHosted) {
        PracticeScreenReviewInitEffect(isReviewMode, reviewProgressId, bindings, sendCommand, sessionHosted)
        PracticeScreenQuizInitEffect(
            quizId, isWrongBookMode, wrongBookFileName, isFavoriteMode, favoriteFileName,
            fillConfigVersion, practiceCount, randomPractice, isReviewMode,
            externalState.settingsReady, bindings, sendCommand, sessionHosted,
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val questions by bindings.questions.collectAsState()
    val fontSize = externalState.fontSize
    val correctDelay = externalState.correctDelay
    val wrongDelay = externalState.wrongDelay
    val soundEnabled = externalState.soundEnabled
    val currentIndex by bindings.currentIndex.collectAsState()
    val currentQuestionUi by bindings.currentQuestionUi.collectAsState()
    val selectedOptions by bindings.selectedOptions.collectAsState()
    val progressLoaded by bindings.progressLoaded.collectAsState()
    val reviewReady by bindings.reviewReady.collectAsState()
    val showResultList by bindings.showResultList.collectAsState()
    val question = questions.getOrNull(currentIndex)
    val analysisPair = externalState.analysisPair
    val sparkPair = externalState.sparkPair
    val baiduPair = externalState.baiduPair
    val analysisList by bindings.analysisList.collectAsState()
    val sparkAnalysisList by bindings.sparkAnalysisList.collectAsState()
    val baiduAnalysisList by bindings.baiduAnalysisList.collectAsState()
    val questionTextForAi = remember(question) { question?.let(::formatQuestionForAi).orEmpty() }
    val questionCopyText = remember(question) { question?.let(::formatQuestionForCopy).orEmpty() }
    val parsingText = stringResource(R.string.parsing)
    val parsingKeyword = parsingText.removeSuffix("...")
    val noteList by bindings.noteList.collectAsState()
    val textAnswers by bindings.textAnswers.collectAsState()
    val editableQuestion by bindings.editableQuestion.collectAsState()
    val textAnswer = PracticeQuestionUiResolvePipeline.textAnswer(currentQuestionUi, currentIndex, textAnswers)
    val resolvedFillAnswer = remember(question) { question?.let { resolveFillCorrectAnswer(it) }.orEmpty() }
    val selectedOption = PracticeQuestionUiResolvePipeline.selectedOptions(currentQuestionUi, currentIndex, selectedOptions)
    val showResult = PracticeQuestionUiResolvePipeline.showResult(currentQuestionUi, currentIndex, showResultList)
    val analysisTextRaw = SessionAnalysisResolvePipeline.resolve(
        currentIndex = currentIndex,
        streamingPair = analysisPair,
        sessionValue = if (currentQuestionUi?.index == currentIndex) currentQuestionUi?.analysis else null,
        listValue = analysisList.getOrNull(currentIndex),
        parsingKeyword = parsingKeyword
    )
    val analysisText = analysisTextRaw?.let {
        com.example.testapp.data.network.deepseek.SessionAnalysisInlineDisplayPipeline.toDisplayText(
            it,
            question?.content.orEmpty(),
        )
    }?.takeIf { it.isNotBlank() } ?: analysisTextRaw
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
    val isFavorite = externalState.isFavorite
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); elapsed += 1 } }

    var showList by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showTypographySheet by remember { mutableStateOf(false) }
    var showEditQuestionDialog by remember { mutableStateOf(false) }
    var editedQuestionContent by remember { mutableStateOf("") }
    var editedQuestionAnswer by remember { mutableStateOf("") }
    var editedAnswerParts by remember { mutableStateOf(listOf<String>()) }
    val sessionAnsweredCount by bindings.sessionAnsweredCountFlow.collectAsState()
    val sessionScore by bindings.sessionCorrectCountFlow.collectAsState()
    val hasSessionInput by bindings.hasAnyInputInSessionFlow.collectAsState()
    val sessionInputCount by bindings.sessionInputCountFlow.collectAsState()
    var aiMenuExpanded by remember { mutableStateOf(false) }
    val fc = remember { PracticeFontController(fontSize, 1.3f, bindings.fontSettingsRepository) }
    LaunchedEffect(Unit) { fc.loadFromStore() }
    var showChatGptDialog by remember { mutableStateOf(false) }
    val chatGptResult = externalState.chatGptResult
    val chatGptLoading = externalState.chatGptLoading
    val autoAdvance = remember { PracticeAutoAdvanceController() }
    val overlayNav = rememberPracticeOverlayNavigation(autoAdvance, currentIndex, question?.id, bindings, sendCommand)
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
        if (showList) bindings.buildAnswerCardDisplayInfo(questions) else emptyMap()
    }
    val answerCardEntryGrouped = remember(showList, questions) {
        if (showList) bindings.answerCardEntryGrouped(questions) else false
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
                totalCount = bindings.totalCount,
                sessionScore = sessionScore,
                realUnanswered = bindings.totalCount - bindings.answeredCount
            )
        ) {
            PracticeSessionExitPipeline.Action.ReviewBack -> onReviewBack()
            PracticeSessionExitPipeline.Action.ExitWithoutAnswer -> onExitWithoutAnswer()
            is PracticeSessionExitPipeline.Action.FinishDirect -> {
                if (sessionAnsweredCount > 0) {
                    sendCommand(
                        SessionCommand.AddHistoryRecord(sessionScore, bindings.totalCount, action.realUnanswered)
                    )
                }
                onQuizEnd(
                    action.sessionScore, action.sessionAnsweredCount, action.realUnanswered,
                    bindings.correctCount, bindings.answeredCount
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

        val inAnsweredHistory = bindings.isInAnsweredHistory()
        val postAnswerAdvance: suspend () -> Unit = {
            when (val action = PracticePostAnswerAdvancePipeline.resolve(bindings.hasPendingQuestions())) {
                PracticePostAnswerAdvancePipeline.Action.Advance -> {
                    com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog.postAnswerAdvance(
                        "Advance", currentIndex
                    )
                    sendCommand(SessionCommand.NextQuestion)
                }
                PracticePostAnswerAdvancePipeline.Action.FinishOrPromptExit -> {
                    com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog.postAnswerAdvance(
                        "FinishOrPromptExit", currentIndex
                    )
                    if (sessionAnsweredCount >= bindings.totalCount) {
                        sendCommand(
                            SessionCommand.AddHistoryRecord(
                                sessionScore, bindings.totalCount, bindings.totalCount - bindings.answeredCount
                            )
                        )
                        onQuizEnd(sessionScore, sessionAnsweredCount, bindings.totalCount - bindings.answeredCount,
                            bindings.correctCount, bindings.answeredCount)
                    } else showExitDialog = true
                }
            }
        }

        QuestionSessionChromeLayout(
            scrollState = mainScrollState,
            onScrollInProgress = { if (it) autoAdvance.cancel() },
            modifier = contentModifier.fillMaxSize().padding(AppSpacing.lg)
                .cancelAutoAdvanceOnTouch { autoAdvance.cancel() }
                .practiceHistorySwipe(
                    currentIndex, showResult, inAnsweredHistory, dispatchCommand,
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
                        if (persistentQuestionActionsEnabled) overlayNav {
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
                    onToggleFavorite = externalState.onToggleFavorite,
                    onOpenTypography = { showTypographySheet = true },
                    onEditQuestion = {
                        if (persistentQuestionActionsEnabled) {
                            sendCommand(SessionCommand.ClearEditableQuestion)
                            sendCommand(SessionCommand.PrepareEditableQuestion(question.id))
                            showEditQuestionDialog = true
                        }
                    },
                    settingsLabel = stringResource(R.string.settings),
                    settingsMenuExpanded = menuExpanded,
                    onMenuToggle = { menuExpanded = true },
                    onMenuDismiss = { menuExpanded = false },
                    hasAnyAnalysis = hasDeepSeekAnalysis || hasSparkAnalysis || hasBaiduAnalysis,
                    questionActionsEnabled = persistentQuestionActionsEnabled,
                    aiActionsEnabled = persistentQuestionActionsEnabled,
                )
            },
            scrollContent = {
                PracticeScreenQuestionScrollContent(
                    questions, question, currentIndex, isReviewMode, textAnswer, showResult, resultDisplayReady,
                    selectedOption, displayOptions, resolvedFillAnswer, correctIndices, fc, questionCopyText,
                    analysisText, sparkText, baiduText, noteList, bindings, autoAdvance,
                    onOpenQuestionList = { showList = true },
                    onRetryCurrent = {
                        setAnsweredThisSession(false)
                        sendCommand(SessionCommand.RetryCurrentQuestion(currentIndex))
                    },
                    onRetryWrongBlanks = {
                        setAnsweredThisSession(false)
                        sendCommand(SessionCommand.RetryWrongBlanks(currentIndex))
                    },
                    onViewExplanation = { overlayNav { onViewExplanation(it) } },
                    onEditCorrectAnswer = { text, id, idx ->
                        if (persistentQuestionActionsEnabled) overlayNav { onEditCorrectAnswer(text, id, idx) }
                    },
                    onEditNote = { note, id, idx ->
                        if (persistentQuestionActionsEnabled) overlayNav { onEditNote(note, id, idx) }
                    },
                    onViewDeepSeek = { text, id, idx ->
                        if (persistentQuestionActionsEnabled) overlayNav { onViewDeepSeek(text, id, idx) }
                    },
                    onViewSpark = { text, id, idx ->
                        if (persistentQuestionActionsEnabled) overlayNav { onViewSpark(text, id, idx) }
                    },
                    onViewBaidu = { text, id, idx ->
                        if (persistentQuestionActionsEnabled) overlayNav { onViewBaidu(text, id, idx) }
                    },
                    onDeleteExplanation = {
                        if (persistentQuestionActionsEnabled) showDeleteExplanationDialog = true
                    },
                    onDeleteNote = { if (persistentQuestionActionsEnabled) showDeleteNoteDialog = true },
                    onDeleteAnalysis = {
                        if (persistentQuestionActionsEnabled) {
                            deleteTarget = it
                            showDeleteDialog = true
                        }
                    },
                    onTextAnswerChange = { sendCommand(SessionCommand.UpdateTextAnswer(it)) },
                    onOptionClick = { idx ->
                        PracticeScreenOptionSubmitHandlers.onOptionClick(
                            question, sendCommand, setAnsweredThisSession, idx
                        )
                    },
                    onSubmitOption = { idx ->
                        PracticeScreenOptionSubmitHandlers.onSubmitOption(
                            idx, question, currentIndex, textAnswer, resolvedFillAnswer, correctIndices,
                            true, correctDelay, wrongDelay, soundEnabled,
                            externalState.playCorrect, externalState.playWrong,
                            bindings, sendCommand, setAnsweredThisSession, autoAdvance, coroutineScope,
                            postAnswerAdvance, externalState.onWrongAnswer, onSubmit
                        )
                    }
                )
            },
            bottomBar = {
                PracticeScreenBottomBar(
                    isReviewMode, showResult, inAnsweredHistory, answeredThisSession, hasSessionInput,
                    currentIndex, question, textAnswer, selectedOption, resolvedFillAnswer, correctIndices,
                    correctDelay, wrongDelay, soundEnabled, bindings, dispatchCommand, sendCommand,
                    externalState.playCorrect, externalState.playWrong, externalState.onWrongAnswer, autoAdvance,
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
            sendCommand(SessionCommand.GoToQuestion(it))
        },
        showEditQuestionDialog, editableQuestion, editedQuestionContent, editedQuestionAnswer, editedAnswerParts,
        onConfirmEdit = { newContent, newOptions, finalAnswer ->
            coroutineScope.launch {
                sendCommand(
                    SessionCommand.UpdateQuestionAllFields(
                        currentIndex, newContent, newOptions, finalAnswer, activeQuestion.explanation
                    )
                )
                showEditQuestionDialog = false
                sendCommand(SessionCommand.ClearEditableQuestion)
            }
        },
        onDismissEdit = { showEditQuestionDialog = false; sendCommand(SessionCommand.ClearEditableQuestion) },
        showExplanationFull, { showExplanationFull = false }, activeQuestion,
        showDeleteExplanationDialog, { showDeleteExplanationDialog = false },
        { sendCommand(SessionCommand.ClearExplanation(currentIndex, activeQuestion)); showDeleteExplanationDialog = false },
        showDeleteNoteDialog, { showDeleteNoteDialog = false },
        { sendCommand(SessionCommand.SaveNote(activeQuestion.id, currentIndex, "")); showDeleteNoteDialog = false },
        showDeleteDialog, deleteTarget, { showDeleteDialog = false; deleteTarget = "" },
        {
            when (deleteTarget) {
                "deepseek" -> { externalState.onClearDeepSeek(); sendCommand(SessionCommand.UpdateAnalysis(currentIndex, "")) }
                "spark" -> { externalState.onClearSpark(); sendCommand(SessionCommand.UpdateSparkAnalysis(currentIndex, "")) }
                "baidu" -> { externalState.onClearBaidu(); sendCommand(SessionCommand.UpdateBaiduAnalysis(currentIndex, "")) }
            }
            showDeleteDialog = false; deleteTarget = ""
        },
        showExitDialog, sessionInputCount, bindings.totalCount, { showExitDialog = false },
        autoAdvance, bindings, sendCommand, onQuizEnd,
        showChatGptDialog, { showChatGptDialog = false }, chatGptLoading,
        chatGptResult,
        { sendCommand(SessionCommand.UpdateAnalysis(currentIndex, it)) }
    )
}
