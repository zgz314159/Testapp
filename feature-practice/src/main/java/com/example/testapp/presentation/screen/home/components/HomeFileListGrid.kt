package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import com.example.testapp.uicommon.component.OptimizedFileCard

@Composable
fun HomeFileListGrid(
    displayFileNames: List<String>,
    folders: Map<String, String?>,
    fileStatistics: Map<String, FileStatistics>,
    practiceProgress: Map<String, Int>,
    selectedFileName: String,
    draggingFile: String?,
    dragPosition: Offset,
    hoverFile: String?,
    shouldTrackDropTargets: Boolean,
    userScrollEnabled: Boolean,
    canKeepSwipeNodeStable: (String) -> Boolean,
    canHandleDrag: (String, Boolean) -> Boolean,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onReportCardBounds: (String, Rect) -> Unit,
    onFileCtaClick: ((String) -> Unit)? = null,
    headerContent: @Composable () -> Unit = {},
    showHeader: Boolean = true,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = userScrollEnabled,
        contentPadding = PaddingValues(bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showHeader) {
            item(key = "home_header", span = { GridItemSpan(2) }) {
                headerContent()
            }
        }
        items(items = displayFileNames, key = { it }, contentType = { "file_card" }) { fileName ->
            val fileStats = fileStatistics[fileName] ?: FileStatistics()
            val progressCount = practiceProgress[fileName] ?: 0
            val questionCount = fileStats.questionCount
            val pct = if (questionCount > 0) (progressCount * 100 / questionCount).coerceIn(0, 100) else 0
            val displayName = remember(fileName) { HomeDashboardPipeline.cleanupDisplayName(fileName) }

            OptimizedFileCard(
                fileName = fileName,
                statistics = fileStats,
                progressCount = progressCount,
                isSelected = selectedFileName == fileName,
                isDropTarget = hoverFile == fileName && draggingFile != fileName,
                folderDisplayName = folders[fileName],
                isDragging = draggingFile == fileName,
                showTypeSummary = false,
                useCompactStyle = false,
                enableDragDrop = canHandleDrag(fileName, false),
                enableLongClickAction = false,
                cardOuterPaddingOverride = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                visualContent = {
                    HomeQuestionBankCard(
                        displayName = displayName,
                        fileName = fileName,
                        progressPercent = pct,
                        questionCount = questionCount,
                        wrongCount = fileStats.wrongCount,
                        favoriteCount = fileStats.favoriteCount,
                        statistics = fileStats,
                        onCtaClick = { onFileCtaClick?.invoke(fileName) },
                    )
                },
                onCardClick = { onCardClick(fileName) },
                onLongClick = null,
                onDoubleClick = null,
                onDragStart = { pos, size, offset -> onDragStart(fileName, pos, size, offset) },
                onDragUpdate = onDragUpdate,
                onDragEnd = { onDragEnd(fileName) },
                onDragCancel = { onDragCancel(fileName) },
            )
        }
    }
}
