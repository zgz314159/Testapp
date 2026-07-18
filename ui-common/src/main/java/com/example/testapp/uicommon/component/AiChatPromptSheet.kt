package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens
import com.example.testapp.uicommon.design.AppSpacing

/** Gemini 风格底部 prompt sheet：立体白底 + 圆角输入 + 浮起发送。 */
@Composable
fun AiChatPromptSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
    sendContentDescription: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 6,
    webSearchEnabled: Boolean = false,
    webSearchToggleEnabled: Boolean = true,
    webSearchContentDescription: String = "",
    onWebSearchToggle: ((Boolean) -> Unit)? = null,
) {
    val tokens = AiChatPromptDesignTokens
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = tokens.cardWhite,
        tonalElevation = tokens.sheetTopElevation,
        shadowElevation = tokens.sheetShadowElevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = tokens.sheetHorizontalPadding,
                    vertical = tokens.sheetVerticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (onWebSearchToggle != null) {
                AiChatWebSearchButton(
                    checked = webSearchEnabled,
                    enabled = webSearchToggleEnabled,
                    contentDescription = webSearchContentDescription,
                    onCheckedChange = onWebSearchToggle,
                )
            }
            AiChatPromptField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                maxLines = maxLines,
                enabled = sendEnabled,
                modifier = Modifier.weight(1f),
            )
            AiChatPromptSendButton(
                input = value,
                sendEnabled = sendEnabled,
                sendContentDescription = sendContentDescription,
                onSend = onSend,
            )
        }
    }
}
