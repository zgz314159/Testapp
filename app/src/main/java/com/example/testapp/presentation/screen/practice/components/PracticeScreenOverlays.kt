package com.example.testapp.presentation.screen.practice.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.testapp.R
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.presentation.screen.practice.PracticeAutoAdvanceController
import com.example.testapp.presentation.screen.practice.PracticeDialogsHost
import com.example.testapp.presentation.screen.practice.PracticeFontController
import com.example.testapp.presentation.screen.practice.PracticeSessionExitConfirmPipeline
import com.example.testapp.presentation.screen.practice.PracticeViewModel
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.QuestionEditDialog
import com.example.testapp.uicommon.design.AppScrollBottomSheet
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.QuestionTypographySheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PracticeScreenOverlays(
    showTypographySheet: Boolean,
    onDismissTypography: () -> Unit,
    fc: PracticeFontController,
    coroutineScope: CoroutineScope,
    showList: Boolean,
    onDismissList: () -> Unit,
    questions: List<Question>,
    selectedOptions: List<List<Int>>,
    textAnswers: List<String>,
    showResultList: List<Boolean>,
    answerCardDisplayInfo: Map<Int, AnswerCardDisplayInfo>,
    answerCardEntryGrouped: Boolean,
    currentIndex: Int,
    onSelectQuestion: (Int) -> Unit,
    showEditQuestionDialog: Boolean,
    editableQuestion: Question?,
    editedQuestionContent: String,
    editedQuestionAnswer: String,
    editedAnswerParts: List<String>,
    onConfirmEdit: (String, List<String>, String) -> Unit,
    onDismissEdit: () -> Unit,
    showExplanationFull: Boolean,
    onDismissExplanationFull: () -> Unit,
    activeQuestion: Question,
    showDeleteExplanationDialog: Boolean,
    onDismissDeleteExplanation: () -> Unit,
    onConfirmDeleteExplanation: () -> Unit,
    showDeleteNoteDialog: Boolean,
    onDismissDeleteNote: () -> Unit,
    onConfirmDeleteNote: () -> Unit,
    showDeleteDialog: Boolean,
    deleteTarget: String,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    showExitDialog: Boolean,
    sessionInputCount: Int,
    totalCount: Int,
    onDismissExit: () -> Unit,
    autoAdvance: PracticeAutoAdvanceController,
    viewModel: PracticeViewModel,
    onQuizEnd: (Int, Int, Int, Int?, Int?) -> Unit,
    showChatGptDialog: Boolean,
    onDismissChatGpt: () -> Unit,
    chatGptLoading: Boolean,
    chatGptResult: Pair<Int, LocalizedResult>?,
    onSaveChatGptToAnalysis: (String) -> Unit
) {
    QuestionTypographySheet(
        visible = showTypographySheet,
        fontSize = fc.questionFontSize,
        lineSpacing = fc.questionLineSpacing,
        letterSpacing = fc.questionLetterSpacing,
        onFontSizeChange = { fc.applyFontSize(it, coroutineScope) },
        onLineSpacingChange = { fc.applyLineSpacing(it, coroutineScope) },
        onLetterSpacingChange = { fc.applyLetterSpacing(it, coroutineScope) },
        onDismiss = onDismissTypography
    )
    PracticeQuestionListDialog(
        show = showList,
        onDismiss = onDismissList,
        questions = questions,
        selectedOptions = selectedOptions,
        textAnswers = textAnswers,
        showResultList = showResultList,
        displayInfoByQuestionId = answerCardDisplayInfo,
        entryGrouped = answerCardEntryGrouped,
        currentIndex = currentIndex,
        onSelect = onSelectQuestion
    )
    if (showEditQuestionDialog) {
        QuestionEditDialog(
            editableQuestion = editableQuestion,
            initialQuestionContent = editedQuestionContent,
            initialQuestionAnswer = editedQuestionAnswer,
            initialAnswerParts = editedAnswerParts,
            onConfirm = onConfirmEdit,
            onDismiss = onDismissEdit
        )
    }
    if (showExplanationFull) {
        AppScrollBottomSheet(onDismiss = onDismissExplanationFull) {
            Text(
                text = stringResource(R.string.analysis_prefix) + activeQuestion.explanation,
                fontSize = fc.questionFontSize.sp,
                fontFamily = LocalFontFamily.current,
                lineHeight = (fc.questionFontSize * fc.questionLineSpacing).sp,
                letterSpacing = fc.questionLetterSpacing.sp,
                modifier = Modifier.padding(horizontal = AppSpacing.md)
            )
        }
    }
    if (showDeleteExplanationDialog) {
        AlertDialog(
            onDismissRequest = onDismissDeleteExplanation,
            confirmButton = {
                TextButton(onClick = onConfirmDeleteExplanation) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteExplanation) {
                    Text(stringResource(R.string.cancel))
                }
            },
            text = { Text(stringResource(R.string.confirm_delete_explanation)) }
        )
    }
    PracticeDialogsHost(
        showDeleteNoteDialog = showDeleteNoteDialog,
        onDismissDeleteNote = onDismissDeleteNote,
        onConfirmDeleteNote = onConfirmDeleteNote,
        showDeleteDialog = showDeleteDialog,
        deleteTarget = deleteTarget,
        onDismissDelete = onDismissDelete,
        onConfirmDelete = onConfirmDelete,
        showExitDialog = showExitDialog,
        sessionInputCount = sessionInputCount,
        totalCount = totalCount,
        onDismissExit = onDismissExit,
        onConfirmExit = {
            autoAdvance.cancel()
            onDismissExit()
            coroutineScope.launch {
                val graded = viewModel.gradeSessionOnSubmit()
                val params = PracticeSessionExitConfirmPipeline.buildQuizEndParams(
                    graded = graded,
                    sessionInputCount = sessionInputCount,
                    totalCount = totalCount
                )
                if (params.shouldRecordHistory) {
                    viewModel.addHistoryRecord(
                        params.sessionScore,
                        totalCount,
                        params.realUnanswered
                    )
                }
                onQuizEnd(
                    params.sessionScore,
                    params.sessionAnsweredForDisplay,
                    params.realUnanswered,
                    viewModel.correctCount,
                    viewModel.answeredCount
                )
            }
        },
        showChatGptDialog = showChatGptDialog,
        onDismissChatGpt = onDismissChatGpt,
        chatGptLoading = chatGptLoading,
        chatGptResult = chatGptResult,
        currentIndex = currentIndex,
        onSaveChatGptToAnalysis = onSaveChatGptToAnalysis,
        deepseekLabel = stringResource(R.string.ai_name_deepseek),
        sparkLabel = stringResource(R.string.ai_name_spark),
        baiduLabel = stringResource(R.string.ai_name_baidu)
    )
}
