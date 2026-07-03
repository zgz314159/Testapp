package com.example.testapp.uicommon.design

import androidx.compose.ui.graphics.Color
import com.example.testapp.uicommon.model.AiChatMessageRole

data class AiChatBubbleColors(
    val container: Color,
    val content: Color
)

/** 无状态：角色 → 气泡容器/文字色。 */
object AiChatBubbleColorPipeline {

    fun resolve(
        role: AiChatMessageRole,
        primary: Color,
        primaryContainer: Color,
        onPrimary: Color,
        onPrimaryContainer: Color,
        surfaceVariant: Color,
        onSurface: Color,
        isError: Boolean = false,
        errorContainer: Color = surfaceVariant,
        onErrorContainer: Color = onSurface
    ): AiChatBubbleColors = when {
        isError -> AiChatBubbleColors(errorContainer, onErrorContainer)
        role == AiChatMessageRole.User ->
            AiChatBubbleColors(primaryContainer, onPrimaryContainer)
        else -> AiChatBubbleColors(surfaceVariant, onSurface)
    }
}
