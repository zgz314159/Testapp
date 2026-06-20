package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
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
    private val shouldReopenUnansweredReveal: (QuestionWithState) -> Boolean,
    private val currentSourcePendingIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    private val isCurrentSourceComplete: (PracticeSessionState) -> Boolean,
    private val findNextSourceEntryIndices: (PracticeSessionState) -> List<Int>,
    private val findAdjacentDerivedQuestionIndex: (PracticeSessionState, Boolean) -> Int?,
    private val effectiveCurrentMemoryRoundQuestionIds: (List<QuestionWithState>) -> Set<Int>,
    private val nextFullAnswerCandidateIndices: (List<QuestionWithState>, Int, List<Int>) -> List<Int>,
    private val reopenQuestionForPendingRetry: (Int) -> Unit,
    private val reopenQuestionForFullAnswerRetry: (Int) -> Unit,
    private val saveProgress: () -> Unit,
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
            saveProgress()
        }
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
                    saveProgress()
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
                saveProgress()
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
        saveProgress()
    }

    fun prevQuestion() {
        val currentState = _sessionState.value
        if (history.navigateToPreviousAnsweredQuestion(
                currentState = currentState,
                onUpdateSession = { _sessionState.value = it },
                onSaveProgress = { saveProgress() },
                effectiveCurrentMemoryRoundQuestionIds = effectiveCurrentMemoryRoundQuestionIds,
                memoryModeActive = memoryModeActive(),
                memoryPoolMode = NavigationHistory.MEMORY_POOL_MODE_ROUND,
                isQuestionAnswered = isQuestionPendingForCurrentMode
            )
        ) return

        findAdjacentDerivedQuestionIndex(currentState, false)?.let { targetIndex ->
            if (targetIndex != currentState.currentIndex) {
                _sessionState.value = currentState.copy(currentIndex = targetIndex)
                saveProgress()
            }
            return
        }

        if (currentState.currentIndex > 0) {
            _sessionState.value = currentState.copy(currentIndex = currentState.currentIndex - 1)
            saveProgress()
        }
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
                saveProgress()
            }
        }
    }

    fun resetNavigationForManualJump() {
        history.clearAll()
    }
}
