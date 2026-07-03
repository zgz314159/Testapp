package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

/**
 * Manages the auto-advance timer Job used for examDelay-based
 * automatic navigation after single/judge/fill-submit answer.
 */
class ExamAutoAdvanceTimer {
    var job by mutableStateOf<Job?>(null)
    val isActive get() = job?.isActive == true
    private var _active = true
    private var _paused = false
    val canSchedule get() = _active

    fun cancel() { job?.cancel(); job = null }

    fun pause() { _paused = true }

    fun resume() { _paused = false }

    /**
     * Suspends until [durationMs] has elapsed, respecting pause/resume.
     * While paused, delays in 100ms increments so we can exit promptly on resume.
     */
    private suspend fun pausableDelay(durationMs: Long) {
        val chunk = 100L
        var remaining = durationMs
        while (remaining > 0) {
            while (_paused) {
                delay(chunk)
            }
            val sleep = minOf(chunk, remaining)
            delay(sleep)
            remaining -= sleep
        }
    }

    /**
     * Schedules a delayed action. Cancels any previously active job first.
     * Called from onOptionClick (single/judge) and fill submit.
     */
    fun schedule(
        scope: CoroutineScope,
        delayMs: Long,
        onAdvance: suspend () -> Unit
    ) {
        cancel()
        if (!_active) return
        if (delayMs <= 0) {
            scope.launch {
                ensureActive()
                if (!_active) return@launch
                kotlinx.coroutines.yield()
                ensureActive()
                if (!_active) return@launch
                onAdvance()
            }
        } else {
            job = scope.launch {
                pausableDelay(delayMs)
                ensureActive()
                if (!_active) return@launch
                onAdvance()
            }
        }
    }
}