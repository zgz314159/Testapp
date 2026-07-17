package com.example.testapp.presentation.screen.exam

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.testapp.core.util.SessionAnalysisResolvePipeline
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.feature.exam.R
import com.example.testapp.presentation.screen.exam.components.ExamAnalysisArea
import com.example.testapp.presentation.screen.exam.components.ExamHeader
import com.example.testapp.presentation.screen.exam.components.ExamQuestionBody
import com.example.testapp.presentation.screen.exam.components.ExamScreenBottomBar
import com.example.testapp.presentation.screen.exam.components.ExamScreenOverlays
import com.example.testapp.presentation.screen.exam.components.ExamScreenQuizInitEffect
import com.example.testapp.presentation.screen.exam.components.ExamScreenReviewInitEffect
import com.example.testapp.presentation.screen.exam.components.ExamScreenSaveSuccessEffect
import com.example.testapp.presentation.screen.exam.components.ExamScreenScrollCancelEffects
import com.example.testapp.presentation.screen.exam.components.examScreenGesture
import com.example.testapp.presentation.session.exam.ExamCommandOutcome
import com.example.testapp.presentation.session.exam.ExamScreenBindings
import com.example.testapp.uicommon.component.FillAnswerRoundLabel
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.component.cancelAutoAdvanceOnTouch
import com.example.testapp.uicommon.design.AppCard
import com.example.testapp.uicommon.design.AppEmptyState
import com.example.testapp.uicommon.design.AppLoadingContent
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.PracticeExamTopBar
import com.example.testapp.uicommon.design.QuestionSessionActionRow
import com.example.testapp.uicommon.design.QuestionSessionChromeLayout
import com.example.testapp.uicommon.design.QuestionSessionSideAction
import com.example.testapp.uicommon.layout.ScreenSafeScaffold
import com.example.testapp.uicommon.util.formatQuestionForAi
import com.example.testapp.uicommon.util.formatQuestionForCopy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ExternalExamState(
    val examCount: Int = 0,
    val randomExam: Boolean = false,
    val fillConfigVersion: String = "",
    val examMemoryMode: Boolean = false,
    val examMemoryBatchSize: Int = 0,
    val examMemoryWrongMode: Int = 0,
    val examMemoryPoolMode: Int = 0,
    val fontSize: Float = 16f,
    val examDelay: Long = 0L,
    val questionFontSize: Float = 16f,
    val questionLineSpacing: Float = 1.3f,
    val isFavorite: Boolean = false,
    val onToggleFavorite: () -> Unit = {},
    val analysisPair: Pair<Int, String?>? = null,
    val sparkPair: Pair<Int, String?>? = null,
    val chatGptResult: Pair<Int, String?>? = null,
    val resolveLocalized: (com.example.testapp.core.common.LocalizedResult?) -> String = { "" }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreenContent(
    quizId: String,
    bindings: ExamScreenBindings,
    dispatchCommand: (SessionCommand) -> ExamCommandOutcome?,
    sessionHosted: Boolean = false,
    externalState: ExternalExamState = ExternalExamState(),
    isReviewMode: Boolean = false,
    reviewProgressId: String? = null,
    isWrongBookMode: Boolean = false,
    isFavoriteMode: Boolean = false,
    onReviewBack: () -> Unit = {},
    onExamEnd: (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?, cumulativeExamCount: Int?) -> Unit = { _, _, _, _, _, _ -> },
    onExitWithoutAnswer: () -> Unit = {},
    onViewDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewExplanation: (String) -> Unit = {},
    onEditCorrectAnswer: (String, Int, Int) -> Unit = { _, _, _ -> },
    onEditNote: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    val questions by bindings.questions.collectAsState()
    val currentIndex by bindings.currentIndex.collectAsState()
    val selectedOptions by bindings.selectedOptions.collectAsState()
    val textAnswers by bindings.textAnswers.collectAsState()
    val progressLoaded by bindings.progressLoaded.collectAsState()
    val emptyQuestionResult by bindings.emptyQuestionResult.collectAsState()
    val showResultList by bindings.showResultList.collectAsState()
    val answerTimeList by bindings.answerTimeList.collectAsState()
    val cumulativeCorrect by bindings.cumulativeCorrect.collectAsState()
    val cumulativeAnswered by bindings.cumulativeAnswered.collectAsState()
    val cumulativeExamCount by bindings.cumulativeExamCount.collectAsState()
    val editableQuestion by bindings.editableQuestion.collectAsState()
    val noteList by bindings.noteList.collectAsState()
    val analysisList by bindings.analysisList.collectAsState()
    val sparkAnalysisList by bindings.sparkAnalysisList.collectAsState()
    val baiduAnalysisList by bindings.baiduAnalysisList.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val question = questions.getOrNull(currentIndex)
    val coroutineScope = rememberCoroutineScope()
    val sendCommand: (SessionCommand) -> Unit = { dispatchCommand(it) }

    ExamScreenReviewInitEffect(
        isReviewMode, reviewProgressId, quizId, isWrongBookMode, isFavoriteMode, externalState, sendCommand, sessionHosted,
    )
    ExamScreenQuizInitEffect(quizId, isReviewMode, externalState, progressLoaded, sendCommand, sessionHosted)

    val parsingText = stringResource(R.string.parsing)
    val parsingKeyword = parsingText.removeSuffix("...")
    val analysisTextRaw = SessionAnalysisResolvePipeline.resolve(
        currentIndex = currentIndex,
        streamingPair = externalState.analysisPair,
        sessionValue = analysisList.getOrNull(currentIndex),
        listValue = analysisList.getOrNull(currentIndex),
        parsingKeyword = parsingKeyword
    ).orEmpty()
    val analysisText = com.example.testapp.presentation.screen.shared.SessionDeepSeekAnalysisTextPipeline
        .toDisplayText(analysisTextRaw, question?.content.orEmpty())
        .ifBlank { analysisTextRaw }
    val hasDeepSeekAnalysis = analysisText.isNotBlank()
    val sparkText = SessionAnalysisResolvePipeline.resolve(
        currentIndex = currentIndex,
        streamingPair = externalState.sparkPair,
        sessionValue = sparkAnalysisList.getOrNull(currentIndex),
        listValue = sparkAnalysisList.getOrNull(currentIndex),
        parsingKeyword = parsingKeyword
    ).orEmpty()
    val hasSparkAnalysis = sparkText.isNotBlank()
    val baiduText = SessionAnalysisResolvePipeline.resolve(
        currentIndex = currentIndex,
        streamingPair = externalState.chatGptResult,
        sessionValue = baiduAnalysisList.getOrNull(currentIndex),
        listValue = baiduAnalysisList.getOrNull(currentIndex),
        parsingKeyword = parsingKeyword
    ).orEmpty()
    val hasBaiduAnalysis = baiduText.isNotBlank()
    val hasNote = noteList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val questionTextForAi = remember(question) { question?.let(::formatQuestionForAi).orEmpty() }
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); elapsed += 1 } }

    val ds = remember { ExamDialogState() }
    val gesture = remember { ExamGestureNavigator() }
    val timer = remember { ExamAutoAdvanceTimer() }
    val lifecycle = (context as LifecycleOwner).lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> timer.resume()
                Lifecycle.Event.ON_PAUSE -> timer.cancel()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val stats = remember { ExamSessionStats() }
    val endFlow = remember { ExamEndFlow() }
    val fc = remember { ExamFontController(externalState.fontSize, 1.3f, bindings.fontSettingsRepository) }
    LaunchedEffect(Unit) { fc.loadFromStore() }
    ExamScreenSaveSuccessEffect(bindings) {
        ds.showEditQuestionDialog = false
        sendCommand(SessionCommand.ClearEditableQuestion)
    }

    val explanationScroll = rememberScrollState()
    val noteScroll = rememberScrollState()
    val deepSeekScroll = rememberScrollState()
    val sparkScroll = rememberScrollState()
    val baiduScroll = rememberScrollState()
    val mainScrollState = rememberScrollState()
    ExamScreenScrollCancelEffects(timer, explanationScroll, noteScroll, deepSeekScroll, sparkScroll, baiduScroll)
    LaunchedEffect(currentIndex) { ds.expandedSection = -1 }

    val answerCardDisplayInfo = remember(ds.showList, questions) {
        if (ds.showList) bindings.buildAnswerCardDisplayInfo(questions) else emptyMap()
    }
    val answerCardEntryGrouped = remember(ds.showList, questions) {
        if (ds.showList) bindings.answerCardEntryGrouped(questions) else false
    }

    var answeredThisSession by remember { mutableStateOf(false) }
    var initialAnsweredCount by remember { mutableStateOf(0) }
    LaunchedEffect(progressLoaded) {
        if (progressLoaded) {
            answeredThisSession = false
            endFlow.graded.value = false
            initialAnsweredCount = selectedOptions.count { it.isNotEmpty() }
        }
    }

    val sessionActualAnswered = remember(selectedOptions, initialAnsweredCount) { stats.actualAnswered(selectedOptions, initialAnsweredCount) }
    val sessionScore = remember(selectedOptions, questions, initialAnsweredCount) { stats.score(selectedOptions, questions, initialAnsweredCount) }
    val sessionUnanswered = stats.unanswered(questions.size, selectedOptions)

    fun autoAdvance(afterDelayMs: Long, onComplete: suspend () -> Unit) {
        timer.schedule(coroutineScope, afterDelayMs) { onComplete() }
    }

    fun requestSubmitExam() {
        timer.cancel()
        focusManager.clearFocus(force = true)
        when (ExamSubmitFlow.resolve(answeredThisSession)) {
            ExamSubmitFlow.Action.ExitWithoutAnswer -> onExitWithoutAnswer()
            ExamSubmitFlow.Action.ShowSubmitDialog -> ds.showExitDialog = true
        }
    }

    val postAnswerAdvance: suspend () -> Unit = {
        when (ExamPostAnswerAdvancePipeline.resolve(bindings.hasPendingQuestions())) {
            ExamPostAnswerAdvancePipeline.Action.Advance -> dispatchCommand(SessionCommand.NextQuestion)
            ExamPostAnswerAdvancePipeline.Action.PromptSubmit -> ds.showExitDialog = true
        }
    }

    val sessionExitLabel = if (isReviewMode) stringResource(R.string.review_back)
    else stringResource(R.string.exam_exit_session)
    val requestSessionExit: () -> Unit = {
        when (
            ExamSessionExitPipeline.resolve(
                isReviewMode = isReviewMode,
                answeredThisSession = answeredThisSession,
                hasPendingQuestions = bindings.hasPendingQuestions()
            )
        ) {
            ExamSessionExitPipeline.Action.ReviewBack -> onReviewBack()
            ExamSessionExitPipeline.Action.ExitWithoutAnswer -> onExitWithoutAnswer()
            ExamSessionExitPipeline.Action.ShowSubmitDialog -> ds.showExitDialog = true
            ExamSessionExitPipeline.Action.FinishDirect ->
                coroutineScope.launch {
                    val score = suspendExamCommand(bindings, SessionCommand.GradeSession) ?: return@launch
                    if (score < 0) return@launch
                    onExamEnd(sessionScore, sessionActualAnswered, 0, cumulativeCorrect, cumulativeAnswered, cumulativeExamCount)
                }
        }
    }
    BackHandler(onBack = requestSessionExit)

    ScreenSafeScaffold { contentModifier ->
        if (!progressLoaded) {
            AppLoadingContent(modifier = contentModifier, message = stringResource(R.string.no_questions_loading))
            return@ScreenSafeScaffold
        }
        if (question == null) {
            AppEmptyState(
                message = externalState.resolveLocalized(emptyQuestionResult).ifBlank { stringResource(R.string.no_questions) },
                modifier = contentModifier
            )
            return@ScreenSafeScaffold
        }

        val selectedOption = selectedOptions.getOrElse(currentIndex) { emptyList() }
        val textAnswer = textAnswers.getOrElse(currentIndex) { "" }
        val showResult = showResultList.getOrNull(currentIndex) ?: false
        val questionCopyText = remember(question) { formatQuestionForCopy(question) }

        QuestionSessionChromeLayout(
            scrollState = mainScrollState,
            onScrollInProgress = {
                if (it) {
                    ds.expandedSection = -1
                    timer.cancel()
                }
            },
            modifier = contentModifier.fillMaxSize().padding(AppSpacing.lg)
                .onSizeChanged { gesture.containerWidth = it.width.toFloat() }
                .cancelAutoAdvanceOnTouch { timer.cancel() }
                .examScreenGesture(
                    currentIndex, gesture, timer, isReviewMode, answeredThisSession, bindings,
                    dispatchCommand,
                    { focusManager.clearFocus(force = true) }, context,
                    stringResource(R.string.answered_history_at_oldest),
                    stringResource(R.string.answered_history_at_latest),
                    onExitWithoutAnswer, { ds.showExitDialog = true }
                ),
            topBar = {
                PracticeExamTopBar(
                    elapsedSeconds = elapsed,
                    onRequestExit = requestSessionExit,
                    exitContentDescription = sessionExitLabel,
                    isFavorite = externalState.isFavorite,
                    favoriteAddLabel = stringResource(R.string.favorite_add),
                    favoriteRemoveLabel = stringResource(R.string.favorite_remove),
                    notesLabel = stringResource(R.string.notes),
                    onEditNote = {
                        if (showResult) {
                            timer.pause()
                            onEditNote(noteList.getOrNull(currentIndex)?.takeIf { it.isNotBlank() } ?: " ", question.id, currentIndex)
                        }
                    },
                    hasNote = hasNote,
                    aiParseLabel = stringResource(R.string.ai_parse),
                    deepSeekLabel = stringResource(R.string.ai_name_deepseek),
                    sparkLabel = stringResource(R.string.ai_name_spark),
                    aiMenuExpanded = ds.aiMenuExpanded,
                    onAiMenuToggle = { if (showResult) ds.aiMenuExpanded = true },
                    onAiMenuDismiss = { ds.aiMenuExpanded = false },
                    onOpenAiMenu = { timer.pause(); onAskDeepSeek(questionTextForAi, question.id, currentIndex) },
                    onOpenAskMenu = { timer.cancel(); onAskSpark(questionTextForAi, question.id, currentIndex) },
                    onToggleFavorite = externalState.onToggleFavorite,
                    onOpenTypography = { ds.showTypographySheet = true },
                    onEditQuestion = {
                        timer.cancel()
                        focusManager.clearFocus(force = true)
                        sendCommand(SessionCommand.ClearEditableQuestion)
                        sendCommand(SessionCommand.PrepareEditableAtIndex(currentIndex))
                        ds.showEditQuestionDialog = true
                    },
                    settingsLabel = stringResource(R.string.settings),
                    settingsMenuExpanded = ds.menuExpanded,
                    onMenuToggle = { ds.menuExpanded = true },
                    onMenuDismiss = { ds.menuExpanded = false },
                    hasAnyAnalysis = hasDeepSeekAnalysis || hasSparkAnalysis || hasBaiduAnalysis
                )
            },
            scrollContent = {
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                ExamHeader(
                    questionType = stringResource(R.string.question_type_prefix) + localizedQuestionTypeLabel(question.type),
                    currentIndex = currentIndex,
                    total = questions.size,
                    questionListLabel = stringResource(R.string.total_questions, questions.size),
                    onOpenQuestionList = { ds.showList = true }
                ) {
                    FillAnswerRoundLabel(
                        questionId = question.id,
                        sessionQuestionIds = questions.map { it.id },
                        modifier = Modifier.padding(bottom = AppSpacing.sm)
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
                }
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                AppCard {
                    ExamQuestionBody(
                        question = question,
                        questionFontSize = fc.questionFontSize,
                        lineSpacingMultiplier = fc.questionLineSpacing,
                        letterSpacing = fc.questionLetterSpacing,
                        selectedOption = selectedOption,
                        textAnswer = textAnswer,
                        showResult = showResult,
                        onOptionClick = if (isReviewMode) { {} } else { { idx ->
                            val isSingleOrJudge =
                                QuestionTypes.isSingle(question.type) || QuestionTypes.isJudge(question.type)
                            answeredThisSession = true
                            sendCommand(SessionCommand.SelectOptionWithSkip(idx, isSingleOrJudge))
                            if (isSingleOrJudge) autoAdvance(externalState.examDelay * 1000L) { postAnswerAdvance() }
                        } },
                        onTextAnswerChange = if (isReviewMode) {
                            { }
                        } else {
                            { text ->
                                answeredThisSession = true
                                sendCommand(SessionCommand.UpdateTextAnswer(text))
                            }
                        }
                    )
                }
                val retryCurrentLabel = stringResource(R.string.retry_current_question)
                val retryWrongLabel = stringResource(R.string.retry_wrong_blanks)
                QuestionSessionActionRow(
                    questionCopyText = questionCopyText,
                    leadingAction = if (!isReviewMode && showResult) {
                        QuestionSessionSideAction(
                            contentDescription = retryCurrentLabel,
                            onClick = {
                                timer.cancel()
                                sendCommand(SessionCommand.RetryCurrentQuestion(currentIndex))
                                answeredThisSession = true
                            }
                        )
                    } else null,
                    trailingAction = if (!isReviewMode && showResult && QuestionTypes.isInlineBlank(question.type)) {
                        QuestionSessionSideAction(
                            contentDescription = retryWrongLabel,
                            onClick = {
                                timer.cancel()
                                sendCommand(SessionCommand.RetryWrongBlanks(currentIndex))
                                answeredThisSession = true
                            }
                        )
                    } else null
                )
                ExamAnalysisArea(
                    question = question, currentIndex = currentIndex, selectedOption = selectedOption, textAnswer = textAnswer,
                    questionFontSize = fc.questionFontSize, showResult = showResult,
                    questionTextForAi = questionTextForAi,
                    expandedSection = ds.expandedSection,
                    onToggleSection = { section -> timer.cancel(); ds.expandedSection = section },
                    explanationScroll = explanationScroll, noteScroll = noteScroll, deepSeekScroll = deepSeekScroll,
                    sparkScroll = sparkScroll, baiduScroll = baiduScroll, noteList = noteList,
                    analysisText = analysisText, sparkText = sparkText, baiduText = baiduText,
                    onEditNote = { note, qId, idx -> timer.pause(); onEditNote(note, qId, idx) },
                    onViewDeepSeek = { text, qId, idx -> timer.pause(); onViewDeepSeek(text, qId, idx) },
                    onViewSpark = { text, qId, idx -> timer.pause(); onViewSpark(text, qId, idx) },
                    onViewBaidu = { text, qId, idx -> timer.pause(); onViewBaidu(text, qId, idx) },
                    onViewExplanation = onViewExplanation,
                    onEditCorrectAnswer = { text, qId, idx -> timer.pause(); onEditCorrectAnswer(text, qId, idx) },
                    onShowDeleteNoteDialog = { timer.cancel(); ds.showDeleteNoteDialog = true },
                    onSetDeleteTargetAndShow = { target ->
                        timer.cancel()
                        ds.deleteTarget = target
                        ds.showDeleteDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(AppSpacing.lg))
            },
            bottomBar = {
                ExamScreenBottomBar(
                    isReviewMode, showResult, selectedOption, bindings, sendCommand,
                    { answeredThisSession = true }, ::requestSubmitExam,
                )
            }
        )
    }

    val activeQuestion = question ?: return
    ExamScreenOverlays(
        ds, fc, coroutineScope, editableQuestion, questions, selectedOptions, textAnswers, showResultList,
        answerTimeList, answerCardDisplayInfo, answerCardEntryGrouped, currentIndex, bindings, sendCommand, activeQuestion,
        sessionScore, sessionActualAnswered, sessionUnanswered, cumulativeCorrect, cumulativeAnswered,
        cumulativeExamCount, onExamEnd
    )
}
