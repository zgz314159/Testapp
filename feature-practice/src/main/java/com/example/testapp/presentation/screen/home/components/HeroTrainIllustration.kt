package com.example.testapp.presentation.screen.home.components

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.presentation.screen.home.HomePerformanceLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Hero 背景列车装饰图。
 * 遮罩用 [drawWithCache] 缓存 Brush，避免每帧重分配。
 */
@Composable
fun HeroTrainIllustration(
    @DrawableRes imageRes: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    val image by produceState<ImageBitmap?>(initialValue = HomeHeroImageCache.image, resources, imageRes) {
        if (value == null) {
            value = withContext(Dispatchers.Default) {
                HomePerformanceLog.measure("hero_decode") {
                    BitmapFactory.decodeResource(resources, imageRes)?.asImageBitmap()
                }
            }
            HomeHeroImageCache.image = value
            HomePerformanceLog.event("hero_ready size=${value?.width}x${value?.height}")
        } else {
            HomePerformanceLog.event("hero_cache_hit size=${value?.width}x${value?.height}")
        }
    }

    Box(modifier = modifier) {
        image?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.32f,
                alignment = Alignment.CenterEnd,
                modifier = Modifier.fillMaxSize().fadeEdgesCached(backgroundColor),
            )
        }
    }
}

private object HomeHeroImageCache {
    @Volatile
    var image: ImageBitmap? = null
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
