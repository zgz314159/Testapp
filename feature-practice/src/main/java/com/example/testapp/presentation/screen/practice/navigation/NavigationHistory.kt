package com.example.testapp.presentation.screen.practice.navigation



import android.util.Log

import com.example.testapp.domain.model.PracticeSessionState

import com.example.testapp.domain.model.QuestionWithState

import com.example.testapp.domain.review.AnsweredBrowseOrder

import com.example.testapp.presentation.screen.practice.PracticeFullAnswerHistoryNavigation



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

        isQuestionAnswered: (QuestionWithState) -> Boolean,

        preferSnapshot: Boolean = false

    ): PracticeSessionState {

        val liveQuestionState = currentState.questionsWithState.getOrNull(index) ?: return currentState

        val snapshot = answeredHistorySnapshots[liveQuestionState.question.id]

            ?: return currentState.copy(currentIndex = index)

        if (!preferSnapshot && liveQuestionState.showResult && isQuestionAnswered(liveQuestionState)) {

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



    fun updateAnsweredHistoryNavigation(

        originIndex: Int,

        historyPosition: Int,

        orderedIndices: List<Int>,

        anchorPoolIndices: Set<Int> = emptySet()

    ) {

        navigationState = navigationState.copy(

            mode = AnsweredHistoryNavigationState.Active(

                originIndex = originIndex,

                historyPosition = historyPosition,

                orderedIndices = orderedIndices,

                anchorPoolIndices = anchorPoolIndices

            )

        )

    }



    fun resumeFromAnsweredHistory(currentState: PracticeSessionState): PracticeSessionState {

        val mode = navigationState.mode as? AnsweredHistoryNavigationState.Active

            ?: return currentState

        logHistory("resumeFromHistory", currentState, mode, null, null)

        val originIndex = mode.originIndex.takeIf { it in currentState.questionsWithState.indices }

            ?: currentState.currentIndex

        val restoredState = restoreAnsweredHistoryOverlays(currentState)

        clearAnsweredHistoryNavigation()

        return if (originIndex != restoredState.currentIndex) {

            restoredState.copy(currentIndex = originIndex)

        } else restoredState

    }



    fun exitAnsweredHistoryBrowsing(currentState: PracticeSessionState): PracticeSessionState {

        if (navigationState.mode !is AnsweredHistoryNavigationState.Active) return currentState

        val restored = restoreAnsweredHistoryOverlays(currentState)

        clearAnsweredHistoryNavigation()

        return restored

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

        val answeredByTimeDesc = PracticeAnsweredBrowseNavigation.buildSwipeHistoryIndices(

            questionsWithState = qws,

            resolveSnapshot = ::historySnapshotFor

        )

        return PracticeAnsweredBrowseNavigation.applyMemoryRoundPriority(
            answeredByTimeDesc = answeredByTimeDesc,
            questionsWithState = qws,
            roundIds = effectiveCurrentMemoryRoundQuestionIds(qws),
            memoryModeActive = memoryModeActive,
            memoryPoolMode = memoryPoolMode
        ).also { ordered ->
            Log.d(
                TAG,
                "buildOrdered | size=${ordered.size} | " +
                    PracticeFullAnswerHistoryNavigation.formatOrderedDebugLine(
                        ordered, currentState.questions, { answerTimeAt(currentState, it) }
                    )
            )
        }
    }



    private fun answerTimeAt(state: PracticeSessionState, index: Int): Long {

        val qws = state.questionsWithState.getOrNull(index) ?: return 0L

        return historySnapshotFor(qws)?.sessionAnswerTime ?: qws.sessionAnswerTime

    }



    private fun logHistory(

        action: String,

        state: PracticeSessionState,

        mode: AnsweredHistoryNavigationState.Active?,

        targetIndex: Int?,

        result: String?

    ) {

        val ordered = mode?.orderedIndices ?: emptyList()

        val debugLine = PracticeFullAnswerHistoryNavigation.formatOrderedDebugLine(

            orderedIndices = ordered,

            questions = state.questions,

            resolveAnswerTime = { answerTimeAt(state, it) }

        )

        Log.d(

            TAG,

            "$action | result=$result | currentIdx=${state.currentIndex} | " +

                "origin=${mode?.originIndex} | pos=${mode?.historyPosition} | " +

                "pool=${mode?.anchorPoolIndices} | ordered=$debugLine | target=$targetIndex"

        )

    }



    private fun commitHistoryNavigation(

        currentState: PracticeSessionState,

        orderedIndices: List<Int>,

        targetIndex: Int,

        originIndex: Int,

        anchorPoolIndices: Set<Int>,

        isQuestionAnswered: (QuestionWithState) -> Boolean,

        onUpdateSession: (PracticeSessionState) -> Unit

    ) {

        val targetPosition = orderedIndices.indexOf(targetIndex).coerceAtLeast(0)

        updateAnsweredHistoryNavigation(originIndex, targetPosition, orderedIndices, anchorPoolIndices)

        val restoredState = restoreAnsweredHistoryOverlays(currentState)

        onUpdateSession(

            applyAnsweredHistorySnapshot(

                restoredState, targetIndex, isQuestionAnswered, preferSnapshot = true

            )

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

        readOnlyBrowse: Boolean = false

    ): AnsweredHistoryBackwardResult {

        val currentNavMode = navigationState.mode

        val originIndex = when (currentNavMode) {

            is AnsweredHistoryNavigationState.Active -> currentNavMode.originIndex

            AnsweredHistoryNavigationState.Idle -> currentState.currentIndex

        }

        val anchorPoolIndices = when (currentNavMode) {

            is AnsweredHistoryNavigationState.Active -> currentNavMode.anchorPoolIndices

            AnsweredHistoryNavigationState.Idle -> if (fullAnswerModeActive) {

                PracticeFullAnswerHistoryNavigation.sourcePoolIndices(currentState.questions, originIndex)

            } else {

                emptySet()

            }

        }

        val orderedIndices = when (currentNavMode) {

            is AnsweredHistoryNavigationState.Active -> currentNavMode.orderedIndices

            AnsweredHistoryNavigationState.Idle -> buildPreviousAnsweredIndices(

                currentState, effectiveCurrentMemoryRoundQuestionIds, memoryModeActive, memoryPoolMode

            )

        }

        if (orderedIndices.isEmpty()) {

            logHistory("swipeRight", currentState, currentNavMode as? AnsweredHistoryNavigationState.Active, null, "NoMoreHistory")

            return AnsweredHistoryBackwardResult.NoMoreHistory

        }



        val currentIndex = currentState.currentIndex

        val targetIndex = if (fullAnswerModeActive) {

            PracticeFullAnswerHistoryNavigation.resolveOlderTargetIndex(

                orderedIndices, anchorPoolIndices, currentIndex

            )

        } else {

            val activePos = (currentNavMode as? AnsweredHistoryNavigationState.Active)?.historyPosition

            val targetPos = if (activePos != null) {

                activePos + 1

            } else {

                when (val pos = orderedIndices.indexOf(currentIndex)) {

                    -1 -> 0

                    else -> if (pos < orderedIndices.lastIndex) pos + 1 else null

                }

            }

            targetPos?.let { orderedIndices.getOrNull(it) }

        }



        if (targetIndex == null) {
            val globalPos = orderedIndices.indexOf(currentIndex)
            val atGlobalOldest = globalPos >= 0 && globalPos == orderedIndices.lastIndex
            logHistory(
                "swipeRight", currentState, currentNavMode as? AnsweredHistoryNavigationState.Active,
                null, if (atGlobalOldest) "AtOldest" else "NoMoreHistory"
            )
            return if (atGlobalOldest || currentNavMode is AnsweredHistoryNavigationState.Active) {
                AnsweredHistoryBackwardResult.AtOldestAnswered
            } else {
                AnsweredHistoryBackwardResult.NoMoreHistory
            }
        }



        if (readOnlyBrowse) {

            val targetPosition = orderedIndices.indexOf(targetIndex)

            updateAnsweredHistoryNavigation(originIndex, targetPosition, orderedIndices, anchorPoolIndices)

            PracticeAnsweredBrowseNavigation.navigateReadOnly(

                restoreAnsweredHistoryOverlays(currentState), targetIndex, onUpdateSession

            )

        } else {

            commitHistoryNavigation(

                currentState, orderedIndices, targetIndex, originIndex, anchorPoolIndices,

                isQuestionAnswered, onUpdateSession

            )

        }

        logHistory("swipeRight", currentState, navigationState.mode as? AnsweredHistoryNavigationState.Active, targetIndex, "Navigated")

        onSaveProgress()

        return AnsweredHistoryBackwardResult.Navigated

    }



    fun navigateToNextAnsweredInHistory(

        currentState: PracticeSessionState,

        onUpdateSession: (PracticeSessionState) -> Unit,

        onSaveProgress: () -> Unit,

        effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,

        memoryModeActive: Boolean,

        memoryPoolMode: Int,

        isQuestionAnswered: (QuestionWithState) -> Boolean,

        fullAnswerModeActive: Boolean = false

    ): AnsweredHistoryForwardResult {

        val currentNavMode = navigationState.mode

        val originIndex = when (currentNavMode) {

            is AnsweredHistoryNavigationState.Active -> currentNavMode.originIndex

            AnsweredHistoryNavigationState.Idle -> currentState.currentIndex

        }

        val anchorPoolIndices = when (currentNavMode) {

            is AnsweredHistoryNavigationState.Active -> currentNavMode.anchorPoolIndices

            AnsweredHistoryNavigationState.Idle -> if (fullAnswerModeActive) {

                PracticeFullAnswerHistoryNavigation.sourcePoolIndices(currentState.questions, originIndex)

            } else {

                emptySet()

            }

        }

        val orderedIndices = when (currentNavMode) {

            is AnsweredHistoryNavigationState.Active -> currentNavMode.orderedIndices

            AnsweredHistoryNavigationState.Idle -> buildPreviousAnsweredIndices(

                currentState, effectiveCurrentMemoryRoundQuestionIds, memoryModeActive, memoryPoolMode

            )

        }

        if (orderedIndices.isEmpty()) {

            logHistory("swipeLeft", currentState, currentNavMode as? AnsweredHistoryNavigationState.Active, null, "NotInHistory")

            return AnsweredHistoryForwardResult.NotInHistory

        }



        val currentIndex = currentState.currentIndex

        val targetIndex = if (fullAnswerModeActive) {

            PracticeFullAnswerHistoryNavigation.resolveNewerTargetIndex(

                orderedIndices, anchorPoolIndices, currentIndex

            )

        } else {

            val activePos = (currentNavMode as? AnsweredHistoryNavigationState.Active)?.historyPosition

            if (activePos != null) {

                if (activePos > 0) orderedIndices[activePos - 1] else null

            } else {

                val pos = orderedIndices.indexOf(currentIndex)

                if (pos > 0) orderedIndices[pos - 1] else null

            }

        }



        if (targetIndex == null) {

            if (currentNavMode is AnsweredHistoryNavigationState.Active) {

                onUpdateSession(resumeFromAnsweredHistory(currentState))

                onSaveProgress()

                logHistory("swipeLeft", currentState, currentNavMode, null, "ResumeLive")

                return AnsweredHistoryForwardResult.Navigated

            }

            logHistory("swipeLeft", currentState, null, null, "AtLatest")

            return AnsweredHistoryForwardResult.AtLatestAnswered

        }



        commitHistoryNavigation(

            currentState, orderedIndices, targetIndex, originIndex, anchorPoolIndices,

            isQuestionAnswered, onUpdateSession

        )

        logHistory("swipeLeft", currentState, navigationState.mode as? AnsweredHistoryNavigationState.Active, targetIndex, "Navigated")

        onSaveProgress()

        return AnsweredHistoryForwardResult.Navigated

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

        val entries = questionsWithState.mapIndexedNotNull { index, qws ->
            if (index == currentIndex || !isQuestionAnswered(qws)) null
            else index to qws.sessionAnswerTime
        }
        val seededHistory = AnsweredBrowseOrder.sortIndicesByAnswerTimeDesc(entries)

        navigationState = navigationState.copy(

            randomHistory = RandomNavigationHistoryState(history = seededHistory)

        )

    }



    companion object {

        const val MEMORY_POOL_MODE_ROUND = 1

        private const val TAG = "PracticeHistorySwipe"

    }

}


