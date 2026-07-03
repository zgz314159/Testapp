package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog

/** Auto-advance / programmatic nextQuestion after answer reveal. */
internal class NavigationSequentialNext(
    private val env: NavigationEnvironment,
    private val targets: NavigationTargetNavigator
) {
    fun nextQuestion() {
        PracticeJumpDebugLog.vmNextQuestion(env.sessionState.value.currentIndex)
        val stateBeforeFn = env.sessionState.value
        var currentState = env.history.prepareStateForForwardNavigation(stateBeforeFn)
        val exitedAnsweredHistory = currentState.currentIndex != stateBeforeFn.currentIndex

        if (exitedAnsweredHistory) {
            env.sessionState.value = currentState
            if (!env.randomPracticeEnabled()) {
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
        }

        val currentIdx = currentState.currentIndex

        val fullAnswerStayIndices = if (env.fullAnswerModeActive()) {
            env.currentSourcePendingIndices(
                currentState.questionsWithState, currentIdx,
                currentState.questionsWithState.indices.toList()
            )
        } else emptyList()

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

        val curQws = currentState.questionsWithState.getOrNull(currentIdx)
        val otherPendingIndices = currentState.questionsWithState.indices.filter { index ->
            index != currentIdx && env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
        }
        if (curQws != null && env.shouldReopenUnansweredReveal(curQws) && otherPendingIndices.isEmpty()) {
            env.reopenQuestionForPendingRetry(currentIdx)
            return
        }
        if (env.fullAnswerRequireCorrect() && curQws?.showResult == true && curQws.isCorrect != true &&
            otherPendingIndices.isEmpty()
        ) {
            env.reopenQuestionForFullAnswerRetry(currentIdx)
            return
        }

        if (env.fullAnswerModeActive() && env.isCurrentSourceComplete(currentState)) {
            val nextSourceIndex = env.findNextSourceEntryIndices(currentState).firstOrNull()
            if (nextSourceIndex != null && nextSourceIndex != currentIdx) {
                env.history.recordRandomNavigationOrigin(currentIdx, env.randomPracticeEnabled())
                targets.navigateToQuestion(nextSourceIndex, reopenWrongFullAnswerRetry = true)
                return
            }
        }

        env.findAdjacentDerivedQuestionIndex(currentState, true)?.let { targetIndex ->
            if (targetIndex != currentState.currentIndex) {
                env.history.recordRandomNavigationOrigin(currentState.currentIndex, env.randomPracticeEnabled())
                PracticeJumpDebugLog.sequentialNextDirectIndex(currentState.currentIndex, targetIndex)
                env.sessionState.value = currentState.copy(currentIndex = targetIndex)
                env.scheduleNavigationSave()
            }
            return
        }

        val roundIds = env.effectiveCurrentMemoryRoundQuestionIds(currentState.questionsWithState)

        if (env.randomPracticeEnabled()) {
            val eligibleIndices = currentState.questionsWithState.indices.filter { index ->
                !env.memoryModeActive() || roundIds.isEmpty() ||
                    currentState.questionsWithState[index].question.id in roundIds
            }
            val candidateIndices = if (env.fullAnswerModeActive()) {
                env.nextFullAnswerCandidateIndices(currentState.questionsWithState, currentIdx, eligibleIndices)
            } else {
                val pending = eligibleIndices.filter { index ->
                    index != currentIdx && env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
                }
                if (pending.isNotEmpty()) pending else eligibleIndices.filter { it != currentIdx }
            }
            if (candidateIndices.isNotEmpty()) {
                val randomIndex = candidateIndices.random(kotlin.random.Random(System.currentTimeMillis()))
                env.history.recordRandomNavigationOrigin(currentIdx, env.randomPracticeEnabled())
                targets.navigateToQuestion(randomIndex, reopenWrongFullAnswerRetry = true)
            }
        } else {
            val eligibleIndices = currentState.questionsWithState.indices.filter { index ->
                !env.memoryModeActive() || roundIds.isEmpty() ||
                    currentState.questionsWithState[index].question.id in roundIds
            }
            val sequentialCandidates = if (env.fullAnswerModeActive()) {
                env.nextFullAnswerCandidateIndices(currentState.questionsWithState, currentIdx, eligibleIndices)
            } else emptyList()

            if (sequentialCandidates.isNotEmpty()) {
                val targetIndex = sequentialCandidates.firstOrNull { it > currentIdx }
                    ?: sequentialCandidates.firstOrNull()
                if (targetIndex != null) {
                    targets.navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                    return
                }
            }

            val unansweredAfter = eligibleIndices.firstOrNull { index ->
                index > currentIdx && env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
            }
            val unansweredBefore = eligibleIndices.firstOrNull { index ->
                index < currentIdx && env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
            }
            val nextSeq = eligibleIndices.firstOrNull { index -> index > currentIdx }
            val targetIndex = unansweredAfter ?: unansweredBefore ?: nextSeq

            if (targetIndex != null && targetIndex != currentIdx) {
                targets.navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
                return
            }
        }
        env.scheduleNavigationSave()
    }
}
