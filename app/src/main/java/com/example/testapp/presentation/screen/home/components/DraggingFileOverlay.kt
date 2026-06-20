package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.component.DraggingFileCard
import kotlin.math.roundToInt

@Composable
fun DraggingFileOverlay(
    fileName: String,
    statistics: FileStatistics,
    progressCount: Int = 0,
    folderDisplayName: String?,
    dragPosition: Offset,
    dragOffset: Offset,
    dragItemSize: IntSize,
    showTypeSummary: Boolean = false,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val widthDp = with(density) { dragItemSize.width.toDp() }
    val heightDp = with(density) { dragItemSize.height.toDp() }

    DraggingFileCard(
        fileName = fileName,
        statistics = statistics,
        progressCount = progressCount,
        folderDisplayName = folderDisplayName,
        dragPosition = dragPosition,
        dragOffset = dragOffset,
        dragItemSize = dragItemSize,
        showTypeSummary = showTypeSummary,
        modifier = modifier
            .offset {
                IntOffset(
                    (dragPosition.x - dragOffset.x).roundToInt(),
                    (dragPosition.y - dragOffset.y).roundToInt()
                )
            }
            .width(widthDp)
            .height(heightDp)
    )
}
