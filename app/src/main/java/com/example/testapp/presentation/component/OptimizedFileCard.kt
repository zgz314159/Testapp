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
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize

private data class FileCardPalette(
    val containerColor: Color,
    val borderColor: Color
)

private fun placeholderFileCardPalette(fileName: String): FileCardPalette {
    return when (fileName.trim().hashCode().ushr(1) % 5) {
        0 -> FileCardPalette(Color(0xFFE5F0FF), Color(0xFF4A7BD1))
        1 -> FileCardPalette(Color(0xFFE8F6E8), Color(0xFF4D9A57))
        2 -> FileCardPalette(Color(0xFFFFECD9), Color(0xFFC97A30))
        3 -> FileCardPalette(Color(0xFFFFE4E6), Color(0xFFD45D72))
        else -> FileCardPalette(Color(0xFFEAF2E0), Color(0xFF6F8F3A))
    }
}

@Composable
private fun rememberFileCardPalette(fileName: String, statistics: FileStatistics): FileCardPalette {
    val primaryType = statistics.primaryQuestionType.trim()
    val placeholderPalette = remember(fileName) { placeholderFileCardPalette(fileName) }
    return remember(fileName, primaryType, statistics.questionTypeStats.size, placeholderPalette) {
        when {
            statistics.questionTypeStats.size > 1 -> FileCardPalette(
                containerColor = Color(0xFFF6EFE2),
                borderColor = Color(0xFFC69A54)
            )
            primaryType == QuestionTypes.SINGLE -> FileCardPalette(
                containerColor = Color(0xFFFFF4CC),
                borderColor = Color(0xFFD4A62A)
            )
            primaryType == QuestionTypes.MULTI -> FileCardPalette(
                containerColor = Color(0xFFE0F7F1),
                borderColor = Color(0xFF1D8F7A)
            )
            primaryType == QuestionTypes.JUDGE -> FileCardPalette(
                containerColor = Color(0xFFFFE4E6),
                borderColor = Color(0xFFD45D72)
            )
            primaryType == QuestionTypes.BLANK -> FileCardPalette(
                containerColor = Color(0xFFE5F0FF),
                borderColor = Color(0xFF4A7BD1)
            )
            primaryType == "简答题" -> FileCardPalette(
                containerColor = Color(0xFFE8F6E8),
                borderColor = Color(0xFF4D9A57)
            )
            primaryType == "综合题" -> FileCardPalette(
                containerColor = Color(0xFFFFECD9),
                borderColor = Color(0xFFC97A30)
            )
            primaryType == "论述题" -> FileCardPalette(
                containerColor = Color(0xFFE8F1D9),
                borderColor = Color(0xFF6F8F3A)
            )
            else -> placeholderPalette
        }
    }
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
                    valueColor = MaterialTheme.colorScheme.primary
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_wrong_questions),
                    value = statistics.wrongCount.toString(),
                    valueColor = Color.Red
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_favorite_count),
                    value = statistics.favoriteCount.toString(),
                    valueColor = Color.Cyan
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_progress_count),
                    value = progressCount.toString(),
                    valueColor = MaterialTheme.colorScheme.tertiary
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
                    valueColor = MaterialTheme.colorScheme.primary
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_wrong_questions),
                    value = statistics.wrongCount.toString(),
                    valueColor = Color.Red
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_favorite_count),
                    value = statistics.favoriteCount.toString(),
                    valueColor = Color.Cyan
                )
                OptimizedFileStatBlock(
                    label = stringResource(R.string.label_progress_count),
                    value = progressCount.toString(),
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
