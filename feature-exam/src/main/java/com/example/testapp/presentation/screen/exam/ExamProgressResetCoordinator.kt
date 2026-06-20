package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.session.SessionEngine
import com.example.testapp.domain.model.PracticeSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class ExamProgressResetCoordinator(
    private val scope: CoroutineScope,
    private val sessionEngine: SessionEngine,
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val progressId: () -> String,
    private val setProgressSeed: (Long) -> Unit,
    private val resetArtifactLoadedFlags: () -> Unit,
    private val loadProgress: () -> Unit
) {
    fun clearProgressAndReload() {
        scope.launch {
            sessionEngine.progressManager.clearProgress(progressId())
            val state = sessionState.value
            val reset = state.questionsWithState.map {
                it.copy(
                    selectedOptions = emptyList(),
                    textAnswer = "",
                    showResult = false,
                    analysis = "",
                    sparkAnalysis = "",
                    baiduAnalysis = "",
                    note = ""
                )
            }
            sessionState.value = state.copy(
                questionsWithState = reset,
                currentIndex = 0,
                finished = false,
                progressLoaded = false
            )
            setProgressSeed(System.currentTimeMillis())
            resetArtifactLoadedFlags()
            loadProgress()
        }
    }

    fun resetAllStates() {
        val state = sessionState.value
        val reset = state.questionsWithState.map {
            it.copy(
                selectedOptions = emptyList(),
                textAnswer = "",
                showResult = false,
                analysis = "",
                sparkAnalysis = "",
                baiduAnalysis = "",
                note = ""
            )
        }
        sessionState.value = state.copy(
            questionsWithState = reset,
            currentIndex = 0,
            finished = false,
            progressLoaded = true
        )
    }

    fun clearProgress() {
        scope.launch {
            sessionEngine.progressManager.clearProgress(progressId())
            sessionState.value = PracticeSessionState(finished = false)
            setProgressSeed(System.currentTimeMillis())
            resetArtifactLoadedFlags()
        }
    }
}
