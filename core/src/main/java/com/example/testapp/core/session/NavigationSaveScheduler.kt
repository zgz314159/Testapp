package com.example.testapp.core.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Coalesces index-only navigation saves so rapid prev/next does not block the main thread.
 */
class NavigationSaveScheduler(
    private val scope: CoroutineScope,
    private val debounceMs: Long = DEFAULT_DEBOUNCE_MS
) {
    private var pendingJob: Job? = null

    fun schedule(save: suspend () -> Unit) {
        pendingJob?.cancel()
        pendingJob = scope.launch {
            delay(debounceMs)
            save()
        }
    }

    fun flushAndSave(save: suspend () -> Unit) {
        pendingJob?.cancel()
        pendingJob = scope.launch { save() }
    }

    fun cancel() {
        pendingJob?.cancel()
        pendingJob = null
    }

    companion object {
        const val DEFAULT_DEBOUNCE_MS = 350L
    }
}
