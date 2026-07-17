package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens
import com.example.testapp.uicommon.design.AppSpacing

@Composable
fun AiChatTypingBubble(
    label: String,
    modifier: Modifier = Modifier,
    geminiStyle: Boolean = true,
) {
    val tokens = AiChatPromptDesignTokens
    Surface(
        modifier = modifier
            .fillMaxWidth(if (geminiStyle) 1f else 0.55f)
            .padding(vertical = AppSpacing.xs),
        shape = RoundedCornerShape(if (geminiStyle) tokens.assistantCardCornerRadius else 16.dp),
        color = tokens.cardWhite,
        tonalElevation = 2.dp,
        shadowElevation = tokens.bubbleElevation,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = tokens.brandBlue,
            )
            Text(
                text = label,
                color = tokens.textSecondary,
            )
        }
    }
}
