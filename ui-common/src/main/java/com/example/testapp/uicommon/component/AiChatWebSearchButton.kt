package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens

@Composable
fun AiChatWebSearchButton(
    checked: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = AiChatPromptDesignTokens
    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier.size(tokens.sendButtonSize),
    ) {
        AiChatPromptIcon(
            imageVector = Icons.Filled.Public,
            contentDescription = contentDescription,
            tint = if (checked) {
                tokens.brandBlue
            } else {
                tokens.textSecondary.copy(alpha = 0.8f)
            },
        )
    }
}
