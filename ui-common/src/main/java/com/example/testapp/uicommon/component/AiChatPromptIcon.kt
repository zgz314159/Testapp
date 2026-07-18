package com.example.testapp.uicommon.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.testapp.uicommon.design.AiChatPromptDesignTokens

/** 输入栏图标统一绘制：同尺寸 + 同款投影（错位暗影复绘出立体感，无圆圈底）。 */
@Composable
fun AiChatPromptIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = AiChatPromptDesignTokens
    Box(modifier = modifier.size(tokens.promptIconSize), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = Color.Black.copy(alpha = tokens.promptIconShadowAlpha),
            modifier = Modifier
                .size(tokens.promptIconSize)
                .offset(x = 1.dp, y = 1.5.dp),
        )
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(tokens.promptIconSize),
        )
    }
}
