package com.example.testapp.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.testapp.R
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize

@Composable
private fun rememberFileCardPalette(fileName: String, statistics: FileStatistics): FileCardPalette {
    val tone = remember(fileName, statistics.primaryQuestionType, statistics.questionTypeStats.size) {
        resolveFileCardTone(fileName, statistics)
    }
    return fileCardPalette(tone)
}

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
    enableLongClickAction: Boolean = true,
    onCardClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onDragStart: (position: Offset, size: IntSize, offset: Offset) -> Unit = { _, _, _ -> },
    onDragUpdate: (position: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: (() -> Unit)? = null,
    onReportCardBounds: ((String, Rect) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDragUpdate by rememberUpdatedState(onDragUpdate)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)
    val itemCoordsRef = remember {
        object {
            var value: LayoutCoordinates? = null
        }
    }
    
    val displayName = folderDisplayName?.let { "$it/$fileName" } ?: fileName
    val palette = rememberFileCardPalette(fileName, statistics)
    val statColors = fileStatPalette()
    val cardBorder = if (isDropTarget) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, palette.borderColor.copy(alpha = 0.55f))
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .onGloballyPositioned { coords ->
                itemCoordsRef.value = coords
                onReportCardBounds?.invoke(fileName, coords.boundsInRoot())
            }
            .alpha(if (isDragging) 0f else 1f)
            .then(
                if (enableDragDrop) {
                    Modifier.pointerInput(fileName, enableDragDrop) {
                        var dragActive = false
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offsetWithinCard ->
                                dragActive = true
                                val coords = itemCoordsRef.value
                                val startPos = coords?.localToRoot(offsetWithinCard) ?: Offset.Zero
                                val size = coords?.size ?: IntSize.Zero
                                currentOnDragStart(startPos, size, offsetWithinCard)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val pos = itemCoordsRef.value?.localToRoot(change.position) ?: Offset.Zero
                                currentOnDragUpdate(pos)
                            },
                            onDragEnd = {
                                if (dragActive) {
                                    dragActive = false
                                    currentOnDragEnd()
                                }
                            },
                            onDragCancel = {
                                dragActive = false
                                currentOnDragCancel?.invoke()
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .then(
                if (onDoubleClick == null && !enableLongClickAction) {
                    Modifier.clickable(onClick = onCardClick)
                } else if (onDoubleClick == null) {
                    Modifier.combinedClickable(
                        onClick = onCardClick,
                        onLongClick = onLongClick ?: {}
                    )
                } else if (enableLongClickAction) {
                    Modifier.combinedClickable(
                        onClick = onCardClick,
                        onLongClick = onLongClick,
                        onDoubleClick = onDoubleClick
                    )
                } else {
                    Modifier.combinedClickable(
                        onClick = onCardClick,
                        onDoubleClick = onDoubleClick
                    )
                }
            ),
        shape = RoundedCornerShape(16.dp),
        border = cardBorder,
        colors = when {
            isSelected -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            isDropTarget -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
            )
            else -> CardDefaults.cardColors(containerColor = palette.containerColor)
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isSelected || isDropTarget -> 8.dp
                else -> 2.dp
            }
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = displayName,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_total_questions),
                    value = statistics.questionCount.toString(),
                    valueColor = statColors.total
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_wrong_questions),
                    value = statistics.wrongCount.toString(),
                    valueColor = statColors.wrong
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_favorite_count),
                    value = statistics.favoriteCount.toString(),
                    valueColor = statColors.favorite
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_progress_count),
                    value = progressCount.toString(),
                    valueColor = statColors.progress
                )
            }
        }
    }
}

@Composable
private fun OptimizedFileStatBlock(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontFamily = LocalFontFamily.current,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = LocalFontFamily.current,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DraggingFileCard(
    fileName: String,
    statistics: FileStatistics,
    progressCount: Int = 0,
    folderDisplayName: String?,
    dragPosition: Offset,
    dragOffset: Offset,
    dragItemSize: IntSize,
    showTypeSummary: Boolean = true,
    modifier: Modifier = Modifier
) {
    val displayName = folderDisplayName?.let { "$it/$fileName" } ?: fileName
    val palette = rememberFileCardPalette(fileName, statistics)
    val statColors = fileStatPalette()
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, palette.borderColor.copy(alpha = 0.55f)),
        colors = CardDefaults.cardColors(containerColor = palette.containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = displayName,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_total_questions),
                    value = statistics.questionCount.toString(),
                    valueColor = statColors.total
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_wrong_questions),
                    value = statistics.wrongCount.toString(),
                    valueColor = statColors.wrong
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_favorite_count),
                    value = statistics.favoriteCount.toString(),
                    valueColor = statColors.favorite
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_progress_count),
                    value = progressCount.toString(),
                    valueColor = statColors.progress
                )
            }
        }
    }
}
