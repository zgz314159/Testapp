package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    Surface(
        onClick = onSend,
        enabled = active,
        modifier = modifier.size(tokens.sendButtonSize),
        shape = CircleShape,
        color = if (active) tokens.brandBlue else Color(0xFFE4EAF2),
        tonalElevation = if (active) 2.dp else 0.dp,
        shadowElevation = if (active) tokens.sendButtonElevation else 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = sendContentDescription,
                tint = if (active) Color.White else tokens.textSecondary.copy(alpha = 0.45f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
