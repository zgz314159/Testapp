package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens
import com.example.testapp.uicommon.design.AppSpacing

/** Gemini 风格底部 prompt sheet：顶部分割 + 圆角输入 + 圆形发送。不含 imePadding（由 Scaffold bottomBar 处理）。 */
@Composable
fun AiChatPromptSheet(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
    sendContentDescription: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 6
) {
    val tokens = AiChatPromptDesignTokens
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = tokens.sheetTopElevation,
        shadowElevation = tokens.sheetShadowElevation,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = tokens.sheetHorizontalPadding,
                    vertical = tokens.sheetVerticalPadding
                ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.Bottom
        ) {
            AiChatPromptField(
                value = value,
                onValueChange = onValueChange,
                placeholder = placeholder,
                maxLines = maxLines,
                enabled = sendEnabled,
                modifier = Modifier.weight(1f)
            )
            AiChatPromptSendButton(
                input = value,
                sendEnabled = sendEnabled,
                sendContentDescription = sendContentDescription,
                onSend = onSend
            )
        }
    }
}
