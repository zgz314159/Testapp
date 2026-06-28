package com.example.testapp.presentation.screen.exam

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ExamFontSettingsMenu(
    increaseFontLabel: String,
    decreaseFontLabel: String,
    increaseLineSpacingLabel: String,
    decreaseLineSpacingLabel: String,
    increaseLetterSpacingLabel: String,
    decreaseLetterSpacingLabel: String,
    editQuestionLabel: String,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit,
    onIncreaseLineSpacing: () -> Unit,
    onDecreaseLineSpacing: () -> Unit,
    onIncreaseLetterSpacing: () -> Unit,
    onDecreaseLetterSpacing: () -> Unit,
    onEditQuestion: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(text = { Text(increaseFontLabel) }, onClick = { onIncreaseFont(); onDismiss() })
    DropdownMenuItem(text = { Text(decreaseFontLabel) }, onClick = { onDecreaseFont(); onDismiss() })
    DropdownMenuItem(text = { Text(increaseLineSpacingLabel) }, onClick = { onIncreaseLineSpacing(); onDismiss() })
    DropdownMenuItem(text = { Text(decreaseLineSpacingLabel) }, onClick = { onDecreaseLineSpacing(); onDismiss() })
    DropdownMenuItem(text = { Text(increaseLetterSpacingLabel) }, onClick = { onIncreaseLetterSpacing(); onDismiss() })
    DropdownMenuItem(text = { Text(decreaseLetterSpacingLabel) }, onClick = { onDecreaseLetterSpacing(); onDismiss() })
    DropdownMenuItem(text = { Text(editQuestionLabel) }, onClick = { onEditQuestion(); onDismiss() })
}
