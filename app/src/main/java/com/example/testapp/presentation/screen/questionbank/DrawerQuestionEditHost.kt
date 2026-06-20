package com.example.testapp.presentation.screen.questionbank

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.R
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.presentation.screen.practice.PracticeViewModel
import com.example.testapp.uicommon.component.QuestionEditDialog
import com.example.testapp.uicommon.util.normalizeEditableFillAnswers
import kotlinx.coroutines.launch

@Composable
fun DrawerQuestionEditHost(
    fileName: String,
    questionId: Int,
    onDismiss: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val editableQuestion by viewModel.editableQuestion.collectAsState()
    val context = LocalContext.current
    val saveSuccessMessage = stringResource(R.string.save_success)
    val coroutineScope = rememberCoroutineScope()

    var targetIndex by remember(fileName, questionId) { mutableIntStateOf(-1) }
    var requestedEdit by remember(fileName, questionId) { mutableStateOf(false) }
    var initialQuestionContent by remember(fileName, questionId) { mutableStateOf("") }
    var initialQuestionAnswer by remember(fileName, questionId) { mutableStateOf("") }
    var initialAnswerParts by remember(fileName, questionId) { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(fileName, questionId) {
        requestedEdit = false
        targetIndex = -1
        viewModel.clearEditableQuestion()
        viewModel.setProgressId(
            id = fileName,
            questionsId = fileName,
            questionCount = 0,
            random = false
        )
    }

    LaunchedEffect(progressLoaded, questions, fileName, questionId) {
        if (requestedEdit || !progressLoaded || questions.isEmpty()) return@LaunchedEffect
        val resolvedIndex = viewModel.indexOfQuestionBySourceId(questionId)
        if (resolvedIndex < 0) return@LaunchedEffect

        targetIndex = resolvedIndex
        val question = questions.getOrNull(resolvedIndex)
        initialQuestionContent = question?.content.orEmpty()
        initialQuestionAnswer = question?.answer.orEmpty()
        initialAnswerParts = if (question?.let { QuestionTypes.isInlineBlank(it.type) } == true) {
            normalizeEditableFillAnswers(initialQuestionContent, initialQuestionAnswer)
        } else {
            listOf(initialQuestionAnswer)
        }
        requestedEdit = true
        viewModel.prepareEditableQuestion(resolvedIndex)
    }

    LaunchedEffect(editableQuestion?.id, editableQuestion?.content, editableQuestion?.answer) {
        val question = editableQuestion ?: return@LaunchedEffect
        initialQuestionContent = question.content
        initialQuestionAnswer = question.answer
        initialAnswerParts = if (QuestionTypes.isInlineBlank(question.type)) {
            normalizeEditableFillAnswers(question.content, question.answer)
        } else {
            listOf(question.answer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collect {
            Toast.makeText(context, saveSuccessMessage, Toast.LENGTH_SHORT).show()
            viewModel.clearEditableQuestion()
            onDismiss()
        }
    }

    QuestionEditDialog(
        editableQuestion = editableQuestion,
        initialQuestionContent = initialQuestionContent,
        initialQuestionAnswer = initialQuestionAnswer,
        initialAnswerParts = initialAnswerParts,
        onConfirm = { newContent, newOptions, finalAnswer ->
            val index = targetIndex
            if (index >= 0) {
                val editedQuestion = editableQuestion?.copy(
                    content = newContent,
                    answer = finalAnswer,
                    options = newOptions,
                    isEdited = true
                )
                if (editedQuestion != null) {
                    coroutineScope.launch {
                        viewModel.saveEditedQuestion(editedQuestion)
                    }
                }
            }
        },
        onDismiss = {
            viewModel.clearEditableQuestion()
            onDismiss()
        }
    )
}

