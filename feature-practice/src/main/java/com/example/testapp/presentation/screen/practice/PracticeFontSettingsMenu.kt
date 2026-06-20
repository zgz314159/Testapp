package com.example.testapp.presentation.screen.practice

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PracticeFontSettingsMenu(
    increaseFontLabel: String,
    decreaseFontLabel: String,
    increaseLineSpacingLabel: String,
    decreaseLineSpacingLabel: String,
    increaseLetterSpacingLabel: String,
    decreaseLetterSpacingLabel: String,
    editQuestionLabel: String,
    onFontSizeChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onLetterSpacingChange: (Float) -> Unit,
    onEditQuestion: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(increaseFontLabel) },
        onClick = {
            onFontSizeChange(2f)
            onDismiss()
        }
    )
    DropdownMenuItem(
        text = { Text(decreaseFontLabel) },
        onClick = {
            onFontSizeChange(-2f)
            onDismiss()
        }
    )
    DropdownMenuItem(
        text = { Text(increaseLineSpacingLabel) },
        onClick = {
            onLineSpacingChange(0.1f)
            onDismiss()
        }
    )
    DropdownMenuItem(
        text = { Text(decreaseLineSpacingLabel) },
        onClick = {
            onLineSpacingChange(-0.1f)
            onDismiss()
        }
    )
    DropdownMenuItem(
        text = { Text(increaseLetterSpacingLabel) },
        onClick = {
            onLetterSpacingChange(0.05f)
            onDismiss()
        }
    )
    DropdownMenuItem(
        text = { Text(decreaseLetterSpacingLabel) },
        onClick = {
            onLetterSpacingChange(-0.05f)
            onDismiss()
        }
    )
    DropdownMenuItem(
        text = { Text(editQuestionLabel) },
        onClick = {
            onEditQuestion()
            onDismiss()
        }
    )
}
