package com.example.testapp.presentation.screen.ai

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.data.repository.QuestionCorrectionParsePipeline
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionCorrectionRequest
import com.example.testapp.feature.ai.R
import com.example.testapp.uicommon.component.QuestionEditDialog
import com.example.testapp.uicommon.component.QuestionEditDraftPatch

/**
 * 共享「修改当前题目」入口：内嵌 AI纠题，仅回填草稿，落库仍走原 onConfirm。
 */
@Composable
fun AiQuestionEditDialog(
    editableQuestion: Question?,
    initialQuestionContent: String,
    initialQuestionAnswer: String,
    initialAnswerParts: List<String>,
    onConfirm: (String, List<String>, String) -> Unit,
    onDismiss: () -> Unit,
    viewModel: AiQuestionCorrectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var draftPatch by remember { mutableStateOf<QuestionEditDraftPatch?>(null) }
    var lastRequestSnapshot by remember {
        mutableStateOf<Triple<String, List<String>, String>?>(null)
    }

    QuestionEditDialog(
        editableQuestion = editableQuestion,
        initialQuestionContent = initialQuestionContent,
        initialQuestionAnswer = initialQuestionAnswer,
        initialAnswerParts = initialAnswerParts,
        onConfirm = onConfirm,
        onDismiss = {
            viewModel.dismissPreview()
            onDismiss()
        },
        onRequestAiCorrect = { content, options, answer ->
            val question = editableQuestion ?: return@QuestionEditDialog
            lastRequestSnapshot = Triple(content, options, answer)
            viewModel.correct(
                QuestionCorrectionRequest(
                    questionType = question.type,
                    content = content,
                    options = options,
                    answer = answer,
                    explanation = question.explanation,
                ),
            )
        },
        draftPatch = draftPatch,
        onDraftPatchConsumed = { draftPatch = null },
        aiCorrectEnabled = uiState !is AiQuestionCorrectionUiState.Loading,
    )

    when (val state = uiState) {
        AiQuestionCorrectionUiState.Idle -> Unit
        AiQuestionCorrectionUiState.Loading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.ai_correct_loading)) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                },
                confirmButton = {},
            )
        }
        is AiQuestionCorrectionUiState.Error -> {
            AlertDialog(
                onDismissRequest = viewModel::dismissPreview,
                title = { Text(stringResource(R.string.parse_failed)) },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissPreview) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
        is AiQuestionCorrectionUiState.Success -> {
            val snapshot = lastRequestSnapshot
            if (snapshot == null) {
                viewModel.dismissPreview()
            } else {
                AiQuestionCorrectionPreviewDialog(
                    originalContent = snapshot.first,
                    originalOptions = snapshot.second,
                    originalAnswer = snapshot.third,
                    suggestion = state.suggestion,
                    onDismiss = viewModel::dismissPreview,
                    onApply = { selection ->
                        val applied = QuestionCorrectionParsePipeline.applyToDraft(
                            currentContent = snapshot.first,
                            currentOptions = snapshot.second,
                            currentAnswer = snapshot.third,
                            currentExplanation = editableQuestion?.explanation.orEmpty(),
                            suggestion = state.suggestion,
                            selection = selection,
                        )
                        draftPatch = QuestionEditDraftPatch(
                            content = if (selection.applyContent) applied.content else null,
                            options = if (selection.applyOptions) applied.options else null,
                            answer = if (selection.applyAnswer) applied.answer else null,
                        )
                        viewModel.dismissPreview()
                        Toast.makeText(
                            context,
                            context.getString(R.string.ai_correct_apply),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }
        }
    }
}
