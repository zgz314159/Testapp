package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconNavigation
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconRetryPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerNavigation
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerUnansweredSourceNavigation
import com.example.testapp.presentation.screen.practice.PracticeUnansweredNavigation
import com.example.testapp.presentation.screen.practice.SkipUnansweredSourceResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Navigation Controller — core nextQuestion / prevQuestion / goToQuestion orchestration.
 * Extracted from PracticeNavigationCoordinator's Phase 4 section (~250 lines).
 */
class NavigationController(
    private val _sessionState: MutableStateFlow<PracticeSessionState>,
    private val scope: CoroutineScope,
    private val history: NavigationHistory,
    // lambda dependencies
    private val isQuestionPendingForCurrentMode: (QuestionWithState) -> Boolean,
    private val isQuestionAnswered: (QuestionWithState) -> Boolean,
    private val shouldReopenUnansweredReveal: (QuestionWithState) -> Boolean,
    private val currentSourcePendingIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    private val isCurrentSourceComplete: (PracticeSessionState) -> Boolean,
    private val findNextSourceEntryIndices: (PracticeSessionState) -> List<Int>,
    private val findAdjacentDerivedQuestionIndex: (PracticeSessionState, Boolean) -> Int?,
    private val effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
    private val nextFullAnswerCandidateIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    private val reopenQuestionForPendingRetry: (Int) -> Unit,
    private val reopenQuestionForFullAnswerRetry: (Int) -> Unit,
    private val scheduleNavigationSave: () -> Unit,
    private val fullAnswerModeActive: () -> Boolean,
    private val fullAnswerRequireCorrect: () -> Boolean,
    private val memoryModeActive: () -> Boolean,
    private val randomPracticeEnabled: () -> Boolean
) {
    private fun navigateToQuestion(index: Int, reopenWrongFullAnswerRetry: Boolean = false) {
        val currentState = _sessionState.value
        if (index !in currentState.questionsWithState.indices) return

        val targetQuestion = currentState.questionsWithState[index]
        if (reopenWrongFullAnswerRetry && fullAnswerRequireCorrect() &&
            targetQuestion.showResult && targetQuestion.isCorrect != true
        ) {
            reopenQuestionForFullAnswerRetry(index)
            return
        }
        if (shouldReopenUnansweredReveal(targetQuestion)) {
            reopenQuestionForPendingRetry(index)
            return
        }
        if (index != currentState.currentIndex) {
            _sessionState.value = currentState.copy(currentIndex = index)
            scheduleNavigationSave()
        }
    }

    fun nextQuestionViaIcon(): UnansweredNavResult = navigateUnansweredNext()

    fun prevQuestionViaIcon(): UnansweredNavResult = navigateUnansweredPrev()

    fun canNavigateToPrevUnanswered(): Boolean {
        val state = prepareStateForUnansweredIconNav(_sessionState.value)
        if (fullAnswerModeActive()) {
            return hasPrevInFullAnswerSourcePool(state) ||
                PracticeUnansweredNavigation.hasPrevUnanswered(
                    state.currentIndex, state.questionsWithState, ::isPendingAt,
                    randomPractice = randomPracticeEnabled()
                )
        }
        return PracticeUnansweredNavigation.hasPrevUnanswered(
            state.currentIndex, state.questionsWithState, ::isPendingAt,
            randomPractice = randomPracticeEnabled()
        )
    }

    fun canNavigateToNextUnanswered(): Boolean {
        val state = prepareStateForUnansweredIconNav(_sessionState.value)
        if (fullAnswerModeActive()) {
            return hasNextInFullAnswerSourcePool(state) ||
                PracticeUnansweredNavigation.hasNextUnanswered(
                    state.currentIndex, state.questionsWithState, ::isPendingAt,
                    randomPractice = randomPracticeEnabled()
                )
        }
        return PracticeUnansweredNavigation.hasNextUnanswered(
            state.currentIndex, state.questionsWithState, ::isPendingAt,
            randomPractice = randomPracticeEnabled()
        )
    }

    private fun hasPrevInFullAnswerSourcePool(state: PracticeSessionState): Boolean =
        PracticeFullAnswerIconNavigation.hasPrevInSourcePool(
            state.currentIndex, state.questions, isCurrentSourceComplete(state)
        )

    private fun hasNextInFullAnswerSourcePool(state: PracticeSessionState): Boolean =
        PracticeFullAnswerIconNavigation.hasNextInSourcePool(
            state.currentIndex, state.questions, isCurrentSourceComplete(state)
        )

    private fun prepareStateForUnansweredIconNav(currentState: PracticeSessionState): PracticeSessionState =
        if (history.isInAnsweredHistory) history.exitAnsweredHistoryBrowsing(currentState) else currentState

    private fun isPendingAt(qws: QuestionWithState): Boolean = isQuestionPendingForCurrentMode(qws)

    private fun navigateUnansweredPrev(): UnansweredNavResult {
        val stateBefore = _sessionState.value
        val anchorIndex = stateBefore.currentIndex
        val clearedState = prepareStateForUnansweredIconNav(stateBefore)
        if (clearedState != stateBefore) {
            _sessionState.value = clearedState
        }

        if (fullAnswerModeActive()) {
            val questions = clearedState.questions
            val currentIdx = clearedState.currentIndex
            val sourceComplete = isCurrentSourceComplete(clearedState)
            PracticeFullAnswerIconNavigation.resolvePrevInSourcePool(
                currentIdx, questions, sourceComplete
            )?.let { targetIndex ->
                if (targetIndex != currentIdx || !sourceComplete) {
                    navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                    return UnansweredNavResult.Navigated
                }
            }
        }

        val (result, targetIndex) = PracticeUnansweredNavigation.resolvePrevUnansweredIndex(
            anchorIndex = anchorIndex,
            questionsWithState = clearedState.questionsWithState,
            isPending = ::isPendingAt,
            randomPractice = randomPracticeEnabled()
        )
        if (result == UnansweredNavResult.Navigated && targetIndex != null) {
            navigateToQuestion(targetIndex)
        } else {
            scheduleNavigationSave()
        }
        return result
    }

    private fun navigateUnansweredNext(): UnansweredNavResult {
        val stateBefore = _sessionState.value
        val anchorIndex = stateBefore.currentIndex
        val clearedState = prepareStateForUnansweredIconNav(stateBefore)
        if (clearedState != stateBefore) {
            _sessionState.value = clearedState
        }

        if (fullAnswerModeActive()) {
            val questions = clearedState.questions
            val currentIdx = clearedState.currentIndex
            val sourceComplete = isCurrentSourceComplete(clearedState)
            PracticeFullAnswerIconNavigation.resolveNextInSourcePool(
                currentIdx, questions, sourceComplete
            )?.let { targetIndex ->
                if (targetIndex != currentIdx || !sourceComplete) {
                    if (targetIndex != currentIdx) {
                        history.recordRandomNavigationOrigin(currentIdx, randomPracticeEnabled())
                    }
                    navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                    return UnansweredNavResult.Navigated
                }
            }
            PracticeFullAnswerIconRetryPipeline.resolveStayIndexForWrongRetry(
                clearedState,
                sourceComplete,
                fullAnswerRequireCorrect()
            )?.let { targetIndex ->
                navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                return UnansweredNavResult.Navigated
            }
        }

        val (result, targetIndex) = PracticeUnansweredNavigation.resolveNextUnansweredIndex(
            anchorIndex = anchorIndex,
            questionsWithState = clearedState.questionsWithState,
            isPending = ::isPendingAt,
            randomPractice = randomPracticeEnabled()
        )
        if (result == UnansweredNavResult.Navigated && targetIndex != null) {
            navigateToQuestion(targetIndex)
        } else {
            scheduleNavigationSave()
        }
        return result
    }

    fun nextQuestion() {
        val stateBeforeFn = _sessionState.value
        var currentState = history.prepareStateForForwardNavigation(stateBeforeFn)
        val exitedAnsweredHistory = currentState.currentIndex != stateBeforeFn.currentIndex

        if (exitedAnsweredHistory) {
            _sessionState.value = currentState
            if (!randomPracticeEnabled()) {
                val restoredQuestion = currentState.questionsWithState.getOrNull(currentState.currentIndex)
                if (restoredQuestion != null && isQuestionPendingForCurrentMode(restoredQuestion)) {
                    if (shouldReopenUnansweredReveal(restoredQuestion)) {
                        reopenQuestionForPendingRetry(currentState.currentIndex)
                        return
                    }
                    scheduleNavigationSave()
                    return
                }
            }
        }

        val currentIdx = currentState.currentIndex

        // Full answer mode: stay within current source
        val fullAnswerStayIndices = if (fullAnswerModeActive()) {
            currentSourcePendingIndices(
                currentState.questionsWithState, currentIdx,
                currentState.questionsWithState.indices.toList()
            )
        } else emptyList()

        if (fullAnswerStayIndices.isNotEmpty()) {
            val otherPending = fullAnswerStayIndices.filter { it != currentIdx }
            val targetIndex = otherPending.firstOrNull { it > currentIdx } ?: otherPending.firstOrNull()
            if (targetIndex != null) {
                history.recordRandomNavigationOrigin(currentIdx, randomPracticeEnabled())
                navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                return
            }
            val curQws = currentState.questionsWithState.getOrNull(currentIdx)
            if (fullAnswerRequireCorrect() && curQws?.showResult == true && curQws.isCorrect != true) {
                reopenQuestionForFullAnswerRetry(currentIdx)
                return
            }
        }

        // Check pending questions
        val curQws = currentState.questionsWithState.getOrNull(currentIdx)
        val otherPendingIndices = currentState.questionsWithState.indices.filter { index ->
            index != currentIdx && isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
        }
        if (curQws != null && shouldReopenUnansweredReveal(curQws) && otherPendingIndices.isEmpty()) {
            reopenQuestionForPendingRetry(currentIdx)
            return
        }
        if (fullAnswerRequireCorrect() && curQws?.showResult == true && curQws.isCorrect != true && otherPendingIndices.isEmpty()) {
            reopenQuestionForFullAnswerRetry(currentIdx)
            return
        }

        // Next source entry
        if (fullAnswerModeActive() && isCurrentSourceComplete(currentState)) {
            val nextSourceIndex = findNextSourceEntryIndices(currentState).firstOrNull()
            if (nextSourceIndex != null && nextSourceIndex != currentIdx) {
                history.recordRandomNavigationOrigin(currentIdx, randomPracticeEnabled())
                navigateToQuestion(nextSourceIndex, reopenWrongFullAnswerRetry = true)
                return
            }
        }

        // Adjacent derived question
        findAdjacentDerivedQuestionIndex(currentState, true)?.let { targetIndex ->
            if (targetIndex != currentState.currentIndex) {
                history.recordRandomNavigationOrigin(currentState.currentIndex, randomPracticeEnabled())
                _sessionState.value = currentState.copy(currentIndex = targetIndex)
                scheduleNavigationSave()
            }
            return
        }

        val roundIds = effectiveCurrentMemoryRoundQuestionIds(currentState.questionsWithState)

        // Random mode
        if (randomPracticeEnabled()) {
            val eligibleIndices = currentState.questionsWithState.indices.filter { index ->
                !memoryModeActive() || roundIds.isEmpty() ||
                    currentState.questionsWithState[index].question.id in roundIds
            }
            val candidateIndices = if (fullAnswerModeActive()) {
                nextFullAnswerCandidateIndices(currentState.questionsWithState, currentIdx, eligibleIndices)
            } else {
                val pending = eligibleIndices.filter { index ->
                    index != currentIdx && isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
                }
                if (pending.isNotEmpty()) pending else eligibleIndices.filter { it != currentIdx }
            }
            if (candidateIndices.isNotEmpty()) {
                val randomIndex = candidateIndices.random(kotlin.random.Random(System.currentTimeMillis()))
                history.recordRandomNavigationOrigin(currentIdx, randomPracticeEnabled())
                navigateToQuestion(randomIndex, reopenWrongFullAnswerRetry = true)
            }
        } else {
            // Sequential mode
            val eligibleIndices = currentState.questionsWithState.indices.filter { index ->
                !memoryModeActive() || roundIds.isEmpty() ||
                    currentState.questionsWithState[index].question.id in roundIds
            }
            val sequentialCandidates = if (fullAnswerModeActive()) {
                nextFullAnswerCandidateIndices(currentState.questionsWithState, currentIdx, eligibleIndices)
            } else emptyList()

            if (sequentialCandidates.isNotEmpty()) {
                val targetIndex = sequentialCandidates.firstOrNull { it > currentIdx }
                    ?: sequentialCandidates.firstOrNull()
                if (targetIndex != null) {
                    navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                    return
                }
            }

            val unansweredAfter = eligibleIndices.firstOrNull { index ->
                index > currentIdx && isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
            }
            val unansweredBefore = eligibleIndices.firstOrNull { index ->
                index < currentIdx && isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
            }
            val nextSeq = eligibleIndices.firstOrNull { index -> index > currentIdx }
            val targetIndex = unansweredAfter ?: unansweredBefore ?: nextSeq

            if (targetIndex != null && targetIndex != currentIdx) {
                navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                return
            }
        }
        scheduleNavigationSave()
    }

    fun browseAnsweredHistoryOlder(): AnsweredHistoryBackwardResult {
        val currentState = _sessionState.value
        return history.navigateToPreviousAnsweredQuestion(
            currentState = currentState,
            onUpdateSession = { _sessionState.value = it },
            onSaveProgress = { scheduleNavigationSave() },
            effectiveCurrentMemoryRoundQuestionIds = effectiveCurrentMemoryRoundQuestionIds,
            memoryModeActive = memoryModeActive(),
            memoryPoolMode = NavigationHistory.MEMORY_POOL_MODE_ROUND,
            isQuestionAnswered = isQuestionAnswered,
            fullAnswerModeActive = fullAnswerModeActive()
        )
    }

    fun browseAnsweredHistoryNewer(): AnsweredHistoryForwardResult {
        val currentState = _sessionState.value
        return history.navigateToNextAnsweredInHistory(
            currentState = currentState,
            onUpdateSession = { _sessionState.value = it },
            onSaveProgress = { scheduleNavigationSave() },
            effectiveCurrentMemoryRoundQuestionIds = effectiveCurrentMemoryRoundQuestionIds,
            memoryModeActive = memoryModeActive(),
            memoryPoolMode = NavigationHistory.MEMORY_POOL_MODE_ROUND,
            isQuestionAnswered = isQuestionAnswered,
            fullAnswerModeActive = fullAnswerModeActive()
        )
    }

    fun goToQuestion(index: Int) {
        var currentState = _sessionState.value
        if (index in 0 until currentState.questionsWithState.size) {
            history.clearAll()
            currentState = _sessionState.value
            val targetQuestion = currentState.questionsWithState[index]
            if (shouldReopenUnansweredReveal(targetQuestion)) {
                reopenQuestionForPendingRetry(index)
            } else {
                _sessionState.value = currentState.copy(currentIndex = index)
                scheduleNavigationSave()
            }
        }
    }

    fun resetNavigationForManualJump() {
        history.clearAll()
    }

    fun canSkipToUnansweredSource(forward: Boolean): Boolean {
        if (!fullAnswerModeActive()) return false
        val state = prepareStateForUnansweredIconNav(_sessionState.value)
        val isPendingAt: (Int) -> Boolean = { idx ->
            state.questionsWithState.getOrNull(idx)?.let { isQuestionPendingForCurrentMode(it) } ?: false
        }
        val entryIndex = PracticeFullAnswerUnansweredSourceNavigation.resolveUnansweredSourceEntryIndex(
            questions = state.questions,
            currentIndex = state.currentIndex,
            forward = forward,
            randomOrder = randomPracticeEnabled(),
            isSourceIncomplete = { entryIdx ->
                PracticeFullAnswerUnansweredSourceNavigation.isSourceIncomplete(
                    state.questions, entryIdx, isPendingAt
                )
            }
        ) ?: return false
        return PracticeFullAnswerUnansweredSourceNavigation.resolveFirstPendingInSource(
            state.questions, entryIndex, isPendingAt
        ) != null
    }

    fun skipToUnansweredSource(forward: Boolean): SkipUnansweredSourceResult {
        if (!fullAnswerModeActive()) {
            return if (forward) SkipUnansweredSourceResult.NoNextSource
            else SkipUnansweredSourceResult.NoPrevSource
        }
        var state = _sessionState.value
        if (history.isInAnsweredHistory) {
            state = history.exitAnsweredHistoryBrowsing(state)
            _sessionState.value = state
        }
        val isPendingAt: (Int) -> Boolean = { idx ->
            state.questionsWithState.getOrNull(idx)?.let { isQuestionPendingForCurrentMode(it) } ?: false
        }
        val entryIndex = PracticeFullAnswerUnansweredSourceNavigation.resolveUnansweredSourceEntryIndex(
            questions = state.questions,
            currentIndex = state.currentIndex,
            forward = forward,
            randomOrder = randomPracticeEnabled(),
            isSourceIncomplete = { entryIdx ->
                PracticeFullAnswerUnansweredSourceNavigation.isSourceIncomplete(
                    state.questions, entryIdx, isPendingAt
                )
            }
        ) ?: return if (forward) SkipUnansweredSourceResult.NoNextSource
        else SkipUnansweredSourceResult.NoPrevSource

        val targetIndex = PracticeFullAnswerUnansweredSourceNavigation.resolveFirstPendingInSource(
            state.questions, entryIndex, isPendingAt
        )
        if (targetIndex == null) {
            android.util.Log.d(
                "PracticeHistorySwipe",
                "skipToUnansweredSource | forward=$forward | entry=$entryIndex | target=null"
            )
            return if (forward) SkipUnansweredSourceResult.NoNextSource
            else SkipUnansweredSourceResult.NoPrevSource
        }

        history.clearAnsweredHistoryNavigation()
        navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
        android.util.Log.d(
            "PracticeHistorySwipe",
            "skipToUnansweredSource | forward=$forward | entry=$entryIndex | target=$targetIndex"
        )
        return SkipUnansweredSourceResult.Navigated
    }

    @Deprecated("Use canSkipToUnansweredSource")
    fun canSkipToAdjacentSource(forward: Boolean): Boolean = canSkipToUnansweredSource(forward)

    @Deprecated("Use skipToUnansweredSource")
    fun skipToAdjacentSource(forward: Boolean) {
        skipToUnansweredSource(forward)
    }
}
