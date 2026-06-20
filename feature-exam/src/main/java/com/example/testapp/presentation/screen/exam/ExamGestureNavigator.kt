package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * State holder for ExamScreen horizontal-drag gesture state.
 * Extracted to remove dragAmount/dragStartX/containerWidth inline remember blocks.
 */
class ExamGestureNavigator {
    var dragAmount by mutableStateOf(0f)
    var dragStartX by mutableStateOf(0f)
    var containerWidth by mutableStateOf(0f)

    fun resetDrag() { dragAmount = 0f }
}
