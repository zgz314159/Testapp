package com.example.testapp.presentation.screen.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.screen.questionbank.components.DefaultQuestionBankFolderItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeFolderCard(
    folderName: String,
    itemCount: Int,
    isDropTarget: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onReportBounds: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    DefaultQuestionBankFolderItem(
        title = folderName,
        count = itemCount,
        isHighlighted = isDropTarget,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .onGloballyPositioned { coords -> onReportBounds(coords.boundsInRoot()) }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    )
}
