package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.testapp.core.common.FontSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ExamFontController(
    defaultFontSize: Float,
    defaultLineSpacing: Float,
    private val fontSettings: FontSettingsRepository
) {
    var questionFontSize by mutableStateOf(defaultFontSize)
    var questionLineSpacing by mutableStateOf(defaultLineSpacing)
    var loaded by mutableStateOf(false); private set

    suspend fun loadFromStore() {
        if (loaded) return
        val storedSize = fontSettings.examFontSize.firstOrNull() ?: Float.NaN
        if (!storedSize.isNaN()) questionFontSize = storedSize
        val storedSpacing = fontSettings.examLineSpacing.firstOrNull() ?: 1.3f
        questionLineSpacing = storedSpacing
        loaded = true
    }

    fun increaseFont(scope: CoroutineScope) {
        val newSize = (questionFontSize + 2f).coerceAtMost(42f)
        questionFontSize = newSize
        scope.launch { fontSettings.setExamFontSize(newSize) }
    }

    fun decreaseFont(scope: CoroutineScope) {
        val newSize = (questionFontSize - 2f).coerceAtLeast(12f)
        questionFontSize = newSize
        scope.launch { fontSettings.setExamFontSize(newSize) }
    }

    fun increaseSpacing(scope: CoroutineScope) {
        val newSpacing = (questionLineSpacing + 0.1f).coerceAtMost(2.2f)
        questionLineSpacing = newSpacing
        scope.launch { fontSettings.setExamLineSpacing(newSpacing) }
    }

    fun decreaseSpacing(scope: CoroutineScope) {
        val newSpacing = (questionLineSpacing - 0.1f).coerceAtLeast(1.0f)
        questionLineSpacing = newSpacing
        scope.launch { fontSettings.setExamLineSpacing(newSpacing) }
    }
}
