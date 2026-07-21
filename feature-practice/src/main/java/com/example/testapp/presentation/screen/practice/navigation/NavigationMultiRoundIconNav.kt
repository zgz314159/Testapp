package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconRetryPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerRoundIconNavPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerSameSourceRoundAdvancePipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerSourceTouchPipeline
import com.example.testapp.presentation.screen.practice.SkipUnansweredSourceResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult

/** Multi-round full-answer icon tap decision chain (steps 0–4). */
internal class NavigationMultiRoundIconNav(
    private val env: NavigationEnvironment,
    private val targets: NavigationTargetNavigator,
    private val skipSource: NavigationSkipSource
) {
    private fun pendingInCurrentRoundPool(state: PracticeSessionState): List<Int> =
        PracticeFullAnswerRoundIconNavPipeline.pendingIndicesInRound(
            questions = state.questions,
            questionsWithState = state.questionsWithState,
            currentIndex = state.currentIndex,
            fullAnswerRequireCorrect = env.fullAnswerRequireCorrect()
        )

    fun navigateMultiRoundViaIcon(forward: Boolean): UnansweredNavResult {
        val state = env.prepareStateForUnansweredIconNav(env.sessionState.value)
        val currentIdx = state.currentIndex
        val requireCorrect = env.fullAnswerRequireCorrect()
        val randomOrder = env.iconNavRandomOrder()
        val pendingInRound = pendingInCurrentRoundPool(state)


        if (PracticeFullAnswerSourceTouchPipeline.isSourceCompletelyUntouched(
                state.questions, state.questionsWithState, currentIdx
            )
        ) {
            return trySkipToAdjacentSourceOnly(forward)
        }

        if (pendingInRound.isNotEmpty()) {
            val target = PracticeFullAnswerRoundIconNavPipeline.resolveTargetIndex(
                currentIdx, pendingInRound, forward, randomOrder
            )
            if (target != null && target != currentIdx) {
                if (forward) {
                    env.history.recordRandomNavigationOrigin(currentIdx, randomOrder)
                }
                targets.navigateToQuestion(target, reopenWrongFullAnswerRetry = true)
                return UnansweredNavResult.Navigated
            }
        } else {
        }

        return trySameSourceOtherRoundThenSkip(state, currentIdx, requireCorrect, forward, randomOrder)
    }

    private fun trySameSourceOtherRoundThenSkip(
        state: PracticeSessionState,
        currentIdx: Int,
        requireCorrect: Boolean,
        forward: Boolean,
        randomOrder: Boolean
    ): UnansweredNavResult {
        val roundComplete = !PracticeFullAnswerRoundIconNavPipeline.hasPendingInRound(
            state.questions, state.questionsWithState, currentIdx, requireCorrect
        )
        PracticeFullAnswerIconRetryPipeline.resolveStayIndexForWrongRetry(
            state, roundComplete, requireCorrect
        )?.let { stayIndex ->
            targets.navigateToQuestion(stayIndex, reopenWrongFullAnswerRetry = true)
            return UnansweredNavResult.Navigated
        }

        PracticeFullAnswerSameSourceRoundAdvancePipeline.resolvePendingInSameSourceOtherRound(
            state.questions,
            state.questionsWithState,
            currentIdx,
            requireCorrect,
            forward,
            randomOrder
        )?.let { target ->
            if (forward) {
                env.history.recordRandomNavigationOrigin(currentIdx, randomOrder)
            }
            targets.navigateToQuestion(target, reopenWrongFullAnswerRetry = true)
            return UnansweredNavResult.Navigated
        }

        when (skipSource.skipToUnansweredSource(forward = forward)) {
            SkipUnansweredSourceResult.Navigated -> {
                return UnansweredNavResult.Navigated
            }
            else -> Unit
        }
        val boundary = if (forward) UnansweredNavResult.AtLastUnanswered else UnansweredNavResult.AtFirstUnanswered
        return boundary
    }

    private fun trySkipToAdjacentSourceOnly(forward: Boolean): UnansweredNavResult {
        when (skipSource.skipToUnansweredSource(forward = forward)) {
            SkipUnansweredSourceResult.Navigated -> {
                return UnansweredNavResult.Navigated
            }
            else -> Unit
        }
        val boundary = if (forward) UnansweredNavResult.AtLastUnanswered else UnansweredNavResult.AtFirstUnanswered
        return boundary
    }

    fun pendingInRoundForCanMove(state: PracticeSessionState): List<Int> = pendingInCurrentRoundPool(state)
}
