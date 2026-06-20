package com.example.testapp.presentation.screen.practice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class PracticeAutoAdvanceController {
    private var job: Job? = null
    private var _active = true
    private var _paused = false
    val isActive: Boolean get() = _active

    fun cancel() {
        job?.cancel()
        job = null
    }

    fun pause() {
        _paused = true
    }

    fun resume() {
        _paused = false
    }

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

    fun schedule(
        scope: CoroutineScope,
        answeredIndex: Int,
        delaySec: Int,
        revealResultFirst: Boolean,
        showResult: (Int, Boolean) -> Unit,
        onAdvance: suspend () -> Unit
    ) {
        job?.cancel()
        if (!_active) return
        job = scope.launch {
            if (!_active) return@launch
            if (revealResultFirst) showResult(answeredIndex, true)
            if (delaySec > 0) pausableDelay(delaySec * 1000L)
            ensureActive()
            if (!_active) return@launch
            if (!revealResultFirst) showResult(answeredIndex, true)
            onAdvance()
        }
    }
}