package com.example.testapp.presentation.screen.practice

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.testapp.core.common.FontSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/** 练习字体/行距/字间距读写与持久化。 */
class PracticeFontController(
    private val defaultFontSize: Float,
    private val defaultLineSpacing: Float,
    private val fontSettings: FontSettingsRepository
) {
    var questionFontSize by mutableStateOf(defaultFontSize)
    var questionLineSpacing by mutableStateOf(defaultLineSpacing)
    var questionLetterSpacing by mutableStateOf(0f)
    var loaded by mutableStateOf(false); private set

    suspend fun loadFromStore() {
        if (loaded) return
        val storedSize = fontSettings.practiceFontSize.firstOrNull() ?: Float.NaN
        questionFontSize = if (!storedSize.isNaN()) storedSize else defaultFontSize
        questionLineSpacing = fontSettings.practiceLineSpacing.firstOrNull() ?: defaultLineSpacing
        questionLetterSpacing = fontSettings.practiceLetterSpacing.firstOrNull() ?: 0f
        loaded = true
    }

    fun applyFontSize(size: Float, scope: CoroutineScope) {
        val newSize = size.coerceIn(12f, 42f)
        if (newSize == questionFontSize) return
        questionFontSize = newSize
        scope.launch { fontSettings.setPracticeFontSize(newSize) }
    }

    fun applyLineSpacing(spacing: Float, scope: CoroutineScope) {
        val newSpacing = spacing.coerceIn(1.0f, 2.2f)
        if (newSpacing == questionLineSpacing) return
        questionLineSpacing = newSpacing
        scope.launch { fontSettings.setPracticeLineSpacing(newSpacing) }
    }

    fun applyLetterSpacing(spacing: Float, scope: CoroutineScope) {
        val newSpacing = spacing.coerceIn(0f, 2.0f)
        if (newSpacing == questionLetterSpacing) return
        questionLetterSpacing = newSpacing
        scope.launch { fontSettings.setPracticeLetterSpacing(newSpacing) }
    }

    fun increaseFont(scope: CoroutineScope) {
        val newSize = (questionFontSize + 2f).coerceAtMost(42f)
        questionFontSize = newSize
        scope.launch { fontSettings.setPracticeFontSize(newSize) }
    }

    fun decreaseFont(scope: CoroutineScope) {
        val newSize = (questionFontSize - 2f).coerceAtLeast(12f)
        questionFontSize = newSize
        scope.launch { fontSettings.setPracticeFontSize(newSize) }
    }

    fun increaseSpacing(scope: CoroutineScope) {
        val newSpacing = (questionLineSpacing + 0.1f).coerceAtMost(2.2f)
        questionLineSpacing = newSpacing
        scope.launch { fontSettings.setPracticeLineSpacing(newSpacing) }
    }

    fun decreaseSpacing(scope: CoroutineScope) {
        val newSpacing = (questionLineSpacing - 0.1f).coerceAtLeast(1.0f)
        questionLineSpacing = newSpacing
        scope.launch { fontSettings.setPracticeLineSpacing(newSpacing) }
    }

    fun increaseLetterSpacing(scope: CoroutineScope) {
        val newSpacing = (questionLetterSpacing + 0.1f).coerceAtMost(2.0f)
        questionLetterSpacing = newSpacing
        scope.launch { fontSettings.setPracticeLetterSpacing(newSpacing) }
    }

    fun decreaseLetterSpacing(scope: CoroutineScope) {
        val newSpacing = (questionLetterSpacing - 0.1f).coerceAtLeast(0f)
        questionLetterSpacing = newSpacing
        scope.launch { fontSettings.setPracticeLetterSpacing(newSpacing) }
    }
}
