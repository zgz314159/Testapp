package com.example.testapp.presentation.screen

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DragDropViewModel @Inject constructor() : ViewModel() {
    private val _dragPosition = MutableStateFlow(Offset.Zero)
    val dragPosition: StateFlow<Offset> = _dragPosition.asStateFlow()

    private val _draggingFile = MutableStateFlow<String?>(null)
    val draggingFile: StateFlow<String?> = _draggingFile.asStateFlow()

    private val _dragItemSize = MutableStateFlow(IntSize.Zero)
    val dragItemSize: StateFlow<IntSize> = _dragItemSize.asStateFlow()

    private val _hoverFolder = MutableStateFlow<String?>(null)
    val hoverFolder: StateFlow<String?> = _hoverFolder.asStateFlow()

    fun startDragging(file: String, position: Offset, size: IntSize) {
        _draggingFile.value = file
        _dragPosition.value = position
        _dragItemSize.value = size
    }

    fun updatePosition(position: Offset) {
        _dragPosition.value = position
    }

    fun endDragging() {
        _draggingFile.value = null
        _hoverFolder.value = null
    }

    fun setHoverFolder(folder: String?) {
        _hoverFolder.value = folder
    }
}