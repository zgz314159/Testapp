package com.example.testapp.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testapp.domain.usecase.FileStatistics

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OptimizedFileCard(
    fileName: String,
    statistics: FileStatistics,
    progressCount: Int = 0,
    isSelected: Boolean,
    folderDisplayName: String?,
    isDragging: Boolean,
    enableDragDrop: Boolean = true,
    onCardClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onDragStart: (position: Offset, size: IntSize, offset: Offset) -> Unit = { _, _, _ -> },
    onDragUpdate: (position: Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var itemCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    
    val displayName = folderDisplayName?.let { "$it/$fileName" } ?: fileName
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .onGloballyPositioned { coords -> 
                if (enableDragDrop) itemCoords = coords 
            }
            .alpha(if (isDragging) 0f else 1f)
            .then(
                if (enableDragDrop) {
                    Modifier.pointerInput(fileName) {
                        var startedLocally = false
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offsetWithinCard ->
                                startedLocally = true
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                if (startedLocally) {
                                    startedLocally = false
                                    val startPos = itemCoords?.localToRoot(change.position) ?: Offset.Zero
                                    val size = itemCoords?.size ?: IntSize.Zero
                                    onDragStart(startPos, size, change.position)
                                }
                                val pos = itemCoords?.localToRoot(change.position) ?: Offset.Zero
                                onDragUpdate(pos)
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onCardClick,
                onLongClick = { /* 空实现 */ },
                onDoubleClick = onDoubleClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = if (isSelected)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
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
                    label = "总题数",
                    value = statistics.questionCount.toString(),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                OptimizedFileStatBlock(
                    label = "错题数",
                    value = statistics.wrongCount.toString(),
                    valueColor = Color.Red
                )
                OptimizedFileStatBlock(
                    label = "收藏数",
                    value = statistics.favoriteCount.toString(),
                    valueColor = Color.Cyan
                )
                OptimizedFileStatBlock(
                    label = "进度数",
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
    modifier: Modifier = Modifier
) {
    val displayName = folderDisplayName?.let { "$it/$fileName" } ?: fileName
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
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
                    label = "总题数",
                    value = statistics.questionCount.toString(),
                    valueColor = MaterialTheme.colorScheme.primary
                )
                OptimizedFileStatBlock(
                    label = "错题数",
                    value = statistics.wrongCount.toString(),
                    valueColor = Color.Red
                )
                OptimizedFileStatBlock(
                    label = "收藏数",
                    value = statistics.favoriteCount.toString(),
                    valueColor = Color.Cyan
                )
                OptimizedFileStatBlock(
                    label = "进度数",
                    value = progressCount.toString(),
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
