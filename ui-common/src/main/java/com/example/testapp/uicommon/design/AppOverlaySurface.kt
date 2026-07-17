package com.example.testapp.uicommon.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

/** Overlay surfaces aligned with answer-page elevated chrome (keep eye-care page bg unchanged). */
object AppOverlayMetrics {
    val dialogCorner = 24.dp
    val sheetTopCorner = 28.dp
    val dialogElevation = 8.dp
    val sheetTonalElevation = 6.dp
    val listItemCorner = 16.dp
    val listItemElevation = 7.dp
    val answerCardCellElevation = 6.dp
    val answerCardCellCurrentElevation = 8.dp
}

@Composable
fun appOverlayContainerColor(): Color =
    if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        QuestionSessionCardContainerLight
    }

@Composable
fun appOverlayDialogShape(): Shape = RoundedCornerShape(AppOverlayMetrics.dialogCorner)

@Composable
fun appOverlaySheetShape(): Shape =
    RoundedCornerShape(
        topStart = AppOverlayMetrics.sheetTopCorner,
        topEnd = AppOverlayMetrics.sheetTopCorner,
    )

/**
 * Consume leftover nested scroll/fling so ModalBottomSheet does not rubber-band
 * when an inner LazyColumn hits the top/bottom (答题卡顶部抖动).
 */
@Composable
fun rememberSheetContentNestedScrollConnection(): NestedScrollConnection =
    remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = if (available.y != 0f) Offset(x = 0f, y = available.y) else Offset.Zero

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                if (available.y != 0f) available else Velocity.Zero
        }
    }
