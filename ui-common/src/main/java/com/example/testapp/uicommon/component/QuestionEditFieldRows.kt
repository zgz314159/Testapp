package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.R
import com.example.testapp.uicommon.design.AppOverlayMetrics
import com.example.testapp.uicommon.util.EditableFillAnswerFields
import com.example.testapp.uicommon.util.buildEditableFillAnswerPart
import com.example.testapp.uicommon.util.parseEditableFillAnswerFields

@Composable
internal fun QuestionEditToolbar(
    showAddOption: Boolean,
    showAddBlank: Boolean,
    showAiCorrect: Boolean,
    aiCorrectEnabled: Boolean,
    onAddOption: () -> Unit,
    onAddBlank: () -> Unit,
    onPasteFillAnswers: () -> Unit,
    onClearFillAnswers: () -> Unit,
    onAiCorrect: () -> Unit,
) {
    if (!showAddOption && !showAddBlank && !showAiCorrect) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showAddOption) {
            IconButton(onClick = onAddOption) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.uicommon_add_option),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        if (showAddBlank) {
            IconButton(onClick = onAddBlank) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.uicommon_add_blank),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            TextButton(onClick = onPasteFillAnswers) {
                Text(stringResource(R.string.uicommon_paste_fill_answers))
            }
            TextButton(onClick = onClearFillAnswers) {
                Text(stringResource(R.string.uicommon_clear_fill_answers))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (showAiCorrect) {
            IconButton(
                onClick = onAiCorrect,
                enabled = aiCorrectEnabled,
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = stringResource(R.string.uicommon_ai_correct_question),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * 填空答案编辑行：拆成「答案正文 / 属性标签 / 分值」三个输入框，
 * 编辑时不出现「【】」，对外仍以存储串 `答案【标签】【N分】` 交互。
 */
@Composable
internal fun EditableFillAnswerRow(
    rawPart: String,
    label: String,
    removeContentDescription: String,
    removeEnabled: Boolean,
    onRawPartChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val fieldShape = RoundedCornerShape(AppOverlayMetrics.listItemCorner)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    )
    var fields by remember { mutableStateOf(parseEditableFillAnswerFields(rawPart)) }
    // 外部变更（弹窗重载、粘贴/清空答案、删除空位）时按存储串重新同步；
    // 本地编辑往返一致则不重置，保证答案暂空时标签/分值输入不丢。
    if (rawPart != buildEditableFillAnswerPart(fields.answerText, fields.tag, fields.score)) {
        fields = parseEditableFillAnswerFields(rawPart)
    }
    fun update(transform: (EditableFillAnswerFields) -> EditableFillAnswerFields) {
        fields = transform(fields)
        onRawPartChange(buildEditableFillAnswerPart(fields.answerText, fields.tag, fields.score))
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = fields.answerText,
                onValueChange = { newValue -> update { it.copy(answerText = newValue) } },
                label = { Text(label) },
                modifier = Modifier.weight(1f),
                minLines = 2,
                shape = fieldShape,
                colors = fieldColors,
            )
            IconButton(
                onClick = onRemove,
                enabled = removeEnabled,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = removeContentDescription,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = fields.tag,
                onValueChange = { newValue -> update { it.copy(tag = newValue) } },
                label = { Text(stringResource(R.string.uicommon_blank_tag_label)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = fieldShape,
                colors = fieldColors,
            )
            OutlinedTextField(
                value = fields.score,
                onValueChange = { newValue ->
                    update { it.copy(score = newValue.filter(Char::isDigit).take(2)) }
                },
                label = { Text(stringResource(R.string.uicommon_blank_score_label)) },
                modifier = Modifier.width(96.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = fieldShape,
                colors = fieldColors,
            )
        }
    }
}

@Composable
internal fun EditableTextRow(
    value: String,
    label: String,
    removeContentDescription: String,
    removeEnabled: Boolean,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val fieldShape = RoundedCornerShape(AppOverlayMetrics.listItemCorner)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            minLines = 2,
            shape = fieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
        )
        IconButton(
            onClick = onRemove,
            enabled = removeEnabled,
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = removeContentDescription,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
