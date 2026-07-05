package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.session.strategy.navigation.SessionAnsweredHistoryBrowseContextPipeline
import com.example.testapp.core.session.strategy.navigation.SessionAnsweredHistoryBrowsePipeline
import com.example.testapp.core.session.strategy.navigation.SessionAnsweredHistoryCommitPipeline
import com.example.testapp.core.session.strategy.navigation.SessionAnsweredHistoryTargetPipeline
import com.example.testapp.core.session.strategy.navigation.SessionFullAnswerSourcePoolPipeline
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.session.navigation.SessionAnsweredHistoryBrowseContext

object AnsweredHistorySwipeNavigator {
    fun navigateToPreviousAnsweredQuestion(
        history: NavigationHistory,
        currentState: PracticeSessionState,
        onUpdateSession: (PracticeSessionState) -> Unit,
        onSaveProgress: () -> Unit,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        fullAnswerModeActive: Boolean = false,
        readOnlyBrowse: Boolean = false,
    ): AnsweredHistoryBackwardResult {
        val currentNavMode = history.navigationState.mode
        val browseContext =
            resolveBrowseContext(
                history,
                currentState,
                currentNavMode,
                fullAnswerModeActive,
                effectiveCurrentMemoryRoundQuestionIds,
                memoryModeActive,
                memoryPoolMode,
            )
        val orderedIndices = browseContext.orderedIndices
        if (orderedIndices.isEmpty()) {
            NavigationHistoryDebugLog.logSwipe(
                "swipeRight",
                currentState,
                currentNavMode as? AnsweredHistoryNavigationState.Active,
                null,
                "NoMoreHistory",
                { state, index -> answerTimeAt(history, state, index) },
            )
            return AnsweredHistoryBackwardResult.NoMoreHistory
        }

        val currentIndex = currentState.currentIndex
        val targetIndex =
            SessionAnsweredHistoryTargetPipeline.resolveOlderTargetIndex(
                fullAnswerModeActive = fullAnswerModeActive,
                orderedIndices = orderedIndices,
                anchorPoolIndices = browseContext.anchorPoolIndices,
                currentIndex = currentIndex,
                activeHistoryPosition = browseContext.activeHistoryPosition,
            )

        if (targetIndex == null) {
            val stop =
                SessionAnsweredHistoryBrowsePipeline.resolveBackwardStopWhenNoTarget(
                    orderedIndices = orderedIndices,
                    currentIndex = currentIndex,
                    inActiveHistoryMode = browseContext.inActiveHistoryMode,
                )
            NavigationHistoryDebugLog.logSwipe(
                "swipeRight",
                currentState,
                currentNavMode as? AnsweredHistoryNavigationState.Active,
                null,
                if (stop == SessionAnsweredHistoryBrowsePipeline.BackwardStop.AtOldestAnswered) {
                    "AtOldest"
                } else {
                    "NoMoreHistory"
                },
                { state, index -> answerTimeAt(history, state, index) },
            )
            return when (stop) {
                SessionAnsweredHistoryBrowsePipeline.BackwardStop.AtOldestAnswered ->
                    AnsweredHistoryBackwardResult.AtOldestAnswered
                SessionAnsweredHistoryBrowsePipeline.BackwardStop.NoMoreHistory ->
                    AnsweredHistoryBackwardResult.NoMoreHistory
            }
        }

        if (readOnlyBrowse) {
            val update =
                SessionAnsweredHistoryCommitPipeline.navigationUpdate(
                    originIndex = browseContext.originIndex,
                    orderedIndices = orderedIndices,
                    targetIndex = targetIndex,
                    anchorPoolIndices = browseContext.anchorPoolIndices,
                )
            history.updateAnsweredHistoryNavigation(
                update.originIndex,
                update.historyPosition,
                update.orderedIndices,
                update.anchorPoolIndices,
            )
            PracticeAnsweredBrowseNavigation.navigateReadOnly(
                history.restoreAnsweredHistoryOverlays(currentState),
                targetIndex,
                onUpdateSession,
            )
        } else {
            commitHistoryNavigation(
                history,
                currentState,
                orderedIndices,
                targetIndex,
                browseContext.originIndex,
                browseContext.anchorPoolIndices,
                isQuestionAnswered,
                onUpdateSession,
            )
        }
        NavigationHistoryDebugLog.logSwipe(
            "swipeRight",
            currentState,
            history.navigationState.mode as? AnsweredHistoryNavigationState.Active,
            targetIndex,
            "Navigated",
            { state, index -> answerTimeAt(history, state, index) },
        )
        onSaveProgress()
        return AnsweredHistoryBackwardResult.Navigated
    }

    fun navigateToNextAnsweredInHistory(
        history: NavigationHistory,
        currentState: PracticeSessionState,
        onUpdateSession: (PracticeSessionState) -> Unit,
        onSaveProgress: () -> Unit,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        fullAnswerModeActive: Boolean = false,
    ): AnsweredHistoryForwardResult {
        val currentNavMode = history.navigationState.mode
        val browseContext =
            resolveBrowseContext(
                history,
                currentState,
                currentNavMode,
                fullAnswerModeActive,
                effectiveCurrentMemoryRoundQuestionIds,
                memoryModeActive,
                memoryPoolMode,
            )
        val orderedIndices = browseContext.orderedIndices
        if (orderedIndices.isEmpty()) {
            NavigationHistoryDebugLog.logSwipe(
                "swipeLeft",
                currentState,
                currentNavMode as? AnsweredHistoryNavigationState.Active,
                null,
                "NotInHistory",
                { state, index -> answerTimeAt(history, state, index) },
            )
            return AnsweredHistoryForwardResult.NotInHistory
        }

        val currentIndex = currentState.currentIndex
        val targetIndex =
            SessionAnsweredHistoryTargetPipeline.resolveNewerTargetIndex(
                fullAnswerModeActive = fullAnswerModeActive,
                orderedIndices = orderedIndices,
                anchorPoolIndices = browseContext.anchorPoolIndices,
                currentIndex = currentIndex,
                activeHistoryPosition = browseContext.activeHistoryPosition,
            )

        if (targetIndex == null) {
            if (SessionAnsweredHistoryCommitPipeline.shouldResumeLiveOnForwardMiss(browseContext.inActiveHistoryMode)) {
                onUpdateSession(history.resumeFromAnsweredHistory(currentState))
                onSaveProgress()
                NavigationHistoryDebugLog.logSwipe(
                    "swipeLeft",
                    currentState,
                    currentNavMode as? AnsweredHistoryNavigationState.Active,
                    null,
                    "ResumeLive",
                    { state, index -> answerTimeAt(history, state, index) },
                )
                return AnsweredHistoryForwardResult.Navigated
            }
            NavigationHistoryDebugLog.logSwipe(
                "swipeLeft",
                currentState,
                null,
                null,
                "AtLatest",
                { state, index -> answerTimeAt(history, state, index) },
            )
            return AnsweredHistoryForwardResult.AtLatestAnswered
        }

        commitHistoryNavigation(
            history,
            currentState,
            orderedIndices,
            targetIndex,
            browseContext.originIndex,
            browseContext.anchorPoolIndices,
            isQuestionAnswered,
            onUpdateSession,
        )
        NavigationHistoryDebugLog.logSwipe(
            "swipeLeft",
            currentState,
            history.navigationState.mode as? AnsweredHistoryNavigationState.Active,
            targetIndex,
            "Navigated",
            { state, index -> answerTimeAt(history, state, index) },
        )
        onSaveProgress()
        return AnsweredHistoryForwardResult.Navigated
    }

    fun answerTimeAt(
        history: NavigationHistory,
        state: PracticeSessionState,
        index: Int,
    ): Long {
        val qws = state.questionsWithState.getOrNull(index) ?: return 0L
        return history.historySnapshotFor(qws)?.sessionAnswerTime ?: qws.sessionAnswerTime
    }

    private fun resolveBrowseContext(
        history: NavigationHistory,
        currentState: PracticeSessionState,
        currentNavMode: AnsweredHistoryNavigationState,
        fullAnswerModeActive: Boolean,
        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
        memoryModeActive: Boolean,
        memoryPoolMode: Int,
    ): SessionAnsweredHistoryBrowseContext {
        val active = currentNavMode as? AnsweredHistoryNavigationState.Active
        return SessionAnsweredHistoryBrowseContextPipeline.resolve(
            liveCurrentIndex = currentState.currentIndex,
            activeOriginIndex = active?.originIndex,
            activeHistoryPosition = active?.historyPosition,
            activeOrderedIndices = active?.orderedIndices,
            activeAnchorPoolIndices = active?.anchorPoolIndices,
            fullAnswerModeActive = fullAnswerModeActive,
            idleSourcePoolIndices =
                SessionFullAnswerSourcePoolPipeline.sourcePoolIndices(
                    currentState.questions,
                    currentState.currentIndex,
                ),
            idleOrderedIndices =
                history.buildPreviousAnsweredIndices(
                    currentState,
                    effectiveCurrentMemoryRoundQuestionIds,
                    memoryModeActive,
                    memoryPoolMode,
                ),
        )
    }

    private fun commitHistoryNavigation(
        history: NavigationHistory,
        currentState: PracticeSessionState,
        orderedIndices: List<Int>,
        targetIndex: Int,
        originIndex: Int,
        anchorPoolIndices: Set<Int>,
        isQuestionAnswered: (QuestionWithState) -> Boolean,
        onUpdateSession: (PracticeSessionState) -> Unit,
    ) {
        val update =
            SessionAnsweredHistoryCommitPipeline.navigationUpdate(
                originIndex = originIndex,
                orderedIndices = orderedIndices,
                targetIndex = targetIndex,
                anchorPoolIndices = anchorPoolIndices,
            )
        history.updateAnsweredHistoryNavigation(
            update.originIndex,
            update.historyPosition,
            update.orderedIndices,
            update.anchorPoolIndices,
        )
        val restoredState = history.restoreAnsweredHistoryOverlays(currentState)
        onUpdateSession(
            history.applyAnsweredHistorySnapshot(
                restoredState,
                targetIndex,
                isQuestionAnswered,
                preferSnapshot = true,
            ),
        )
    }
}
