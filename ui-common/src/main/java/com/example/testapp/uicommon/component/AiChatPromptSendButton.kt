package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.IconButton
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
    modifier: Modifier = Modifier,
) {
    val tokens = AiChatPromptDesignTokens
    val active = AiChatSendEnabledPipeline.isEnabled(sendEnabled, input)
    IconButton(
        onClick = onSend,
        enabled = active,
        modifier = modifier.size(tokens.sendButtonSize),
    ) {
        AiChatPromptIcon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = sendContentDescription,
            tint = if (active) {
                tokens.brandBlue
            } else {
                tokens.textSecondary.copy(alpha = 0.45f)
            },
        )
    }
}
