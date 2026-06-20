package com.example.testapp.presentation.screen.file

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

    private val _hoverFile = MutableStateFlow<String?>(null)
    val hoverFile: StateFlow<String?> = _hoverFile.asStateFlow()

    private var dropCommitted = false

    fun tryCommitDrop(): Boolean {
        if (dropCommitted || _draggingFile.value == null) {
            Log.d(tag, "tryCommitDrop=false committed=$dropCommitted active=${_draggingFile.value}")
            return false
        }
        dropCommitted = true
        Log.d(tag, "tryCommitDrop=true active=${_draggingFile.value}")
        return true
    }

    fun isDropCommitted(): Boolean = dropCommitted

    fun startDragging(file: String, position: Offset, size: IntSize, offset: Offset) {
        dropCommitted = false
        _draggingFile.value = file
        _dragPosition.value = position
        _dragItemSize.value = size
        _offsetWithinItem.value = offset
        Log.d(tag, "startDragging file=$file pos=(${position.x.toInt()},${position.y.toInt()})")
    }

    fun updatePosition(position: Offset) {
        
        _dragPosition.value = position
    }

    fun endDragging() {
        Log.d(tag, "endDragging was=${_draggingFile.value}")
        dropCommitted = false
        _draggingFile.value = null
        _hoverFolder.value = null
        _hoverFile.value = null
        _offsetWithinItem.value = Offset.Zero
    }

    fun setHoverFolder(folder: String?) {
        
        _hoverFolder.value = folder
    }

    fun setHoverFile(file: String?) {
        _hoverFile.value = file
    }
}