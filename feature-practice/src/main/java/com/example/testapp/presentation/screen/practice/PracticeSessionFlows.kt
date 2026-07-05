package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.AnswerStatus
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.uicommon.model.QuestionUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Derived [StateFlow]s from practice [PracticeSessionState]. */
internal class PracticeSessionFlows private constructor(
    sessionState: StateFlow<PracticeSessionState>,
    scope: CoroutineScope,
) {
    val questions: StateFlow<List<Question>> =
        sessionState.map { it.questions }
            .stateIn(scope, SharingStarted.Lazily, emptyList())

    val uiQuestions: StateFlow<List<QuestionUiModel>> =
        sessionState.map { state ->
            state.questionsWithState.map { questionWithState ->
                QuestionUiModel(
                    question = questionWithState.question,
                    status =
                        when {
                            !questionWithState.isAnswered -> AnswerStatus.UNANSWERED
                            !questionWithState.showResult -> AnswerStatus.UNANSWERED
                            questionWithState.isCorrect == true -> AnswerStatus.CORRECT
                            else -> AnswerStatus.INCORRECT
                        },
                    selectedOptions = questionWithState.selectedOptions,
                )
            }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val currentIndex: StateFlow<Int> =
        sessionState.map { it.currentIndex }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Lazily, 0)

    val sessionAnsweredCountFlow: StateFlow<Int> =
        sessionState.map { it.sessionAnsweredCount }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Lazily, 0)

    val sessionCorrectCountFlow: StateFlow<Int> =
        sessionState.map { it.sessionCorrectCount }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Lazily, 0)

    val hasAnyInputInSessionFlow: StateFlow<Boolean> =
        sessionState
            .map { PracticeSessionInputPipeline.hasAnyInput(it.questionsWithState) }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Lazily, false)

    val sessionInputCountFlow: StateFlow<Int> =
        sessionState
            .map { PracticeSessionInputPipeline.inputCount(it.questionsWithState) }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Lazily, 0)

    val currentQuestionUi: StateFlow<PracticeCurrentQuestionUi?> =
        sessionState
            .map { PracticeCurrentQuestionUiPipeline.snapshot(it) }
            .distinctUntilChanged()
            .stateIn(scope, SharingStarted.Lazily, null)

    val answeredList: StateFlow<List<Int>> =
        sessionState.map { it.answeredIndices }
            .stateIn(scope, SharingStarted.Lazily, emptyList())

    val selectedOptions: StateFlow<List<List<Int>>> =
        sessionState.map { state ->
            state.questionsWithState.map { it.selectedOptions }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val progressLoaded: StateFlow<Boolean> =
        sessionState.map { it.progressLoaded }
            .stateIn(scope, SharingStarted.Lazily, false)

    val showResultList: StateFlow<List<Boolean>> =
        sessionState.map { state ->
            state.questionsWithState.map { it.showResult }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val analysisList: StateFlow<List<String>> =
        sessionState.map { state ->
            state.questionsWithState.map { it.analysis }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val sparkAnalysisList: StateFlow<List<String>> =
        sessionState.map { state ->
            state.questionsWithState.map { it.sparkAnalysis }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val baiduAnalysisList: StateFlow<List<String>> =
        sessionState.map { state ->
            state.questionsWithState.map { it.baiduAnalysis }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val noteList: StateFlow<List<String>> =
        sessionState.map { state ->
            state.questionsWithState.map { it.note }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    val textAnswers: StateFlow<List<String>> =
        sessionState.map { state ->
            state.questionsWithState.map { it.textAnswer }
        }.stateIn(scope, SharingStarted.Lazily, emptyList())

    companion object {
        fun create(
            sessionState: StateFlow<PracticeSessionState>,
            scope: CoroutineScope,
        ) = PracticeSessionFlows(sessionState, scope)
    }
}
