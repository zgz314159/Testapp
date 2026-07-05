package com.example.testapp.presentation.screen.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.HomeViewModel

@Composable
fun HomeDraggingFileOverlay(
    viewModel: HomeViewModel,
    fileName: String,
    folderDisplayName: String?,
    dragPosition: Offset,
    dragOffset: Offset,
    dragItemSize: IntSize,
    showTypeSummary: Boolean,
    modifier: Modifier = Modifier
) {
    val fileStatistics by viewModel.fileStatistics.collectAsState()
    val practiceProgress by viewModel.practiceProgress.collectAsState()

    DraggingFileOverlay(
        fileName = fileName,
        statistics = fileStatistics[fileName] ?: FileStatistics(),
        progressCount = practiceProgress[fileName] ?: 0,
        folderDisplayName = folderDisplayName,
        dragPosition = dragPosition,
        dragOffset = dragOffset,
        dragItemSize = dragItemSize,
        showTypeSummary = showTypeSummary,
        modifier = modifier
    )
}
