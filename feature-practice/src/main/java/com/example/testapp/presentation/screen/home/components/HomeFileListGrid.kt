package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import com.example.testapp.presentation.screen.home.model.HomeQuestionBankCardModel
import com.example.testapp.uicommon.component.OptimizedFileCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeFileListGrid(
    fileCards: List<HomeQuestionBankCardModel>,
    folders: Map<String, String?>,
    selectedFileName: String,
    draggingFile: String?,
    hoverFile: String?,
    userScrollEnabled: Boolean,
    cardLayout: HomeDashboardPipeline.QuestionBankCardLayout,
    canHandleDrag: (String, Boolean) -> Boolean,
    onCardClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onFileCtaClick: ((String) -> Unit)? = null,
    headerContent: @Composable () -> Unit = {},
    showHeader: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState(cacheWindow = HomeQuestionBankCacheWindow)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        state = gridState,
        userScrollEnabled = userScrollEnabled,
        contentPadding = PaddingValues(bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showHeader) {
            item(key = "header", contentType = "header", span = { GridItemSpan(2) }) {
                headerContent()
            }
        }
        items(items = fileCards, key = { "file:${it.fileName}" }, contentType = { "file_card" }) { card ->
            val fileName = card.fileName
            OptimizedFileCard(
                fileName = fileName,
                statistics = card.statistics,
                progressCount = card.progressCount,
                isSelected = selectedFileName == fileName,
                isDropTarget = hoverFile == fileName && draggingFile != fileName,
                folderDisplayName = folders[fileName],
                isDragging = draggingFile == fileName,
                showTypeSummary = false,
                useCompactStyle = false,
                enableDragDrop = true,
                allowDragStart = { canHandleDrag(fileName, false) },
                enableLongClickAction = false,
                reportBounds = false,
                cardOuterPaddingOverride = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                visualContent = {
                    HomeQuestionBankCard(
                        model = card,
                        layout = cardLayout,
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
