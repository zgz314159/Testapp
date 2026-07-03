package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import com.example.testapp.uicommon.design.AppLoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.R
import com.example.testapp.uicommon.util.buildEditableFillAnswer
import com.example.testapp.uicommon.util.countEditableFillBlanks
import com.example.testapp.uicommon.util.insertEditableAnswerPart
import com.example.testapp.uicommon.util.insertEditableBlankAtCursor
import com.example.testapp.uicommon.util.parsePastedEditableFillAnswers
import com.example.testapp.uicommon.util.removeEditableAnswerPart
import com.example.testapp.uicommon.util.removeEditableBlankAt
import com.example.testapp.uicommon.util.syncEditableFillAnswers

@Composable
fun QuestionEditDialog(
    editableQuestion: Question?,
    initialQuestionContent: String,
    initialQuestionAnswer: String,
    initialAnswerParts: List<String>,
    onConfirm: (String, List<String>, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isFillQuestion = editableQuestion?.let { QuestionTypes.isInlineBlank(it.type) } == true
    val supportsOptionEditing = editableQuestion?.let {
        QuestionTypes.isSingle(it.type) || QuestionTypes.isMulti(it.type) || QuestionTypes.isJudge(it.type)
    } == true
    val minimumOptionCount = 2
    val clipboardManager = LocalClipboardManager.current
    val questionSnapshotKey = remember(
        editableQuestion?.id,
        editableQuestion?.content,
        editableQuestion?.options,
        editableQuestion?.answer,
        initialQuestionContent,
        initialQuestionAnswer,
        initialAnswerParts
    ) {
        listOf(
            editableQuestion?.id,
            editableQuestion?.content,
            editableQuestion?.options?.joinToString("\u0001"),
            editableQuestion?.answer,
            initialQuestionContent,
            initialQuestionAnswer,
            initialAnswerParts.joinToString("\u0001")
        )
    }
    var contentFieldValue by remember(questionSnapshotKey) {
        mutableStateOf(
            TextFieldValue(
                text = editableQuestion?.content ?: initialQuestionContent,
                selection = TextRange((editableQuestion?.content ?: initialQuestionContent).length)
            )
        )
    }
    var editedQuestionAnswer by remember(questionSnapshotKey) {
        mutableStateOf(editableQuestion?.answer ?: initialQuestionAnswer)
    }
    var editedOptions by remember(questionSnapshotKey) {
        mutableStateOf(
            if (supportsOptionEditing) {
                editableQuestion?.options?.takeIf { it.isNotEmpty() } ?: List(minimumOptionCount) { "" }
            } else {
                editableQuestion?.options.orEmpty()
            }
        )
    }
    var editedAnswerParts by remember(questionSnapshotKey) {
        mutableStateOf(
            if (isFillQuestion) {
                syncEditableFillAnswers(
                    initialAnswerParts.ifEmpty { listOf(editableQuestion?.answer ?: initialQuestionAnswer) },
                    countEditableFillBlanks(editableQuestion?.content ?: initialQuestionContent).coerceAtLeast(1)
                )
            } else {
                initialAnswerParts
            }
        )
    }

    LaunchedEffect(questionSnapshotKey) {
        val resolvedContent = editableQuestion?.content ?: initialQuestionContent
        val resolvedAnswer = editableQuestion?.answer ?: initialQuestionAnswer
        contentFieldValue = TextFieldValue(resolvedContent, TextRange(resolvedContent.length))
        editedQuestionAnswer = resolvedAnswer
        editedOptions = if (supportsOptionEditing) {
            editableQuestion?.options?.takeIf { it.isNotEmpty() } ?: List(minimumOptionCount) { "" }
        } else {
            editableQuestion?.options.orEmpty()
        }
        editedAnswerParts = if (isFillQuestion) {
            syncEditableFillAnswers(
                initialAnswerParts.ifEmpty { listOf(resolvedAnswer) },
                countEditableFillBlanks(resolvedContent).coerceAtLeast(1)
            )
        } else {
            initialAnswerParts
        }
    }

    val blankCount = countEditableFillBlanks(contentFieldValue.text).coerceAtLeast(1)
    val finalOptions = if (supportsOptionEditing) editedOptions else editableQuestion?.options.orEmpty()
    val finalAnswer = if (isFillQuestion) buildEditableFillAnswer(editedAnswerParts) else editedQuestionAnswer
    val canSave = editableQuestion != null && contentFieldValue.text.isNotBlank() &&
        if (isFillQuestion) {
            editedAnswerParts.all { it.isNotBlank() }
        } else {
            editedQuestionAnswer.isNotBlank() &&
                (!supportsOptionEditing || (editedOptions.size >= minimumOptionCount && editedOptions.all { it.isNotBlank() }))
        }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.uicommon_edit_current_question_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (editableQuestion == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AppLoadingIndicator()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = contentFieldValue,
                            onValueChange = { newValue ->
                                contentFieldValue = newValue
                                if (isFillQuestion) {
                                    editedAnswerParts = syncEditableFillAnswers(
                                        editedAnswerParts,
                                        countEditableFillBlanks(newValue.text).coerceAtLeast(1)
                                    )
                                }
                            },
                            label = { Text(stringResource(R.string.uicommon_question_content_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 8
                        )
                        if (supportsOptionEditing) {
                            TextButton(onClick = { editedOptions = editedOptions + "" }) {
                                Text(stringResource(R.string.uicommon_add_option))
                            }
                            editedOptions.forEachIndexed { index, option ->
                                EditableTextRow(
                                    value = option,
                                    label = stringResource(R.string.uicommon_option_label, index + 1),
                                    removeContentDescription = stringResource(R.string.uicommon_remove_option),
                                    removeEnabled = editedOptions.size > minimumOptionCount,
                                    onValueChange = { newValue ->
                                        editedOptions = editedOptions.toMutableList().also { it[index] = newValue }
                                    },
                                    onRemove = {
                                        editedOptions = editedOptions.toMutableList().also { it.removeAt(index) }
                                    }
                                )
                            }
                        }
                        if (isFillQuestion) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(
                                    onClick = {
                                        val insertion = insertEditableBlankAtCursor(
                                            content = contentFieldValue.text,
                                            cursor = contentFieldValue.selection.start
                                        )
                                        contentFieldValue = TextFieldValue(
                                            text = insertion.content,
                                            selection = TextRange(insertion.cursorPosition)
                                        )
                                        editedAnswerParts = insertEditableAnswerPart(editedAnswerParts, insertion.blankIndex)
                                    }
                                ) {
                                    Text(stringResource(R.string.uicommon_add_blank))
                                }
                                TextButton(
                                    onClick = {
                                        val pastedAnswers = parsePastedEditableFillAnswers(
                                            pastedText = clipboardManager.getText()?.text.orEmpty(),
                                            currentContent = contentFieldValue.text,
                                            blankCount = blankCount
                                        )
                                        if (pastedAnswers.isNotEmpty()) editedAnswerParts = pastedAnswers
                                    }
                                ) {
                                    Text(stringResource(R.string.uicommon_paste_fill_answers))
                                }
                                TextButton(onClick = { editedAnswerParts = List(blankCount) { "" } }) {
                                    Text(stringResource(R.string.uicommon_clear_fill_answers))
                                }
                            }
                            editedAnswerParts.forEachIndexed { index, part ->
                                EditableTextRow(
                                    value = part,
                                    label = stringResource(R.string.uicommon_blank_answer_label_format, index + 1),
                                    removeContentDescription = stringResource(R.string.uicommon_remove_blank),
                                    removeEnabled = blankCount > 1,
                                    onValueChange = { newValue ->
                                        editedAnswerParts = editedAnswerParts.toMutableList().also { it[index] = newValue }
                                    },
                                    onRemove = {
                                        val updatedContent = removeEditableBlankAt(contentFieldValue.text, index)
                                        contentFieldValue = TextFieldValue(
                                            text = updatedContent,
                                            selection = TextRange(updatedContent.length)
                                        )
                                        editedAnswerParts = removeEditableAnswerPart(editedAnswerParts, index)
                                    }
                                )
                            }
                        } else {
                            OutlinedTextField(
                                value = editedQuestionAnswer,
                                onValueChange = { editedQuestionAnswer = it },
                                label = { Text(stringResource(R.string.uicommon_answer_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.uicommon_cancel))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    TextButton(
                        onClick = { onConfirm(contentFieldValue.text, finalOptions, finalAnswer) },
                        enabled = canSave
                    ) {
                        Text(stringResource(R.string.uicommon_save_changes))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableTextRow(
    value: String,
    label: String,
    removeContentDescription: String,
    removeEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            minLines = 2
        )
        IconButton(
            onClick = onRemove,
            enabled = removeEnabled
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = removeContentDescription,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
