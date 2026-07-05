package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.session.strategy.navigation.SessionPostAnswerAdvanceRoute
import com.example.testapp.core.session.strategy.navigation.SessionPracticePostAnswerNavigationPipeline
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.presentation.screen.practice.PracticeUnansweredNavigation
import com.example.testapp.presentation.screen.practice.UnansweredNavResult

/** Programmatic prevQuestion after answer reveal (symmetric to NavigationSequentialNext). */
internal class NavigationSequentialPrev(
    private val env: NavigationEnvironment,
    private val targets: NavigationTargetNavigator,
    private val multiRound: NavigationMultiRoundIconNav,
) {
    fun prevQuestion() {
        val orch = env.effectiveOrchestration()
        val stateBefore = env.sessionState.value

        if (
            SessionPracticePostAnswerNavigationPipeline.shouldTryMultiRoundPostAnswerPrev(
                orchestration = orch,
                fullAnswerModeActive = env.fullAnswerModeActive(),
                multiRoundSession = env.usesMultiRoundIconNav(stateBefore),
            )
        ) {
            if (multiRound.navigateMultiRoundViaIcon(forward = false) == UnansweredNavResult.Navigated) {
                return
            }
        }

        when (
            SessionPracticePostAnswerNavigationPipeline.resolveBackwardAdvanceRoute(
                orchestration = orch,
                randomPracticeEnabled = env.randomPracticeEnabled(),
                fullAnswerModeActive = env.fullAnswerModeActive(),
            )
        ) {
            SessionPostAnswerAdvanceRoute.FULL_ANSWER_SEQUENTIAL -> Unit
            SessionPostAnswerAdvanceRoute.RANDOM -> navigateRandomBackward()
            SessionPostAnswerAdvanceRoute.UNANSWERED_SCAN -> navigatePrevUnanswered()
        }
    }

    private fun navigateRandomBackward() {
        val currentState = env.sessionState.value
        val currentIdx = currentState.currentIndex
        val roundIds = env.effectiveCurrentMemoryRoundQuestionIds(currentState.questionsWithState)
        val eligibleIndices = eligibleIndices(currentState, roundIds)
        val candidateIndices =
            if (env.fullAnswerModeActive()) {
                env.nextFullAnswerCandidateIndices(
                    currentState.questionsWithState,
                    currentIdx,
                    eligibleIndices,
                )
            } else {
                eligibleIndices.filter { index ->
                    index != currentIdx &&
                        env.isQuestionPendingForCurrentMode(currentState.questionsWithState[index])
                }.ifEmpty {
                    eligibleIndices.filter { it != currentIdx }
                }
            }
        val backwardCandidates = candidateIndices.filter { it < currentIdx }
        val targetIndex =
            backwardCandidates.lastOrNull()
                ?: candidateIndices.filter { it != currentIdx }.lastOrNull()
        if (targetIndex != null) {
            env.history.recordRandomNavigationOrigin(currentIdx, env.randomPracticeEnabled())
            targets.navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
        } else {
            env.scheduleNavigationSave()
        }
    }

    private fun navigatePrevUnanswered() {
        val currentState = env.sessionState.value
        val (result, targetIndex) =
            PracticeUnansweredNavigation.resolvePrevUnansweredIndex(
                anchorIndex = currentState.currentIndex,
                questionsWithState = currentState.questionsWithState,
                isPending = env::isPendingAt,
                randomPractice = false,
            )
        if (result == UnansweredNavResult.Navigated && targetIndex != null) {
            targets.navigateToQuestion(targetIndex)
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
