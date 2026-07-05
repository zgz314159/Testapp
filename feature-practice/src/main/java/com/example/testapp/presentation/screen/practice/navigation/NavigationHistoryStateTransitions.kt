package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.session.strategy.navigation.SessionRandomNavigationHistoryPipeline

/** NavigationHistory 导航状态转换（从 NavigationHistory 拆出） */
object NavigationHistoryStateTransitions {
    fun clearRandom(state: PracticeNavigationState): PracticeNavigationState =
        state.copy(randomHistory = RandomNavigationHistoryState())

    fun clearAnsweredHistory(state: PracticeNavigationState): PracticeNavigationState =
        state.copy(mode = AnsweredHistoryNavigationState.Idle)

    fun activateAnsweredHistory(
        state: PracticeNavigationState,
        originIndex: Int,
        historyPosition: Int,
        orderedIndices: List<Int>,
        anchorPoolIndices: Set<Int> = emptySet(),
    ): PracticeNavigationState =
        state.copy(
            mode =
                AnsweredHistoryNavigationState.Active(
                    originIndex = originIndex,
                    historyPosition = historyPosition,
                    orderedIndices = orderedIndices,
                    anchorPoolIndices = anchorPoolIndices,
                ),
        )

    fun appendRandomOrigin(
        state: PracticeNavigationState,
        currentIndex: Int,
    ): PracticeNavigationState? {
        val history = state.randomHistory.history
        if (!SessionRandomNavigationHistoryPipeline.shouldAppendOrigin(history, currentIndex)) return null
        return state.copy(
            randomHistory =
                state.randomHistory.copy(
                    history = SessionRandomNavigationHistoryPipeline.appendedHistory(history, currentIndex),
                ),
        )
    }

    fun seedRandomHistory(
        state: PracticeNavigationState,
        seededIndices: List<Int>,
    ): PracticeNavigationState = state.copy(randomHistory = RandomNavigationHistoryState(history = seededIndices))
}
