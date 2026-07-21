package com.example.testapp.presentation.screen.exam.components

import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.feature.exam.R
import com.example.testapp.presentation.screen.ai.AiQuestionEditDialog
import com.example.testapp.presentation.screen.exam.ExamAutoAdvanceTimer
import com.example.testapp.presentation.screen.exam.ExamFontController
import com.example.testapp.presentation.screen.exam.ExternalExamState
import com.example.testapp.presentation.screen.exam.suspendExamCommand
import com.example.testapp.presentation.session.exam.ExamScreenBindings
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.design.QuestionTypographySheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ExamScreenReviewInitEffect(
    isReviewMode: Boolean,
    reviewProgressId: String?,
    quizId: String,
    isWrongBookMode: Boolean,
    isFavoriteMode: Boolean,
    externalState: ExternalExamState,
    sendCommand: (SessionCommand) -> Unit,
    sessionHosted: Boolean = false,
) {
    if (sessionHosted) return
    LaunchedEffect(isReviewMode, reviewProgressId) {
        if (isReviewMode && !reviewProgressId.isNullOrBlank()) {
            sendCommand(
                SessionCommand.EnterExamReviewSession(
                    targetProgressId = reviewProgressId,
                    quizFile = quizId,
                    questionCount = externalState.examCount,
                    random = externalState.randomExam,
                    wrongBook = isWrongBookMode,
                    favorite = isFavoriteMode,
                ),
            )
        }
    }
}

@Composable
fun ExamScreenQuizInitEffect(
    quizId: String,
    isReviewMode: Boolean,
    externalState: ExternalExamState,
    progressLoaded: Boolean,
    sendCommand: (SessionCommand) -> Unit,
    sessionHosted: Boolean = false,
) {
    if (sessionHosted) return
    LaunchedEffect(quizId, externalState.examCount, externalState.randomExam, externalState.fillConfigVersion) {
        if (isReviewMode) return@LaunchedEffect
        sendCommand(SessionCommand.SetRandomExam(externalState.randomExam))
        sendCommand(
            SessionCommand.SetMemoryModeConfig(
                enabled = externalState.examMemoryMode,
                batchSize = externalState.examMemoryBatchSize,
                wrongMode = externalState.examMemoryWrongMode,
                poolMode = externalState.examMemoryPoolMode,
            ),
        )
        if (progressLoaded) {
            sendCommand(SessionCommand.ReloadForFillConfig())
        } else {
            sendCommand(
                SessionCommand.LoadQuestions(
                    quizId,
                    externalState.examCount,
                    externalState.randomExam,
                ),
            )
        }
    }
}

@Composable
fun ExamScreenScrollCancelEffects(
    timer: ExamAutoAdvanceTimer,
    vararg scrollStates: ScrollState
) {
    scrollStates.forEach { scroll ->
        LaunchedEffect(scroll.isScrollInProgress) {
            if (scroll.isScrollInProgress) timer.cancel()
        }
    }
}

@Composable
fun ExamScreenSaveSuccessEffect(bindings: ExamScreenBindings, onSaved: () -> Unit) {
    val context = LocalContext.current
    val saveSuccessText = stringResource(R.string.save_success)
    LaunchedEffect(Unit) {
        bindings.saveSuccess.collect {
            Toast.makeText(context, saveSuccessText, Toast.LENGTH_SHORT).show()
            onSaved()
        }
    }
}

@Composable
fun ExamScreenOverlays(
    ds: com.example.testapp.presentation.screen.exam.ExamDialogState,
    fc: ExamFontController,
    coroutineScope: CoroutineScope,
    editableQuestion: Question?,
    questions: List<Question>,
    selectedOptions: List<List<Int>>,
    textAnswers: List<String>,
    showResultList: List<Boolean>,
    answerTimeList: List<Long>,
    answerCardDisplayInfo: Map<Int, AnswerCardDisplayInfo>,
    answerCardEntryGrouped: Boolean,
    currentIndex: Int,
    bindings: ExamScreenBindings,
    dispatchCommand: (SessionCommand) -> Unit,
    activeQuestion: Question,
    sessionScore: Int,
    sessionActualAnswered: Int,
    sessionUnanswered: Int,
    cumulativeCorrect: Int,
    cumulativeAnswered: Int,
    cumulativeExamCount: Int,
    onExamEnd: (Int, Int, Int, Int?, Int?, Int?) -> Unit
) {
    QuestionTypographySheet(
        visible = ds.showTypographySheet,
        fontSize = fc.questionFontSize,
        lineSpacing = fc.questionLineSpacing,
        letterSpacing = fc.questionLetterSpacing,
        onFontSizeChange = { fc.applyFontSize(it, coroutineScope) },
        onLineSpacingChange = { fc.applyLineSpacing(it, coroutineScope) },
        onLetterSpacingChange = { fc.applyLetterSpacing(it, coroutineScope) },
        onDismiss = { ds.showTypographySheet = false }
    )
    if (ds.showEditQuestionDialog) {
        AiQuestionEditDialog(
            editableQuestion = editableQuestion,
            initialQuestionContent = "",
            initialQuestionAnswer = "",
            initialAnswerParts = listOf(""),
            onConfirm = { newContent, newOptions, finalAnswer ->
                dispatchCommand(
                    SessionCommand.SaveEditedQuestionFields(
                        currentIndex,
                        newContent,
                        finalAnswer,
                        newOptions,
                    ),
                )
            },
            onDismiss = {
                ds.showEditQuestionDialog = false
                dispatchCommand(SessionCommand.ClearEditableQuestion)
            },
        )
    }
    QuestionListDialog(
        show = ds.showList,
        onDismiss = { ds.showList = false },
        questions = questions,
        selectedOptions = selectedOptions,
        textAnswers = textAnswers,
        showResultList = showResultList,
        answerTimes = answerTimeList,
        displayInfoByQuestionId = answerCardDisplayInfo,
        entryGrouped = answerCardEntryGrouped,
        currentIndex = currentIndex,
        onSelect = { dispatchCommand(SessionCommand.GoToQuestion(it)) }
    )
    ExamDialogs(
        showDeleteNoteDialog = ds.showDeleteNoteDialog,
        onDismissDeleteNote = { ds.showDeleteNoteDialog = false },
        onConfirmDeleteNote = {
            dispatchCommand(SessionCommand.SaveNote(activeQuestion.id, currentIndex, ""))
            ds.showDeleteNoteDialog = false
        },
        showDeleteDialog = ds.showDeleteDialog,
        onDismissDelete = { ds.showDeleteDialog = false; ds.deleteTarget = "" },
        onConfirmDelete = {
            when (ds.deleteTarget) {
                "deepseek" -> dispatchCommand(SessionCommand.UpdateAnalysis(currentIndex, ""))
                "spark" -> dispatchCommand(SessionCommand.UpdateSparkAnalysis(currentIndex, ""))
                "baidu" -> dispatchCommand(SessionCommand.UpdateBaiduAnalysis(currentIndex, ""))
            }
            ds.showDeleteDialog = false
            ds.deleteTarget = ""
        },
        deleteReadableLabel = when (ds.deleteTarget) {
            "deepseek" -> stringResource(R.string.ai_name_deepseek)
            "spark" -> stringResource(R.string.ai_name_spark)
            "baidu" -> stringResource(R.string.ai_name_baidu)
            else -> ""
        },
        showExitDialog = ds.showExitDialog,
        onDismissExit = { ds.showExitDialog = false },
        onConfirmExit = {
            coroutineScope.launch {
                val score = suspendExamCommand(bindings, SessionCommand.GradeSession) ?: return@launch
                if (score < 0) return@launch
                onExamEnd(
                    sessionScore, sessionActualAnswered, sessionUnanswered,
                    cumulativeCorrect, cumulativeAnswered, cumulativeExamCount
                )
                ds.showExitDialog = false
            }
        },
        exitConfirmText = if (selectedOptions.any { it.isEmpty() }) {
            stringResource(R.string.confirm_submit_unfinished)
        } else {
            stringResource(R.string.confirm_submit)
        },
        showChatGptDialog = ds.showChatGptDialog,
        onDismissChatGpt = { ds.showChatGptDialog = false },
        onConfirmChatGpt = { ds.showChatGptDialog = false },
        chatGptLoading = false,
        chatGptResult = null
    )
}
