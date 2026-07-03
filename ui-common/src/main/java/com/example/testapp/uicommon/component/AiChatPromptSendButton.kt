package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens
import com.example.testapp.uicommon.design.AiChatSendEnabledPipeline

@Composable
fun AiChatPromptSendButton(
    input: String,
    sendEnabled: Boolean,
    sendContentDescription: String,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = AiChatSendEnabledPipeline.isEnabled(sendEnabled, input)
    val tokens = AiChatPromptDesignTokens
    FilledIconButton(
        onClick = onSend,
        enabled = active,
        modifier = modifier.size(tokens.sendButtonSize),
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
            contentColor = if (active) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            },
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = sendContentDescription
        )
    }
}
