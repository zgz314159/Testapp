package com.example.testapp.uicommon.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** @see AiChatPromptSheet */
@Composable
fun AiChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
    sendContentDescription: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 6
) {
    AiChatPromptSheet(
        value = value,
        onValueChange = onValueChange,
        onSend = onSend,
        sendEnabled = sendEnabled,
        sendContentDescription = sendContentDescription,
        placeholder = placeholder,
        modifier = modifier,
        maxLines = maxLines
    )
}
