package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import kotlin.math.roundToInt

/** 拖拽悬浮层：复用 [HomeQuestionBankCard] 白卡样式，与列表中的卡片视觉一致。 */
@Composable
fun HomeDraggingFileOverlay(
    fileName: String,
    statistics: FileStatistics,
    progressCount: Int = 0,
    dragPosition: Offset,
    dragOffset: Offset,
    dragItemSize: IntSize,
    modifier: Modifier = Modifier
) {
    val questionCount = statistics.questionCount
    val pct = if (questionCount > 0) (progressCount * 100 / questionCount).coerceIn(0, 100) else 0
    val displayName = remember(fileName) { HomeDashboardPipeline.cleanupDisplayName(fileName) }

    val density = LocalDensity.current
    val widthDp = with(density) { dragItemSize.width.toDp() }
    val heightDp = with(density) { dragItemSize.height.toDp() }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    (dragPosition.x - dragOffset.x).roundToInt(),
                    (dragPosition.y - dragOffset.y).roundToInt()
                )
            }
            .width(widthDp)
            .height(heightDp)
    ) {
        HomeQuestionBankCard(
            displayName = displayName,
            fileName = fileName,
            progressPercent = pct,
            questionCount = questionCount,
            wrongCount = statistics.wrongCount,
            favoriteCount = statistics.favoriteCount,
            statistics = statistics,
            onCtaClick = {},
        )
    }
}
