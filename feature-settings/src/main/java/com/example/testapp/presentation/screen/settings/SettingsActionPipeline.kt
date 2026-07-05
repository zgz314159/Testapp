package com.example.testapp.presentation.screen.settings

import com.example.testapp.core.common.LocalizedResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SettingsActionPipeline(
    private val scope: CoroutineScope,
    private val isLoading: MutableStateFlow<Boolean>,
    private val progress: MutableStateFlow<Float>,
    private val messageResult: MutableStateFlow<LocalizedResult?>
) {
    private var currentJob: Job? = null

    fun launchCancellable(
        onError: suspend (Exception) -> Unit,
        block: suspend SettingsActionPipeline.() -> Unit
    ) {
        currentJob?.cancel()
        currentJob = scope.launch {
            isLoading.value = true
            try {
                block()
            } catch (e: Exception) {
                onError(e)
            } finally {
                isLoading.value = false
                progress.value = 0f
            }
        }
    }

    fun launch(block: suspend SettingsActionPipeline.() -> Unit) {
        scope.launch { block() }
    }

    fun launchLoading(block: suspend SettingsActionPipeline.() -> Unit) {
        scope.launch {
            isLoading.value = true
            try {
                block()
            } finally {
                isLoading.value = false
            }
        }
    }

    fun cancel() {
        currentJob?.cancel()
        isLoading.value = false
        progress.value = 0f
    }

    fun setProgress(value: Float) {
        progress.value = value
    }

    fun setMessage(value: LocalizedResult?) {
        messageResult.value = value
    }
}
