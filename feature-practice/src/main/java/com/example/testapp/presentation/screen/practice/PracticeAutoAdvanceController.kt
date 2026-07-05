package com.example.testapp.presentation.screen.practice

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

class PracticeAutoAdvanceController {
    private var job: Job? = null
    private var _active = true
    private var _screenActive = true
    private var _paused = false
    val isActive: Boolean get() = _active

    /** Practice 页不可见（overlay 路由 / ON_PAUSE）时禁止调度与执行跳题。 */
    fun setScreenActive(active: Boolean) {
        _screenActive = active
        PracticeJumpDebugLog.screenActive(active, "setScreenActive")
        if (!active) cancel("setScreenActive(false)")
    }

    fun cancel(source: String = "cancel") {
        if (job != null) PracticeJumpDebugLog.autoAdvanceCancel(source)
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
        onAdvance: suspend () -> Unit,
        advanceOnly: Boolean = false
    ) {
        job?.cancel()
        if (!_active || !_screenActive) {
            PracticeJumpDebugLog.autoAdvanceSkip("active=$_active screenActive=$_screenActive")
            return
        }
        PracticeJumpDebugLog.autoAdvanceSchedule(answeredIndex, delaySec, advanceOnly)
        job = scope.launch {
            if (!_active || !_screenActive) {
                PracticeJumpDebugLog.autoAdvanceBlocked("launch")
                return@launch
            }
            if (revealResultFirst) showResult(answeredIndex, true)
            if (delaySec > 0) pausableDelay(delaySec * 1000L)
            ensureActive()
            if (!_active || !_screenActive) {
                PracticeJumpDebugLog.autoAdvanceBlocked("afterDelay")
                return@launch
            }
            if (!revealResultFirst && !advanceOnly) showResult(answeredIndex, true)
            if (delaySec <= 0) kotlinx.coroutines.yield()
            ensureActive()
            if (!_active || !_screenActive) {
                PracticeJumpDebugLog.autoAdvanceBlocked("beforeFire")
                return@launch
            }
            PracticeJumpDebugLog.autoAdvanceFired(answeredIndex)
            onAdvance()
        }
    }
}
