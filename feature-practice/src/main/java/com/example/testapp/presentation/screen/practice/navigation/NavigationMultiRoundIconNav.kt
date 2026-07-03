package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconNavDebugLog
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

        PracticeFullAnswerIconNavDebugLog.roundPoolSnapshot(
            state, currentIdx, requireCorrect, pendingInRound
        )

        if (PracticeFullAnswerSourceTouchPipeline.isSourceCompletelyUntouched(
                state.questions, state.questionsWithState, currentIdx
            )
        ) {
            PracticeFullAnswerIconNavDebugLog.branch(
                forward,
                "step0_untouchedSource",
                "no input on any round — direct skipToUnansweredSource"
            )
            return trySkipToAdjacentSourceOnly(forward)
        }

        if (pendingInRound.isNotEmpty()) {
            val target = PracticeFullAnswerRoundIconNavPipeline.resolveTargetIndex(
                currentIdx, pendingInRound, forward, randomOrder
            )
            PracticeFullAnswerIconNavDebugLog.branch(
                forward,
                "step1_inRoundPool",
                "pending=$pendingInRound target=$target random=$randomOrder"
            )
            if (target != null && target != currentIdx) {
                if (forward) {
                    env.history.recordRandomNavigationOrigin(currentIdx, randomOrder)
                }
                targets.navigateToQuestion(target, reopenWrongFullAnswerRetry = true)
                PracticeFullAnswerIconNavDebugLog.result(forward, "Navigated", "step1_inRoundPool")
                return UnansweredNavResult.Navigated
            }
            PracticeFullAnswerIconNavDebugLog.branch(
                forward,
                "step1_noMove",
                "target=$target — fall through to step2/step4"
            )
        } else {
            PracticeFullAnswerIconNavDebugLog.branch(forward, "step1", "pendingInSourceRound EMPTY")
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
            PracticeFullAnswerIconNavDebugLog.result(forward, "Navigated", "wrongRetry stay=$stayIndex")
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
            PracticeFullAnswerIconNavDebugLog.branch(forward, "step2_sameSourceOtherRound", "target=$target")
            if (forward) {
                env.history.recordRandomNavigationOrigin(currentIdx, randomOrder)
            }
            targets.navigateToQuestion(target, reopenWrongFullAnswerRetry = true)
            PracticeFullAnswerIconNavDebugLog.result(forward, "Navigated", "step2_sameSourceOtherRound")
            return UnansweredNavResult.Navigated
        }

        PracticeFullAnswerIconNavDebugLog.branch(forward, "step4", "skipToUnansweredSource")
        when (skipSource.skipToUnansweredSource(forward = forward)) {
            SkipUnansweredSourceResult.Navigated -> {
                PracticeFullAnswerIconNavDebugLog.result(forward, "Navigated", "step4_skipSource")
                return UnansweredNavResult.Navigated
            }
            else -> Unit
        }
        val boundary = if (forward) UnansweredNavResult.AtLastUnanswered else UnansweredNavResult.AtFirstUnanswered
        PracticeFullAnswerIconNavDebugLog.result(forward, boundary.name, "step4_skipSourceFailed")
        return boundary
    }

    private fun trySkipToAdjacentSourceOnly(forward: Boolean): UnansweredNavResult {
        PracticeFullAnswerIconNavDebugLog.branch(forward, "step4", "skipToUnansweredSource")
        when (skipSource.skipToUnansweredSource(forward = forward)) {
            SkipUnansweredSourceResult.Navigated -> {
                PracticeFullAnswerIconNavDebugLog.result(forward, "Navigated", "step4_skipSource")
                return UnansweredNavResult.Navigated
            }
            else -> Unit
        }
        val boundary = if (forward) UnansweredNavResult.AtLastUnanswered else UnansweredNavResult.AtFirstUnanswered
        PracticeFullAnswerIconNavDebugLog.result(forward, boundary.name, "step4_skipSourceFailed")
        return boundary
    }

    fun pendingInRoundForCanMove(state: PracticeSessionState): List<Int> = pendingInCurrentRoundPool(state)
}
