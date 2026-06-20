package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState

/**
 * Navigation History Management — history snapshots + answered history state transitions.
 * Extracted from PracticeNavigationCoordinator.
 */
class NavigationHistory {

    var navigationState: PracticeNavigationState = PracticeNavigationState()

    val answeredHistorySnapshots = mutableMapOf<Int, QuestionWithState>()
    val answeredHistoryOriginalStates = mutableMapOf<Int, QuestionWithState>()

    val isInAnsweredHistory: Boolean
        get() = navigationState.mode is AnsweredHistoryNavigationState.Active

    // ========================================================================
    // History Snapshot Methods
    // ========================================================================

    fun rememberAnsweredHistorySnapshot(questionWithState: QuestionWithState) {
        if (!questionWithState.showResult) return
        val questionId = questionWithState.question.id
        val existingSnapshot = answeredHistorySnapshots[questionId]
        if (existingSnapshot == null ||
            questionWithState.sessionAnswerTime >= existingSnapshot.sessionAnswerTime
        ) {
            answeredHistorySnapshots[questionId] = questionWithState
        }
    }

    fun historySnapshotFor(questionWithState: QuestionWithState): QuestionWithState? {
        if (questionWithState.sessionAnswerTime > 0L &&
            (questionWithState.showResult || hasAnswerContent(questionWithState))
        ) {
            return questionWithState
        }
        return answeredHistorySnapshots[questionWithState.question.id]
    }

    fun restoreAnsweredHistoryOverlays(currentState: PracticeSessionState): PracticeSessionState {
        if (answeredHistoryOriginalStates.isEmpty()) return currentState
        val originalsByQuestionId = answeredHistoryOriginalStates.toMap()
        answeredHistoryOriginalStates.clear()
        var changed = false
        val restored = currentState.questionsWithState.map { qws ->
            val originalState = originalsByQuestionId[qws.question.id]
            if (originalState != null) { changed = true; originalState }
            else qws
        }
        return if (changed) currentState.copy(questionsWithState = restored) else currentState
    }

    fun applyAnsweredHistorySnapshot(
        currentState: PracticeSessionState,
        index: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean
    ): PracticeSessionState {
        val liveQuestionState = currentState.questionsWithState.getOrNull(index) ?: return currentState
        val snapshot = answeredHistorySnapshots[liveQuestionState.question.id]
            ?: return currentState.copy(currentIndex = index)
        if (liveQuestionState.showResult && isQuestionAnswered(liveQuestionState)) {
            return currentState.copy(currentIndex = index)
        }
        answeredHistoryOriginalStates.putIfAbsent(liveQuestionState.question.id, liveQuestionState)
        val updated = currentState.questionsWithState.toMutableList()
        updated[index] = snapshot
        return currentState.copy(currentIndex = index, questionsWithState = updated)
    }

    // ========================================================================
    // Navigation State Transitions
    // ========================================================================

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

    fun updateAnsweredHistoryNavigation(originIndex: Int, historyPosition: Int) {
        navigationState = navigationState.copy(
            mode = AnsweredHistoryNavigationState.Active(
                originIndex = originIndex, historyPosition = historyPosition
            )
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
        } else restoredState
    }

    fun prepareStateForForwardNavigation(currentState: PracticeSessionState): PracticeSessionState {
        return when (navigationState.mode) {
            is AnsweredHistoryNavigationState.Active -> resumeFromAnsweredHistory(currentState)
            AnsweredHistoryNavigationState.Idle -> currentState
        }
    }

    // ========================================================================
    // Answered History Index Building
    // ========================================================================

    fun buildPreviousAnsweredIndices(
        currentState: PracticeSessionState,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int
    ): List<Int> {
        val qws = currentState.questionsWithState
        val answeredByTimeDesc = qws
            .mapIndexedNotNull { index, questionWithState ->
                historySnapshotFor(questionWithState)?.let { snapshot ->
                    index to snapshot.sessionAnswerTime
                }
            }
            .filter { (_, t) -> t > 0L }
            .sortedWith(
                compareByDescending<Pair<Int, Long>> { if (it.second > 0L) it.second else Long.MIN_VALUE }
                    .thenByDescending { it.first }
            )
            .map { it.first }

        val roundIds = effectiveCurrentMemoryRoundQuestionIds(qws)
        val currentRoundAnswered = if (memoryModeActive && memoryPoolMode == MEMORY_POOL_MODE_ROUND && roundIds.isNotEmpty()) {
            answeredByTimeDesc.filter { index -> qws[index].question.id in roundIds }
        } else emptyList()

        if (currentRoundAnswered.isEmpty()) return answeredByTimeDesc

        val other = answeredByTimeDesc.filterNot { index -> qws[index].question.id in roundIds }
        return (currentRoundAnswered + other).distinct()
    }

    fun navigateToPreviousAnsweredQuestion(
        currentState: PracticeSessionState,
        onUpdateSession: (PracticeSessionState) -> Unit,
        onSaveProgress: () -> Unit,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean
    ): Boolean {
        val answeredByTimeDesc = buildPreviousAnsweredIndices(
            currentState, effectiveCurrentMemoryRoundQuestionIds, memoryModeActive, memoryPoolMode
        )
        if (answeredByTimeDesc.isEmpty()) return false

        val currentNavMode = navigationState.mode
        val targetPosition = if (currentNavMode is AnsweredHistoryNavigationState.Active) {
            currentNavMode.historyPosition + 1
        } else {
            val firstHistoryIndex = answeredByTimeDesc.firstOrNull { it != currentState.currentIndex } ?: return false
            answeredByTimeDesc.indexOf(firstHistoryIndex)
        }

        if (targetPosition !in answeredByTimeDesc.indices) {
            return currentNavMode is AnsweredHistoryNavigationState.Active
        }

        val originIndex = when (currentNavMode) {
            is AnsweredHistoryNavigationState.Active -> currentNavMode.originIndex
            AnsweredHistoryNavigationState.Idle -> currentState.currentIndex
        }
        val restoredState = restoreAnsweredHistoryOverlays(currentState)
        val targetIndex = answeredByTimeDesc[targetPosition]
        updateAnsweredHistoryNavigation(originIndex = originIndex, historyPosition = targetPosition)
        onUpdateSession(applyAnsweredHistorySnapshot(restoredState, targetIndex, isQuestionAnswered))
        onSaveProgress()
        return true
    }

    fun recordRandomNavigationOrigin(currentIndex: Int, randomPracticeEnabled: Boolean) {
        if (!randomPracticeEnabled) return
        val history = navigationState.randomHistory.history
        if (history.lastOrNull() != currentIndex) {
            navigationState = navigationState.copy(
                randomHistory = navigationState.randomHistory.copy(history = history + currentIndex)
            )
        }
    }

    fun seedRandomNavigationHistory(
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        randomPracticeEnabled: Boolean
    ) {
        if (!randomPracticeEnabled) return
        clearRandomNavigationState()
        val seededHistory = questionsWithState
            .mapIndexedNotNull { index, qws ->
                if (index == currentIndex || !isQuestionAnswered(qws)) null
                else Triple(index, qws.sessionAnswerTime, qws.showResult)
            }
            .sortedWith(
                compareBy<Triple<Int, Long, Boolean>>(
                    { if (it.second > 0L) 0 else 1 },
                    { it.second },
                    { it.first }
                )
            )
            .map { (index, _, _) -> index }
        navigationState = navigationState.copy(
            randomHistory = RandomNavigationHistoryState(history = seededHistory)
        )
    }

    companion object {
        const val MEMORY_POOL_MODE_ROUND = 1
    }
}
