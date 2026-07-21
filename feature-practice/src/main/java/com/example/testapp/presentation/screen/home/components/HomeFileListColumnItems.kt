package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.screen.home.HomeDashboardPipeline
import com.example.testapp.presentation.screen.home.model.HomeQuestionBankCardModel
import com.example.testapp.uicommon.component.OptimizedFileCard
import com.example.testapp.uicommon.component.SwipeRevealActionBox

@Composable
internal fun HomeFileListFileRow(
    card: HomeQuestionBankCardModel,
    folders: Map<String, String?>,
    selectedFileName: String,
    draggingFile: String?,
    hoverFile: String?,
    shouldTrackDropTargets: Boolean,
    isScrolling: () -> Boolean,
    cardLayout: HomeDashboardPipeline.QuestionBankCardLayout,
    canKeepSwipeNodeStable: (String) -> Boolean,
    canHandleDrag: (String, Boolean) -> Boolean,
    onCardClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDragStart: (String, Offset, IntSize, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (String) -> Unit,
    onDragCancel: (String) -> Unit,
    onReportCardBounds: (String, Rect) -> Unit,
    onFileCtaClick: ((String) -> Unit)?,
) {
    val fileName = card.fileName
    val cardShape = remember { RoundedCornerShape(20.dp) }
    val outerPadding = remember { PaddingValues(horizontal = 24.dp, vertical = 5.dp) }
    SwipeRevealActionBox(
        enabled = canKeepSwipeNodeStable(fileName),
        modifier = Modifier.fillMaxWidth().then(
            if (shouldTrackDropTargets) {
                Modifier.onGloballyPositioned { coords ->
                    onReportCardBounds(fileName, coords.boundsInRoot())
                }
            } else {
                Modifier
            },
        ),
        background = { ca -> HomeFileListSwipeDeleteBackground(fileName, { onDeleteClick(fileName) }, ca) },
    ) {
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
            allowDragStart = { canHandleDrag(fileName, isScrolling()) },
            enableLongClickAction = false,
            reportBounds = false,
            cardShapeOverride = cardShape,
            cardContainerColorOverride = Color.Transparent,
            cardOuterPaddingOverride = outerPadding,
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
            onDragStart = { pos, sz, off -> onDragStart(fileName, pos, sz, off) },
            onDragUpdate = onDragUpdate,
            onDragEnd = { onDragEnd(fileName) },
            onDragCancel = { onDragCancel(fileName) },
        )
    }
}

@Composable
internal fun HomeFileListFolderRow(
    folderName: String,
    fileCount: Int,
    hoverFolder: String?,
    shouldTrackDropTargets: Boolean,
    swipeRevealEnabled: Boolean,
    onFolderClick: (String) -> Unit,
    onFolderLongPress: (String) -> Unit,
    onDeleteFolderClick: (String) -> Unit,
    onReportFolderBounds: (String, Rect) -> Unit,
) {
    SwipeRevealActionBox(
        enabled = swipeRevealEnabled,
        modifier = Modifier.fillMaxWidth().then(
            if (shouldTrackDropTargets) {
                Modifier.onGloballyPositioned { coords ->
                    onReportFolderBounds(folderName, coords.boundsInRoot())
                }
            } else {
                Modifier
            },
        ),
        background = { ca ->
            HomeFileListSwipeDeleteBackground(folderName, { onDeleteFolderClick(folderName) }, ca)
        },
    ) {
        HomeFolderCard(
            folderName,
            fileCount,
            isDropTarget = hoverFolder == folderName,
            onClick = { onFolderClick(folderName) },
            onLongClick = { onFolderLongPress(folderName) },
            onReportBounds = { },
        )
    }
}
