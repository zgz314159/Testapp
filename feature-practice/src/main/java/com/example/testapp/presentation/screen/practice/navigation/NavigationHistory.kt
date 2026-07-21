package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.session.strategy.navigation.SessionRandomNavigationHistoryPipeline
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState

class NavigationHistory {
    var navigationState: PracticeNavigationState = PracticeNavigationState()

    val answeredHistorySnapshots = mutableMapOf<Int, QuestionWithState>()

    val answeredHistoryOriginalStates = mutableMapOf<Int, QuestionWithState>()

    val isInAnsweredHistory: Boolean
        get() = navigationState.mode is AnsweredHistoryNavigationState.Active

    fun rememberAnsweredHistorySnapshot(questionWithState: QuestionWithState) {
        NavigationHistorySnapshots.remember(answeredHistorySnapshots, questionWithState)
    }

    fun historySnapshotFor(questionWithState: QuestionWithState): QuestionWithState? =
        NavigationHistorySnapshots.resolveForBrowse(answeredHistorySnapshots, questionWithState)

    fun restoreAnsweredHistoryOverlays(currentState: PracticeSessionState): PracticeSessionState =
        NavigationHistorySnapshots.restoreOverlays(answeredHistoryOriginalStates, currentState)

    fun applyAnsweredHistorySnapshot(
        currentState: PracticeSessionState,
        index: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        preferSnapshot: Boolean = false,
    ): PracticeSessionState =
        NavigationHistorySnapshots.apply(
            snapshots = answeredHistorySnapshots,
            originals = answeredHistoryOriginalStates,
            currentState = currentState,
            index = index,
            isQuestionAnswered = isQuestionAnswered,
            preferSnapshot = preferSnapshot,
        )

    fun clearRandomNavigationState() {
        navigationState = navigationState.copy(randomHistory = RandomNavigationHistoryState())
    }

    fun clearAnsweredHistoryNavigation() {
        navigationState = navigationState.copy(mode = AnsweredHistoryNavigationState.Idle)
    }

    fun clearAll() {
        restoreAnsweredHistoryIfNeeded()
        clearRandomNavigationState()
        clearAnsweredHistoryNavigation()
    }

    fun restoreAnsweredHistoryIfNeeded() {}

    fun updateAnsweredHistoryNavigation(
        originIndex: Int,
        historyPosition: Int,
        orderedIndices: List<Int>,
        anchorPoolIndices: Set<Int> = emptySet(),
    ) {
        navigationState =
            navigationState.copy(
                mode =
                    AnsweredHistoryNavigationState.Active(
                        originIndex = originIndex,
                        historyPosition = historyPosition,
                        orderedIndices = orderedIndices,
                        anchorPoolIndices = anchorPoolIndices,
                    ),
            )
    }

    fun resumeFromAnsweredHistory(currentState: PracticeSessionState): PracticeSessionState {
        val mode = navigationState.mode as? AnsweredHistoryNavigationState.Active
            ?: return currentState
        val originIndex = mode.originIndex.takeIf { it in currentState.questionsWithState.indices }
            ?: currentState.currentIndex
        val restoredState = restoreAnsweredHistoryOverlays(currentState)
        clearAnsweredHistoryNavigation()
        return if (originIndex != restoredState.currentIndex) {
            restoredState.copy(currentIndex = originIndex)
        } else {
            restoredState
        }
    }

    fun exitAnsweredHistoryBrowsing(currentState: PracticeSessionState): PracticeSessionState {
        if (navigationState.mode !is AnsweredHistoryNavigationState.Active) return currentState
        val restored = restoreAnsweredHistoryOverlays(currentState)
        clearAnsweredHistoryNavigation()
        return restored
    }

    fun prepareStateForForwardNavigation(currentState: PracticeSessionState): PracticeSessionState =
        when (navigationState.mode) {
            is AnsweredHistoryNavigationState.Active -> resumeFromAnsweredHistory(currentState)
            AnsweredHistoryNavigationState.Idle -> currentState
        }

    fun buildPreviousAnsweredIndices(
        currentState: PracticeSessionState,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
    ): List<Int> {
        val qws = currentState.questionsWithState
        val answeredByTimeDesc =
            PracticeAnsweredBrowseNavigation.buildSwipeHistoryIndices(
                questionsWithState = qws,
                resolveSnapshot = ::historySnapshotFor,
            )
        return PracticeAnsweredBrowseNavigation.applyMemoryRoundPriority(
            answeredByTimeDesc = answeredByTimeDesc,
            questionsWithState = qws,
            roundIds = effectiveCurrentMemoryRoundQuestionIds(qws),
            memoryModeActive = memoryModeActive,
            memoryPoolMode = memoryPoolMode,
        ).also { ordered ->
        }
    }

    fun recordRandomNavigationOrigin(currentIndex: Int, randomPracticeEnabled: Boolean) {
        if (!SessionRandomNavigationHistoryPipeline.shouldTrack(randomPracticeEnabled)) return
        val history = navigationState.randomHistory.history
        if (!SessionRandomNavigationHistoryPipeline.shouldAppendOrigin(history, currentIndex)) return
        navigationState =
            navigationState.copy(
                randomHistory =
                    navigationState.randomHistory.copy(
                        history =
                            SessionRandomNavigationHistoryPipeline.appendedHistory(
                                history,
                                currentIndex,
                            ),
                    ),
            )
    }

    fun seedRandomNavigationHistory(
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        randomPracticeEnabled: Boolean,
    ) {
        if (!SessionRandomNavigationHistoryPipeline.shouldTrack(randomPracticeEnabled)) return
        clearRandomNavigationState()
        val seededHistory =
            SessionRandomNavigationHistoryPipeline.seedHistoryIndices(
                questionsWithState = questionsWithState,
                currentIndex = currentIndex,
                isQuestionAnswered = isQuestionAnswered,
            )
        navigationState =
            navigationState.copy(
                randomHistory = RandomNavigationHistoryState(history = seededHistory),
            )
    }

    fun navigateToPreviousAnsweredQuestion(
        currentState: PracticeSessionState,
        onUpdateSession: (PracticeSessionState) -> Unit,
        onSaveProgress: () -> Unit,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        fullAnswerModeActive: Boolean = false,
        readOnlyBrowse: Boolean = false,
    ): AnsweredHistoryBackwardResult =
        AnsweredHistorySwipeNavigator.navigateToPreviousAnsweredQuestion(
            history = this,
            currentState = currentState,
            onUpdateSession = onUpdateSession,
            onSaveProgress = onSaveProgress,
            effectiveCurrentMemoryRoundQuestionIds = effectiveCurrentMemoryRoundQuestionIds,
            memoryModeActive = memoryModeActive,
            memoryPoolMode = memoryPoolMode,
            isQuestionAnswered = isQuestionAnswered,
            fullAnswerModeActive = fullAnswerModeActive,
            readOnlyBrowse = readOnlyBrowse,
        )

    fun navigateToNextAnsweredInHistory(
        currentState: PracticeSessionState,
        onUpdateSession: (PracticeSessionState) -> Unit,
        onSaveProgress: () -> Unit,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        fullAnswerModeActive: Boolean = false,
    ): AnsweredHistoryForwardResult =
        AnsweredHistorySwipeNavigator.navigateToNextAnsweredInHistory(
            history = this,
            currentState = currentState,
            onUpdateSession = onUpdateSession,
            onSaveProgress = onSaveProgress,
            effectiveCurrentMemoryRoundQuestionIds = effectiveCurrentMemoryRoundQuestionIds,
            memoryModeActive = memoryModeActive,
            memoryPoolMode = memoryPoolMode,
            isQuestionAnswered = isQuestionAnswered,
            fullAnswerModeActive = fullAnswerModeActive,
        )

    internal fun answerTimeAt(
        state: PracticeSessionState,
        index: Int,
    ): Long = AnsweredHistorySwipeNavigator.answerTimeAt(this, state, index)

    companion object {
        const val MEMORY_POOL_MODE_ROUND = 1
    }
}
