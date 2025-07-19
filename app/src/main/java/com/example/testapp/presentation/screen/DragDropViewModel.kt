package com.example.testapp.presentation.screen

import android.util.Log
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
    private val tag = "DragDropVM"
    private val _dragPosition = MutableStateFlow(Offset.Zero)
    val dragPosition: StateFlow<Offset> = _dragPosition.asStateFlow()

    private val _offsetWithinItem = MutableStateFlow(Offset.Zero)
    val offsetWithinItem: StateFlow<Offset> = _offsetWithinItem.asStateFlow()


    private val _draggingFile = MutableStateFlow<String?>(null)
    val draggingFile: StateFlow<String?> = _draggingFile.asStateFlow()

    private val _dragItemSize = MutableStateFlow(IntSize.Zero)
    val dragItemSize: StateFlow<IntSize> = _dragItemSize.asStateFlow()

    private val _hoverFolder = MutableStateFlow<String?>(null)
    val hoverFolder: StateFlow<String?> = _hoverFolder.asStateFlow()

    fun startDragging(file: String, position: Offset, size: IntSize, offset: Offset) {
        Log.d(tag, "startDragging file=$file pos=$position size=$size offset=$offset")
        _draggingFile.value = file
        _dragPosition.value = position
        _dragItemSize.value = size
        _offsetWithinItem.value = offset
    }

    fun updatePosition(position: Offset) {
        Log.d(tag, "updatePosition pos=$position")
        _dragPosition.value = position
    }

    fun endDragging() {
        Log.d(tag, "endDragging file=${_draggingFile.value}")
        _draggingFile.value = null
        _hoverFolder.value = null
        _offsetWithinItem.value = Offset.Zero
    }

    fun setHoverFolder(folder: String?) {
        Log.d(tag, "hoverFolder=$folder")
        _hoverFolder.value = folder
    }
}