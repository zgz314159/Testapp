package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

/**
 * Hero 背景列车装饰图。
 * 遮罩用 [drawWithCache] 缓存 Brush，避免每帧重分配。
 */
@Composable
fun HeroTrainIllustration(
    painter: Painter,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.32f,
            alignment = Alignment.CenterEnd,
            modifier = Modifier.fillMaxSize().fadeEdgesCached(backgroundColor),
        )
    }
}

private fun Modifier.fadeEdgesCached(bgColor: Color): Modifier = this.then(
    Modifier.drawWithCache {
        val horizontal = Brush.horizontalGradient(
            colors = listOf(
                bgColor.copy(alpha = 0.92f),
                bgColor.copy(alpha = 0.92f),
                bgColor.copy(alpha = 0.50f),
                Color.Transparent,
            ),
            startX = 0f,
            endX = size.width * 0.78f,
        )
        val vertical = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                bgColor.copy(alpha = 0.55f),
            ),
            startY = size.height * 0.72f,
            endY = size.height,
        )
        onDrawWithContent {
            drawContent()
            drawRect(brush = horizontal)
            drawRect(brush = vertical)
        }
    },
)
