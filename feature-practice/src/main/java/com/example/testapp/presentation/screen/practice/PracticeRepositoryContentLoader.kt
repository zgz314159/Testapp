package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.session.SessionEngine
import com.example.testapp.domain.model.PracticeSessionState
import kotlinx.coroutines.flow.MutableStateFlow

class PracticeRepositoryContentLoader(
    private val sessionEngine: SessionEngine,
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val saveProgress: () -> Unit
) {
    private var analysisLoaded = false
    private var sparkAnalysisLoaded = false
    private var baiduAnalysisLoaded = false
    private var notesLoaded = false

    fun reset() {
        analysisLoaded = false
        sparkAnalysisLoaded = false
        baiduAnalysisLoaded = false
        notesLoaded = false
    }

    suspend fun loadOnce() {
        if (!analysisLoaded) {
            loadAnalysis { sessionEngine.analysisLoader.loadAnalysis(it) }
            analysisLoaded = true
        }
        if (!sparkAnalysisLoaded) {
            loadAnalysis { sessionEngine.analysisLoader.loadSparkAnalysis(it) }
            sparkAnalysisLoaded = true
        }
        if (!baiduAnalysisLoaded) {
            loadAnalysis { sessionEngine.analysisLoader.loadBaiduAnalysis(it) }
            baiduAnalysisLoaded = true
        }
        if (!notesLoaded) {
            val current = sessionState.value.questionsWithState
            val changed = sessionEngine.analysisLoader.loadNotes(current)
            if (changed != current) sessionState.value = sessionState.value.copy(questionsWithState = changed)
            notesLoaded = true
        }
    }

    private suspend fun loadAnalysis(
        loader:
        suspend (List<com.example.testapp.domain.model.QuestionWithState>) -> List<com.example.testapp.domain.model.QuestionWithState>
    ) {
        val current = sessionState.value.questionsWithState
        val changed = loader(current)
        if (changed != current) {
            sessionState.value = sessionState.value.copy(questionsWithState = changed)
            saveProgress()
        }
    }
}
