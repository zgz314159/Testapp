package com.example.testapp.presentation.screen.exam

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.testapp.uicommon.component.cancelAutoAdvanceOnTouch
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.util.extractDerivedFillQuestionRound
import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.feature.exam.R
import com.example.testapp.presentation.screen.exam.components.*
import com.example.testapp.uicommon.component.QuestionEditDialog
import com.example.testapp.uicommon.component.QuestionNavigationControls
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.util.formatQuestionForCopy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data class of ViewModel-derived state that originates from :app's ViewModels.
 */
data class ExternalExamState(
    val examCount: Int = 0,
    val randomExam: Boolean = false,
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
    val resolveLocalized: (LocalizedResult?) -> String = { "" }
)

@Composable
fun ExamScreenContent(
    quizId: String,
    viewModel: ExamViewModel,
    externalState: ExternalExamState = ExternalExamState(),
    onExamEnd: (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?, cumulativeExamCount: Int?) -> Unit = { _, _, _, _, _, _ -> },
    onExitWithoutAnswer: () -> Unit = {},
    onViewDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskDeepSeek: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskSpark: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onAskBaidu: (String, Int, Int) -> Unit = { _, _, _ -> },
    onViewExplanation: (String) -> Unit = {},
    onEditNote: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val textAnswers by viewModel.textAnswers.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val emptyQuestionResult by viewModel.emptyQuestionResult.collectAsState()
    val showResultList by viewModel.showResultList.collectAsState()
    val finished by viewModel.finished.collectAsState()
    val cumulativeCorrect by viewModel.cumulativeCorrect.collectAsState()
    val cumulativeAnswered by viewModel.cumulativeAnswered.collectAsState()
    val cumulativeExamCount by viewModel.cumulativeExamCount.collectAsState()
    val editableQuestion by viewModel.editableQuestion.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val question = questions.getOrNull(currentIndex)
    val coroutineScope = rememberCoroutineScope()
    val noteList by viewModel.noteList.collectAsState()
    val analysisList by viewModel.analysisList.collectAsState()
    val sparkAnalysisList by viewModel.sparkAnalysisList.collectAsState()
    val baiduAnalysisList by viewModel.baiduAnalysisList.collectAsState()

    val analysisText = if (externalState.analysisPair?.first == currentIndex) externalState.analysisPair?.second.orEmpty() else analysisList.getOrNull(currentIndex)
    val hasDeepSeekAnalysis = analysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val sparkText = if (externalState.sparkPair?.first == currentIndex) externalState.sparkPair?.second.orEmpty() else sparkAnalysisList.getOrNull(currentIndex)
    val hasSparkAnalysis = sparkAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val baiduText = if (externalState.chatGptResult?.first == currentIndex) externalState.chatGptResult?.second.orEmpty() else baiduAnalysisList.getOrNull(currentIndex)
    val hasBaiduAnalysis = baiduAnalysisList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val parsingText = stringResource(R.string.parsing)
    val hasNote = noteList.getOrNull(currentIndex).orEmpty().isNotBlank()
    val questionTextForAi = remember(question) { question?.let(::formatQuestionForCopy).orEmpty() }
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(1000); elapsed += 1 } }

    LaunchedEffect(quizId, externalState.examCount, externalState.randomExam, progressLoaded) {
        if (!progressLoaded) {
            viewModel.setRandomExam(externalState.randomExam)
            viewModel.setMemoryModeConfig(
                enabled = externalState.examMemoryMode,
                batchSize = externalState.examMemoryBatchSize,
                wrongMode = externalState.examMemoryWrongMode,
                poolMode = externalState.examMemoryPoolMode
            )
            viewModel.loadQuestions(quizId, externalState.examCount, externalState.randomExam)
        }
    }

    val ds = remember { ExamDialogState() }
    val gesture = remember { ExamGestureNavigator() }
    val timer = remember { ExamAutoAdvanceTimer() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
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
    val fc = remember { ExamFontController(externalState.fontSize, 1.3f, viewModel.fontSettingsRepository) }
    val saveSuccessText = stringResource(R.string.save_success)

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            Toast.makeText(context, saveSuccessText, Toast.LENGTH_SHORT).show()
            ds.showEditQuestionDialog = false; viewModel.clearEditableQuestion()
        }
    }

    // Scroll states
    val explanationScroll = rememberScrollState()
    val noteScroll = rememberScrollState()
    val deepSeekScroll = rememberScrollState()
    val sparkScroll = rememberScrollState()
    val baiduScroll = rememberScrollState()
    val mainScrollState = rememberScrollState()

    LaunchedEffect(currentIndex) { ds.expandedSection = -1 }
    LaunchedEffect(mainScrollState.isScrollInProgress) {
        if (mainScrollState.isScrollInProgress) { ds.expandedSection = -1; timer.cancel() }
    }
    listOf(explanationScroll, noteScroll, deepSeekScroll, sparkScroll, baiduScroll).forEach { scroll ->
        LaunchedEffect(scroll.isScrollInProgress) {
            if (scroll.isScrollInProgress) timer.cancel()
        }
    }

    var answeredThisSession by remember { mutableStateOf(false) }
    var initialAnsweredCount by remember { mutableStateOf(0) }

    val sessionActualAnswered = remember(selectedOptions, initialAnsweredCount) { stats.actualAnswered(selectedOptions, initialAnsweredCount) }
    val sessionScore = remember(selectedOptions, questions, initialAnsweredCount) { stats.score(selectedOptions, questions, initialAnsweredCount) }
    val sessionUnanswered = stats.unanswered(questions.size, selectedOptions)

    LaunchedEffect(progressLoaded) {
        if (progressLoaded) {
            answeredThisSession = false; endFlow.graded.value = false
            initialAnsweredCount = selectedOptions.count { it.isNotEmpty() }
        }
    }

    fun autoAdvance(afterDelayMs: Long, onComplete: suspend () -> Unit) {
        timer.schedule(coroutineScope, afterDelayMs) { onComplete() }
    }

    BackHandler {
        when {
            !answeredThisSession -> onExitWithoutAnswer()
            selectedOptions.count { it.isNotEmpty() } >= questions.size ->
                coroutineScope.launch {
                    viewModel.gradeExam()
                    onExamEnd(sessionScore, sessionActualAnswered, 0, cumulativeCorrect, cumulativeAnswered, cumulativeExamCount)
                }
            else -> ds.showExitDialog = true
        }
    }

    if (!progressLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_questions_loading), fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
        }
        return
    }
    if (question == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(externalState.resolveLocalized(emptyQuestionResult).ifBlank { stringResource(R.string.no_questions) },
                fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
        }
        return
    }

    val selectedOption = selectedOptions.getOrElse(currentIndex) { emptyList<Int>() }
    val textAnswer = textAnswers.getOrElse(currentIndex) { "" }
    val showResult = showResultList.getOrNull(currentIndex) ?: false
    val questionCopyText = remember(question) { formatQuestionForCopy(question) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(mainScrollState).padding(16.dp)
            .onSizeChanged { gesture.containerWidth = it.width.toFloat() }
            .cancelAutoAdvanceOnTouch { timer.cancel() }
            .pointerInput(currentIndex, gesture.containerWidth) {
                detectHorizontalDragGestures(
                    onDragStart = { offset -> gesture.dragStartX = offset.x; timer.cancel() },
                    onHorizontalDrag = { _, amount -> gesture.dragAmount += amount },
                    onDragEnd = {
                        if ((gesture.dragStartX < 20f && gesture.dragAmount > 100f) || (gesture.dragStartX > gesture.containerWidth - 20f && gesture.dragAmount < -100f)) {
                            focusManager.clearFocus(force = true)
                            when { !answeredThisSession -> onExitWithoutAnswer(); else -> ds.showExitDialog = true }
                        } else {
                            if (gesture.dragAmount > 100f && currentIndex > 0) { focusManager.clearFocus(force = true); viewModel.prevQuestion() }
                            else if (gesture.dragAmount < -100f && currentIndex < questions.size - 1) { focusManager.clearFocus(force = true); viewModel.nextQuestion() }
                            else if (gesture.dragAmount < -100f) {
                                focusManager.clearFocus(force = true)
                                when { !answeredThisSession -> onExitWithoutAnswer(); else -> ds.showExitDialog = true }
                            }
                        }
                        gesture.resetDrag()
                    },
                    onDragCancel = { gesture.resetDrag() }
                )
            }
    ) {
        ExamTopBar(
            elapsed = elapsed, isFavorite = externalState.isFavorite,
            onToggleFavorite = externalState.onToggleFavorite,
            aiMenuExpanded = ds.aiMenuExpanded, onAiMenuToggle = { if (showResult) ds.aiMenuExpanded = true }, onAiMenuDismiss = { ds.aiMenuExpanded = false },
            onOpenAiMenu = { timer.pause(); onAskDeepSeek(questionTextForAi, question.id, currentIndex) },
            onOpenAskMenu = { timer.cancel(); onAskSpark(questionTextForAi, question.id, currentIndex) },
            onEditNote = { if (showResult) { timer.pause(); val note = noteList.getOrNull(currentIndex)?.takeIf { it.isNotBlank() } ?: " "; onEditNote(note, question.id, currentIndex) } },
            onShowList = { ds.showList = true },
            settingsMenuExpanded = ds.menuExpanded, onMenuToggle = { ds.menuExpanded = true }, onMenuDismiss = { ds.menuExpanded = false },
            questionsSize = questions.size, hasAnyAnalysis = hasDeepSeekAnalysis || hasSparkAnalysis || hasBaiduAnalysis, hasNote = hasNote,
            settingsMenuContent = {
                DropdownMenuItem(text = { Text(stringResource(R.string.increase_font)) }, onClick = { fc.increaseFont(coroutineScope); ds.menuExpanded = false })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_font)) }, onClick = { fc.decreaseFont(coroutineScope); ds.menuExpanded = false })
                DropdownMenuItem(text = { Text(stringResource(R.string.increase_line_spacing)) }, onClick = { fc.increaseSpacing(coroutineScope); ds.menuExpanded = false })
                DropdownMenuItem(text = { Text(stringResource(R.string.decrease_line_spacing)) }, onClick = { fc.decreaseSpacing(coroutineScope); ds.menuExpanded = false })
                DropdownMenuItem(text = { Text(stringResource(R.string.edit_current_question)) }, onClick = {
                    timer.cancel(); focusManager.clearFocus(force = true); viewModel.clearEditableQuestion()
                    viewModel.prepareEditableQuestion(currentIndex); ds.showEditQuestionDialog = true; ds.menuExpanded = false
                })
            }
        )
        if (ds.showEditQuestionDialog) {
            QuestionEditDialog(
                editableQuestion = editableQuestion,
                initialQuestionContent = "", initialQuestionAnswer = "", initialAnswerParts = listOf(""),
                onConfirm = { newContent, newOptions, finalAnswer -> viewModel.saveEditedQuestion(index = currentIndex, newContent = newContent, newAnswer = finalAnswer, newOptions = newOptions) },
                onDismiss = { ds.showEditQuestionDialog = false; viewModel.clearEditableQuestion() }
            )
        }
        ExamHeader(
            questionType = stringResource(R.string.question_type_prefix) + localizedQuestionTypeLabel(question.type),
            currentIndex = currentIndex, total = questions.size
        )
        if (ds.showList) {
            QuestionListDialog(
                show = ds.showList, onDismiss = { ds.showList = false }, questions = questions,
                selectedOptions = selectedOptions, textAnswers = textAnswers,
                showResultList = showResultList,
                displayInfoByQuestionId = remember(questions) { viewModel.buildAnswerCardDisplayInfo(questions) },
                currentIndex = currentIndex,
                onSelect = { idx -> viewModel.goToQuestion(idx) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        val fillRound = remember(question.id) { extractDerivedFillQuestionRound(question.id) }
        val fillRoundTotal = remember(question.id, questions) {
            if (fillRound == null) null else questions.count { extractSourceQuestionId(it.id) == extractSourceQuestionId(question.id) }
        }
        if (fillRound != null && (fillRoundTotal ?: 0) > 1) {
            Text(text = stringResource(R.string.fill_full_answer_round_template, fillRound, fillRoundTotal ?: 0),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = (LocalFontSize.current.value - 1f).coerceAtLeast(12f).sp, fontFamily = LocalFontFamily.current, color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(bottom = 8.dp))
        }

        ExamQuestionBody(
            question = question, questionFontSize = fc.questionFontSize, lineSpacingMultiplier = fc.questionLineSpacing,
            selectedOption = selectedOption, textAnswer = textAnswer, showResult = showResult,
            onOptionClick = { idx ->
                val isSingleOrJudge = QuestionTypes.isSingle(question.type) || QuestionTypes.isJudge(question.type)
                answeredThisSession = true; viewModel.selectOption(idx, skipAfterChanged = isSingleOrJudge)
                if (isSingleOrJudge) {
                    autoAdvance(externalState.examDelay * 1000L) {
                        if (currentIndex < questions.size - 1) viewModel.nextQuestion()
                        else { viewModel.gradeExam() }
                    }
                }
            },
            onTextAnswerChange = { answeredThisSession = true; viewModel.updateTextAnswer(it) }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = {
                clipboardManager.setText(AnnotatedString(questionCopyText))
                Toast.makeText(context, context.getString(R.string.copy_question_success), Toast.LENGTH_SHORT).show()
            }) {
                Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.copy_question))
            }
        }

        if (showResult && QuestionTypes.isInlineBlank(question.type)) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { timer.cancel(); viewModel.retryWrongFillBlanks(currentIndex); answeredThisSession = true },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = stringResource(R.string.retry_wrong_blanks))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(R.string.retry_wrong_blanks), fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
                }
            }
        }

        ExamAnalysisArea(
            question = question, currentIndex = currentIndex, selectedOption = selectedOption, textAnswer = textAnswer,
            questionFontSize = fc.questionFontSize, showResult = showResult,
            expandedSection = ds.expandedSection, onToggleSection = { timer.cancel(); ds.expandedSection = it },
            explanationScroll = explanationScroll, noteScroll = noteScroll, deepSeekScroll = deepSeekScroll,
            sparkScroll = sparkScroll, baiduScroll = baiduScroll, noteList = noteList,
            analysisText = analysisText, sparkText = sparkText, baiduText = baiduText,
            onEditNote = { note, qId, idx -> timer.pause(); onEditNote(note, qId, idx) },
            onViewDeepSeek = { text, qId, idx -> timer.pause(); onViewDeepSeek(text, qId, idx) },
            onViewSpark = { text, qId, idx -> timer.pause(); onViewSpark(text, qId, idx) },
            onViewBaidu = { text, qId, idx -> timer.pause(); onViewBaidu(text, qId, idx) },
            onViewExplanation = onViewExplanation,
            onShowDeleteNoteDialog = { timer.cancel(); ds.showDeleteNoteDialog = true },
            onSetDeleteTargetAndShow = { target -> timer.cancel(); ds.deleteTarget = target; ds.showDeleteDialog = true }
        )

        QuestionNavigationControls(
            visible = !QuestionTypes.isSingle(question.type) && !QuestionTypes.isJudge(question.type) && !showResult,
            onPrev = { focusManager.clearFocus(force = true); if (selectedOption.isNotEmpty()) answeredThisSession = true; viewModel.prevQuestion() },
            onNext = { focusManager.clearFocus(force = true); if (selectedOption.isNotEmpty()) answeredThisSession = true; viewModel.nextQuestion() },
            enabledPrev = currentIndex > 0,
            enabledNext = currentIndex < questions.size - 1
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    ExamDialogs(
        showDeleteNoteDialog = ds.showDeleteNoteDialog, onDismissDeleteNote = { ds.showDeleteNoteDialog = false },
        onConfirmDeleteNote = { viewModel.saveNote(question.id, currentIndex, ""); ds.showDeleteNoteDialog = false },
        showDeleteDialog = ds.showDeleteDialog, onDismissDelete = { ds.showDeleteDialog = false; ds.deleteTarget = "" },
        onConfirmDelete = {
            when (ds.deleteTarget) {
                "deepseek" -> { viewModel.updateAnalysis(currentIndex, "") }
                "spark" -> { viewModel.updateSparkAnalysis(currentIndex, "") }
                "baidu" -> { viewModel.updateBaiduAnalysis(currentIndex, "") }
            }
            ds.showDeleteDialog = false; ds.deleteTarget = ""
        },
        deleteReadableLabel = when(ds.deleteTarget) {
            "deepseek" -> stringResource(R.string.ai_name_deepseek)
            "spark" -> stringResource(R.string.ai_name_spark)
            "baidu" -> stringResource(R.string.ai_name_baidu)
            else -> ""
        },
        showExitDialog = ds.showExitDialog, onDismissExit = { ds.showExitDialog = false },
        onConfirmExit = {
            coroutineScope.launch {
                viewModel.gradeExam()
                onExamEnd(sessionScore, sessionActualAnswered, sessionUnanswered, cumulativeCorrect, cumulativeAnswered, cumulativeExamCount)
                ds.showExitDialog = false
            }
        },
        exitConfirmText = if (selectedOptions.any { it.isEmpty() }) stringResource(R.string.confirm_submit_unfinished) else stringResource(R.string.confirm_submit),
        showChatGptDialog = ds.showChatGptDialog, onDismissChatGpt = { ds.showChatGptDialog = false },
        onConfirmChatGpt = { ds.showChatGptDialog = false },
        chatGptLoading = false, chatGptResult = null
    )
}
