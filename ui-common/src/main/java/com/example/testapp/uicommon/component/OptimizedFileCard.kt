package com.example.testapp.uicommon.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.uicommon.R

@Composable
private fun rememberFileCardPalette(fileName: String, statistics: FileStatistics): FileCardPalette {
    val tone = remember(fileName, statistics.primaryQuestionType, statistics.questionTypeStats.size) {
        resolveFileCardTone(fileName, statistics)
    }
    return fileCardPalette(tone)
}

@Composable
private fun rememberFileCardStatLabels(): FileCardStatLabels = FileCardStatLabels(
    totalQuestions = stringResource(R.string.uicommon_label_total_questions),
    wrongQuestions = stringResource(R.string.uicommon_label_wrong_questions),
    favoriteCount = stringResource(R.string.uicommon_label_favorite_count),
    progressCount = stringResource(R.string.uicommon_label_progress_count),
)

data class FileCardStatLabels(
    val totalQuestions: String,
    val wrongQuestions: String,
    val favoriteCount: String,
    val progressCount: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OptimizedFileCard(
    fileName: String,
    statistics: FileStatistics,
    progressCount: Int = 0,
    isSelected: Boolean,
    isDropTarget: Boolean = false,
    folderDisplayName: String?,
    isDragging: Boolean,
    showTypeSummary: Boolean = true,
    useCompactStyle: Boolean = false,
    enableDragDrop: Boolean = true,
    /** 长按拖起时再判定；勿用其翻转 enableDragDrop（会重建 pointerInput）。 */
    allowDragStart: () -> Boolean = { true },
    enableLongClickAction: Boolean = true,
    visualContent: (@Composable () -> Unit)? = null,
    cardShapeOverride: RoundedCornerShape? = null,
    cardContainerColorOverride: Color? = null,
    cardBorderOverride: BorderStroke? = null,
    cardElevationOverride: CardElevation? = null,
    cardOuterPaddingOverride: PaddingValues? = null,
    onCardClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onDragStart: (position: Offset, size: IntSize, offset: Offset) -> Unit = { _, _, _ -> },
    onDragUpdate: (position: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: (() -> Unit)? = null,
    onReportCardBounds: ((String, Rect) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragUpdate by rememberUpdatedState(onDragUpdate)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    val currentAllowDragStart by rememberUpdatedState(allowDragStart)
    val currentEnableDragDrop by rememberUpdatedState(enableDragDrop)
    val itemCoordsRef = remember { object { var value: LayoutCoordinates? = null } }

    val appliedShape = cardShapeOverride ?: RoundedCornerShape(16.dp)
    val appliedPadding = cardOuterPaddingOverride ?: PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    val interactionModifier = modifier
        .semantics { contentDescription = "home_file_card:$fileName" }
        .fillMaxWidth()
        .padding(appliedPadding)
            .onGloballyPositioned { coords ->
                itemCoordsRef.value = coords
                onReportCardBounds?.invoke(fileName, coords.boundsInRoot())
            }
            .alpha(if (isDragging) 0f else 1f)
            // 始终挂着 pointerInput，避免 homeContentReady / enableDragDrop 翻转时重建手势链。
            .pointerInput(Unit) {
                var dragActive = false
                detectDragGesturesAfterLongPress(
                    onDragStart = { offsetWithinCard ->
                        if (!currentEnableDragDrop || !currentAllowDragStart()) {
                            return@detectDragGesturesAfterLongPress
                        }
                        dragActive = true
                        val coords = itemCoordsRef.value
                        val startPos = coords?.localToRoot(offsetWithinCard) ?: Offset.Zero
                        val size = coords?.size ?: IntSize.Zero
                        currentOnDragStart(startPos, size, offsetWithinCard)
                    },
                    onDrag = { change, _ ->
                        if (!dragActive) return@detectDragGesturesAfterLongPress
                        change.consume()
                        val pos = itemCoordsRef.value?.localToRoot(change.position) ?: Offset.Zero
                        currentOnDragUpdate(pos)
                    },
                    onDragEnd = { if (dragActive) { dragActive = false; currentOnDragEnd() } },
                    onDragCancel = { if (dragActive) { dragActive = false; currentOnDragCancel?.invoke() } },
                )
            }
            .then(when {
                onDoubleClick == null && !enableLongClickAction -> Modifier.clickable(onClick = onCardClick)
                onDoubleClick == null -> Modifier.combinedClickable(onClick = onCardClick, onLongClick = onLongClick ?: {})
                enableLongClickAction -> Modifier.combinedClickable(onClick = onCardClick, onLongClick = onLongClick, onDoubleClick = onDoubleClick)
                else -> Modifier.combinedClickable(onClick = onCardClick, onDoubleClick = onDoubleClick)
            })

    if (visualContent != null) {
        val visualBorder = cardBorderOverride ?: if (isDropTarget) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
        Box(
            modifier = interactionModifier.then(
                if (visualBorder != null) {
                    Modifier.border(visualBorder, appliedShape)
                } else {
                    Modifier
                },
            ),
        ) {
            visualContent()
        }
        return
    }

    val statLabels = rememberFileCardStatLabels()
    val displayName = folderDisplayName?.let { "$it/$fileName" } ?: fileName
    val palette = rememberFileCardPalette(fileName, statistics)
    val statColors = fileStatPalette()
    val cardBorder = if (isDropTarget) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, palette.borderColor.copy(alpha = 0.55f))
    val appliedContainerColor = cardContainerColorOverride ?: when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isDropTarget -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
        else -> palette.containerColor
    }
    val appliedBorder = cardBorderOverride ?: cardBorder
    val appliedElevation = cardElevationOverride ?: CardDefaults.cardElevation(
        defaultElevation = if (isSelected || isDropTarget) 10.dp else 6.dp,
    )

    Card(
        modifier = interactionModifier,
        shape = appliedShape,
        border = appliedBorder,
        colors = CardDefaults.cardColors(containerColor = appliedContainerColor),
        elevation = appliedElevation,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(text = displayName, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            FileCardStatsRow(labels = statLabels, statistics = statistics, progressCount = progressCount, statColors = statColors)
        }
    }
}

@Composable
private fun FileCardStatsRow(labels: FileCardStatLabels, statistics: FileStatistics, progressCount: Int, statColors: FileStatPalette) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        OptimizedFileStatBlock(label = labels.totalQuestions, value = statistics.questionCount.toString(), valueColor = statColors.total)
        OptimizedFileStatBlock(label = labels.wrongQuestions, value = statistics.wrongCount.toString(), valueColor = statColors.wrong)
        OptimizedFileStatBlock(label = labels.favoriteCount, value = statistics.favoriteCount.toString(), valueColor = statColors.favorite)
        OptimizedFileStatBlock(label = labels.progressCount, value = progressCount.toString(), valueColor = statColors.progress)
    }
}

@Composable
private fun OptimizedFileStatBlock(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = valueColor, fontSize = 14.sp, fontFamily = LocalFontFamily.current, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = label, fontSize = 10.sp, fontFamily = LocalFontFamily.current, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
