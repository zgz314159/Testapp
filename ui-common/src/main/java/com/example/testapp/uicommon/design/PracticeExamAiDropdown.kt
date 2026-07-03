package com.example.testapp.uicommon.design

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PracticeExamAiDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    deepSeekLabel: String,
    sparkLabel: String,
    onDeepSeek: () -> Unit,
    onSparkAsk: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(deepSeekLabel) },
            onClick = {
                onDismiss()
                onDeepSeek()
            },
            leadingIcon = {
                Icon(
                    imageVector = iconForPracticeExamAiMenuAction(PracticeExamAiMenuAction.DeepSeek),
                    contentDescription = null
                )
            }
        )
        DropdownMenuItem(
            text = { Text(sparkLabel) },
            onClick = {
                onDismiss()
                onSparkAsk()
            },
            leadingIcon = {
                Icon(
                    imageVector = iconForPracticeExamAiMenuAction(PracticeExamAiMenuAction.SparkAsk),
                    contentDescription = null
                )
            }
        )
    }
}
