package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Derived [StateFlow]s from unified [PracticeSessionState]. */
internal class ExamViewModelSessionFlows private constructor(
    sessionState: StateFlow<PracticeSessionState>,
    scope: CoroutineScope
) {
    val questions: StateFlow<List<Question>> = sessionState.map { it.questions }
        .stateIn(scope, SharingStarted.Lazily, emptyList())
    val currentIndex: StateFlow<Int> = sessionState.map { it.currentIndex }
        .stateIn(scope, SharingStarted.Lazily, 0)
    val selectedOptions: StateFlow<List<List<Int>>> = sessionState.map { s ->
        s.questionsWithState.map { it.selectedOptions }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    val textAnswers: StateFlow<List<String>> = sessionState.map { s ->
        s.questionsWithState.map { it.textAnswer }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    val showResultList: StateFlow<List<Boolean>> = sessionState.map { s ->
        s.questionsWithState.map { it.showResult }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    val answerTimeList: StateFlow<List<Long>> = sessionState.map { s ->
        s.questionsWithState.map { it.sessionAnswerTime }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    val analysisList: StateFlow<List<String>> = sessionState.map { s ->
        s.questionsWithState.map { it.analysis }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    val sparkAnalysisList: StateFlow<List<String>> = sessionState.map { s ->
        s.questionsWithState.map { it.sparkAnalysis }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    val baiduAnalysisList: StateFlow<List<String>> = sessionState.map { s ->
        s.questionsWithState.map { it.baiduAnalysis }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    val noteList: StateFlow<List<String>> = sessionState.map { s ->
        s.questionsWithState.map { it.note }
    }.stateIn(scope, SharingStarted.Lazily, emptyList())
    val progressLoaded: StateFlow<Boolean> = sessionState.map { it.progressLoaded }
        .stateIn(scope, SharingStarted.Lazily, false)
    val finished: StateFlow<Boolean> = sessionState.map { it.finished }
        .stateIn(scope, SharingStarted.Lazily, false)

    companion object {
        fun create(sessionState: StateFlow<PracticeSessionState>, scope: CoroutineScope) =
            ExamViewModelSessionFlows(sessionState, scope)
    }
}
