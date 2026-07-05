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
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.practice.suspendPracticeCommand
import com.example.testapp.presentation.session.practice.PracticeSessionCommandHandler
import com.example.testapp.presentation.session.practice.rememberQuestionEditSessionBindings
import com.example.testapp.uicommon.component.QuestionEditDialog
import kotlinx.coroutines.launch

@Composable
fun DrawerQuestionEditHost(
    fileName: String,
    questionId: Int,
    onDismiss: () -> Unit,
) {
    val bindings = rememberQuestionEditSessionBindings(fileName, questionId)
    val sendCommand: (SessionCommand) -> Unit = remember(bindings) {
        { command -> PracticeSessionCommandHandler.handle(bindings, command) }
    }
    val questions by bindings.questions.collectAsState()
    val progressLoaded by bindings.progressLoaded.collectAsState()
    val editableQuestion by bindings.editableQuestion.collectAsState()
    val saveSuccess by bindings.saveSuccess.collectAsState()
    val context = LocalContext.current
    val saveSuccessMessage = stringResource(R.string.save_success)
    val coroutineScope = rememberCoroutineScope()

    var targetIndex by remember(fileName, questionId) { mutableIntStateOf(-1) }
    var requestedEdit by remember(fileName, questionId) { mutableStateOf(false) }
    var initialQuestionContent by remember(fileName, questionId) { mutableStateOf("") }
    var initialQuestionAnswer by remember(fileName, questionId) { mutableStateOf("") }
    var initialAnswerParts by remember(fileName, questionId) { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(progressLoaded, questions, fileName, questionId) {
        val resolvedIndex = bindings.indexOfQuestionBySourceId(questionId)
        if (!DrawerQuestionEditPipeline.shouldPrepareEdit(
                requestedEdit,
                progressLoaded,
                questions.isEmpty(),
                resolvedIndex,
            )
        ) {
            return@LaunchedEffect
        }

        targetIndex = resolvedIndex
        val draft = DrawerQuestionEditPipeline.draftFromQuestion(questions.getOrNull(resolvedIndex))
        initialQuestionContent = draft.content
        initialQuestionAnswer = draft.answer
        initialAnswerParts = draft.answerParts
        requestedEdit = true
        sendCommand(SessionCommand.PrepareEditableAtIndex(resolvedIndex))
    }

    LaunchedEffect(editableQuestion?.id, editableQuestion?.content, editableQuestion?.answer) {
        val draft = DrawerQuestionEditPipeline.draftFromQuestion(editableQuestion)
        initialQuestionContent = draft.content
        initialQuestionAnswer = draft.answer
        initialAnswerParts = draft.answerParts
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            Toast.makeText(context, saveSuccessMessage, Toast.LENGTH_SHORT).show()
            sendCommand(SessionCommand.ClearEditableQuestion)
            onDismiss()
        }
    }

    QuestionEditDialog(
        editableQuestion = editableQuestion,
        initialQuestionContent = initialQuestionContent,
        initialQuestionAnswer = initialQuestionAnswer,
        initialAnswerParts = initialAnswerParts,
        onConfirm = { newContent, newOptions, finalAnswer ->
            val editedQuestion = DrawerQuestionEditPipeline.buildSavedQuestion(
                editableQuestion,
                newContent,
                newOptions,
                finalAnswer,
            ) ?: return@QuestionEditDialog
            coroutineScope.launch {
                suspendPracticeCommand(bindings, SessionCommand.SaveEditedQuestion(editedQuestion))
            }
        },
        onDismiss = {
            sendCommand(SessionCommand.ClearEditableQuestion)
            onDismiss()
        },
    )
}
