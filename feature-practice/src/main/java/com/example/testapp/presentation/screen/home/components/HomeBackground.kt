package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 首页环境背景层。
 * 亮色：白色到极浅蓝渐变 + 右上角非常轻微的环境光晕
 * 暗色：仅极轻环境光或返回不处理（使用 MaterialTheme 纯色背景）
 */
@Composable
fun HomeBackground(
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isDark) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF8FAFB),
                        Color(0xFFF5F7FA),
                    ),
                )
            ),
    ) {
        // 右上角非常轻的蓝色环境光晕
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.6f)
                .height(350.dp)
                .clip(RoundedCornerShape(175.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4F8CFF).copy(alpha = 0.04f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}
