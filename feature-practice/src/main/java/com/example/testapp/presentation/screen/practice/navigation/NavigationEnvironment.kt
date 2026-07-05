package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationGate
import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationResolver
import com.example.testapp.core.util.FullAnswerIconNavigationStrategyPipeline
import com.example.testapp.core.util.FullAnswerIconTapStrategy
import com.example.testapp.core.util.FullAnswerMultiRoundSessionPipeline
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.session.navigation.SessionNavigationOrchestration
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconNavOrderPipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/** Shared navigation dependencies and stateless helpers for practice navigation handlers. */
internal class NavigationEnvironment(
    val sessionState: MutableStateFlow<PracticeSessionState>,
    val scope: CoroutineScope,
    val history: NavigationHistory,
    val isQuestionPendingForCurrentMode: (QuestionWithState) -> Boolean,
    val isQuestionAnswered: (QuestionWithState) -> Boolean,
    val shouldReopenUnansweredReveal: (QuestionWithState) -> Boolean,
    val currentSourcePendingIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    val isCurrentSourceComplete: (PracticeSessionState) -> Boolean,
    val findNextSourceEntryIndices: (PracticeSessionState) -> List<Int>,
    val findAdjacentDerivedQuestionIndex: (PracticeSessionState, Boolean) -> Int?,
    val effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
    val nextFullAnswerCandidateIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    val reopenQuestionForPendingRetry: (Int) -> Unit,
    val reopenQuestionForFullAnswerRetry: (Int) -> Unit,
    val scheduleNavigationSave: () -> Unit,
    val fullAnswerModeActive: () -> Boolean,
    val fullAnswerRequireCorrect: () -> Boolean,
    val fullAnswerRandomOrder: () -> Boolean,
    val memoryModeActive: () -> Boolean,
    val randomPracticeEnabled: () -> Boolean,
    val navigationOrchestration: () -> SessionNavigationOrchestration? = { null },
) {
    fun iconNavRandomOrder(): Boolean = PracticeFullAnswerIconNavOrderPipeline.usesRandomOrder(
        fullAnswerModeActive = fullAnswerModeActive(),
        fullAnswerRandomOrder = fullAnswerRandomOrder(),
        randomPractice = randomPracticeEnabled()
    )

    fun iconTapStrategy(): FullAnswerIconTapStrategy =
        FullAnswerIconNavigationStrategyPipeline.resolve(
            fullAnswerModeActive = fullAnswerModeActive(),
            multiRoundSession = FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(
                sessionState.value.questions
            )
        )

    fun effectiveOrchestration(): SessionNavigationOrchestration =
        navigationOrchestration() ?: SessionNavigationOrchestrationResolver.practiceDefault()

    fun prepareStateForUnansweredIconNav(currentState: PracticeSessionState): PracticeSessionState {
        val orch = effectiveOrchestration()
        return if (
            SessionNavigationOrchestrationGate.shouldExitAnsweredHistoryBeforeIconNav(
                orch,
                history.isInAnsweredHistory,
            )
        ) {
            history.exitAnsweredHistoryBrowsing(currentState)
        } else {
            currentState
        }
    }

    fun usesMultiRoundIconNav(state: PracticeSessionState): Boolean =
        fullAnswerModeActive() &&
            FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(state.questions)

    fun isPendingAt(qws: QuestionWithState): Boolean = isQuestionPendingForCurrentMode(qws)
}
