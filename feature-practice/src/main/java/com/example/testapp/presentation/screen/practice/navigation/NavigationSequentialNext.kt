package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.session.strategy.navigation.SessionPostAnswerAdvanceRoute
import com.example.testapp.core.session.strategy.navigation.SessionPracticePostAnswerNavigationPipeline
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog

/** Auto-advance / programmatic nextQuestion after answer reveal. */
internal class NavigationSequentialNext(
    private val env: NavigationEnvironment,
    private val targets: NavigationTargetNavigator
) {
    fun nextQuestion() {
        PracticeJumpDebugLog.vmNextQuestion(env.sessionState.value.currentIndex)
        val orch = env.effectiveOrchestration()
        val stateBeforeFn = env.sessionState.value
        var currentState = env.history.prepareStateForForwardNavigation(stateBeforeFn)
        val exitedAnsweredHistory = currentState.currentIndex != stateBeforeFn.currentIndex

        if (
            SessionPracticePostAnswerNavigationPipeline.shouldResumePendingAfterHistoryExit(
                orchestration = orch,
                randomPracticeEnabled = env.randomPracticeEnabled(),
                exitedAnsweredHistory = exitedAnsweredHistory,
            )
        ) {
            env.sessionState.value = currentState
            val restoredQuestion = currentState.questionsWithState.getOrNull(currentState.currentIndex)
            if (restoredQuestion != null && env.isQuestionPendingForCurrentMode(restoredQuestion)) {
                if (env.shouldReopenUnansweredReveal(restoredQuestion)) {
                    env.reopenQuestionForPendingRetry(currentState.currentIndex)
                    return
                }
                env.scheduleNavigationSave()
                return
            }
        }

        val currentIdx = currentState.currentIndex

        if (
            SessionPracticePostAnswerNavigationPipeline.shouldTryFullAnswerSourceStay(
                orchestration = orch,
                fullAnswerModeActive = env.fullAnswerModeActive(),
            )
        ) {
            val fullAnswerStayIndices = env.currentSourcePendingIndices(
                currentState.questionsWithState,
                currentIdx,
                currentState.questionsWithState.indices.toList(),
            )
            if (fullAnswerStayIndices.isNotEmpty()) {
                val otherPending = fullAnswerStayIndices.filter { it != currentIdx }
                val targetIndex = otherPending.firstOrNull { it > currentIdx } ?: otherPending.firstOrNull()
                if (targetIndex != null) {
                    env.history.recordRandomNavigationOrigin(currentIdx, env.randomPracticeEnabled())
                    targets.navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                    return
                }
                val curQws = currentState.questionsWithState.getOrNull(currentIdx)
                if (env.fullAnswerRequireCorrect() && curQws?.showResult == true && curQws.isCorrect != true) {
                    env.reopenQuestionForFullAnswerRetry(currentIdx)
                    return
                }
            }
        }

        if (SessionPracticePostAnswerNavigationPipeline.shouldTryReopenOnPostAnswerAdvance(orch)) {
            val curQws = currentState.questionsWithState.getOrNull(currentIdx)
            val otherPendingIndices = currentState.questionsWithState.indices.filter { index ->
                index != currentIdx &&
                    env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
            }
            if (curQws != null && env.shouldReopenUnansweredReveal(curQws) && otherPendingIndices.isEmpty()) {
                env.reopenQuestionForPendingRetry(currentIdx)
                return
            }
            if (
                env.fullAnswerRequireCorrect() &&
                    curQws?.showResult == true &&
                    curQws.isCorrect != true &&
                    otherPendingIndices.isEmpty()
            ) {
                env.reopenQuestionForFullAnswerRetry(currentIdx)
                return
            }
        }

        if (
            SessionPracticePostAnswerNavigationPipeline.shouldTryNextSourceEntry(
                orchestration = orch,
                fullAnswerModeActive = env.fullAnswerModeActive(),
                isCurrentSourceComplete = env.isCurrentSourceComplete(currentState),
            )
        ) {
            val nextSourceIndex = env.findNextSourceEntryIndices(currentState).firstOrNull()
            if (nextSourceIndex != null && nextSourceIndex != currentIdx) {
                env.history.recordRandomNavigationOrigin(currentIdx, env.randomPracticeEnabled())
                targets.navigateToQuestion(nextSourceIndex, reopenWrongFullAnswerRetry = true)
                return
            }
        }

        if (SessionPracticePostAnswerNavigationPipeline.shouldTryAdjacentDerived(orch)) {
            env.findAdjacentDerivedQuestionIndex(currentState, true)?.let { targetIndex ->
                if (targetIndex != currentState.currentIndex) {
                    env.history.recordRandomNavigationOrigin(currentState.currentIndex, env.randomPracticeEnabled())
                    PracticeJumpDebugLog.sequentialNextDirectIndex(currentState.currentIndex, targetIndex)
                    env.sessionState.value = currentState.copy(currentIndex = targetIndex)
                    env.scheduleNavigationSave()
                }
                return
            }
        }

        when (
            SessionPracticePostAnswerNavigationPipeline.resolveFinalAdvanceRoute(
                orchestration = orch,
                randomPracticeEnabled = env.randomPracticeEnabled(),
                fullAnswerModeActive = env.fullAnswerModeActive(),
            )
        ) {
            SessionPostAnswerAdvanceRoute.RANDOM -> navigateRandomForward(currentState, currentIdx)
            SessionPostAnswerAdvanceRoute.FULL_ANSWER_SEQUENTIAL ->
                navigateFullAnswerSequentialForward(currentState, currentIdx)
            SessionPostAnswerAdvanceRoute.UNANSWERED_SCAN ->
                navigateUnansweredScanForward(currentState, currentIdx)
        }
    }

    private fun navigateRandomForward(currentState: PracticeSessionState, currentIdx: Int) {
        val roundIds = env.effectiveCurrentMemoryRoundQuestionIds(currentState.questionsWithState)
        val eligibleIndices = eligibleIndices(currentState, roundIds)
        val candidateIndices =
            if (env.fullAnswerModeActive()) {
                env.nextFullAnswerCandidateIndices(currentState.questionsWithState, currentIdx, eligibleIndices)
            } else {
                val pending = eligibleIndices.filter { index ->
                    index != currentIdx &&
                        env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
                }
                if (pending.isNotEmpty()) pending else eligibleIndices.filter { it != currentIdx }
            }
        if (candidateIndices.isNotEmpty()) {
            val randomIndex = candidateIndices.random(kotlin.random.Random(System.currentTimeMillis()))
            env.history.recordRandomNavigationOrigin(currentIdx, env.randomPracticeEnabled())
            targets.navigateToQuestion(randomIndex, reopenWrongFullAnswerRetry = true)
        } else {
            env.scheduleNavigationSave()
        }
    }

    private fun navigateFullAnswerSequentialForward(currentState: PracticeSessionState, currentIdx: Int) {
        val roundIds = env.effectiveCurrentMemoryRoundQuestionIds(currentState.questionsWithState)
        val eligibleIndices = eligibleIndices(currentState, roundIds)
        val sequentialCandidates =
            env.nextFullAnswerCandidateIndices(currentState.questionsWithState, currentIdx, eligibleIndices)
        if (sequentialCandidates.isNotEmpty()) {
            val targetIndex = sequentialCandidates.firstOrNull { it > currentIdx }
                ?: sequentialCandidates.firstOrNull()
            if (targetIndex != null) {
                targets.navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                return
            }
        }
        navigateUnansweredScanForward(currentState, currentIdx)
    }

    private fun navigateUnansweredScanForward(currentState: PracticeSessionState, currentIdx: Int) {
        val roundIds = env.effectiveCurrentMemoryRoundQuestionIds(currentState.questionsWithState)
        val eligibleIndices = eligibleIndices(currentState, roundIds)
        val unansweredAfter = eligibleIndices.firstOrNull { index ->
            index > currentIdx &&
                env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
        }
        val unansweredBefore = eligibleIndices.firstOrNull { index ->
            index < currentIdx &&
                env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
        }
        val nextSeq = eligibleIndices.firstOrNull { index -> index > currentIdx }
        val targetIndex = unansweredAfter ?: unansweredBefore ?: nextSeq
        if (targetIndex != null && targetIndex != currentIdx) {
            targets.navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
        } else {
            env.scheduleNavigationSave()
        }
    }

    private fun eligibleIndices(
        currentState: PracticeSessionState,
        roundIds: Set<Int>,
    ): List<Int> =
        currentState.questionsWithState.indices.filter { index ->
            !env.memoryModeActive() ||
                roundIds.isEmpty() ||
                currentState.questionsWithState[index].question.id in roundIds
        }
}
