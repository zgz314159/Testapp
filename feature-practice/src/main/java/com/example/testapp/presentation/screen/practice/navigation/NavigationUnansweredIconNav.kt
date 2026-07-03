package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.util.FullAnswerIconNavigationStrategyPipeline
import com.example.testapp.core.util.FullAnswerMultiRoundSessionPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconNavDebugLog
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconUnansweredPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerRoundIconNavPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerSameSourceRoundAdvancePipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerSourceTouchPipeline
import com.example.testapp.presentation.screen.practice.PracticeIconUnansweredNavigationPipeline
import com.example.testapp.presentation.screen.practice.PracticeUnansweredNavigation
import com.example.testapp.presentation.screen.practice.SkipUnansweredSourceResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult

/** Bottom-bar ←/→ icon navigation for unanswered questions. */
internal class NavigationUnansweredIconNav(
    private val env: NavigationEnvironment,
    private val targets: NavigationTargetNavigator,
    private val multiRound: NavigationMultiRoundIconNav,
    private val skipSource: NavigationSkipSource
) {
    fun navigatePrev(): UnansweredNavResult = navigate(forward = false)

    fun navigateNext(): UnansweredNavResult = navigate(forward = true)

    private fun navigate(forward: Boolean): UnansweredNavResult {
        val stateBefore = env.sessionState.value
        val anchorIndex = stateBefore.currentIndex
        val clearedState = env.prepareStateForUnansweredIconNav(stateBefore)
        if (clearedState != stateBefore) {
            env.sessionState.value = clearedState
        }
        val strategy = env.iconTapStrategy()
        PracticeFullAnswerIconNavDebugLog.strategy(
            forward = forward,
            fullAnswerActive = env.fullAnswerModeActive(),
            multiRoundSession = FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(clearedState.questions),
            strategyName = strategy.name,
            randomOrder = env.iconNavRandomOrder(),
            requireCorrect = env.fullAnswerRequireCorrect()
        )
        val sourceLabel = if (forward) "navigateUnansweredNext" else "navigateUnansweredPrev"
        PracticeFullAnswerIconNavDebugLog.tapEntry(
            forward = forward,
            source = sourceLabel,
            detail = "anchor=$anchorIndex cleared=${clearedState.currentIndex} " +
                "textLen=${clearedState.questionsWithState.getOrNull(clearedState.currentIndex)?.textAnswer?.length ?: 0}"
        )

        if (FullAnswerIconNavigationStrategyPipeline.singleTapUsesRoundPool(strategy)) {
            return multiRound.navigateMultiRoundViaIcon(forward = forward)
        }

        PracticeFullAnswerIconNavDebugLog.branch(forward, "globalPath", "strategy=$strategy")
        val (result, targetIndex) = if (env.fullAnswerModeActive()) {
            if (forward) {
                PracticeFullAnswerIconUnansweredPipeline.resolveNextIndex(
                    anchorIndex, clearedState.questionsWithState, env::isPendingAt, env.iconNavRandomOrder()
                )
            } else {
                PracticeFullAnswerIconUnansweredPipeline.resolvePrevIndex(
                    anchorIndex, clearedState.questionsWithState, env::isPendingAt, env.iconNavRandomOrder()
                )
            }
        } else {
            if (forward) {
                PracticeUnansweredNavigation.resolveNextUnansweredIndex(
                    anchorIndex, clearedState.questionsWithState, env::isPendingAt, env.randomPracticeEnabled()
                )
            } else {
                PracticeUnansweredNavigation.resolvePrevUnansweredIndex(
                    anchorIndex, clearedState.questionsWithState, env::isPendingAt, env.randomPracticeEnabled()
                )
            }
        }
        if (result == UnansweredNavResult.Navigated && targetIndex != null) {
            PracticeFullAnswerIconNavDebugLog.branch(forward, "globalPath", "resolved target=$targetIndex")
            targets.navigateToQuestion(targetIndex)
            PracticeFullAnswerIconNavDebugLog.result(forward, result.name, "globalPath")
            return result
        }
        if (PracticeIconUnansweredNavigationPipeline.shouldFallbackToUnansweredSource(
                navResult = result,
                strategy = strategy,
                forward = forward
            )
        ) {
            PracticeFullAnswerIconNavDebugLog.branch(forward, "globalPath", "fallback skipSource navResult=$result")
            when (skipSource.skipToUnansweredSource(forward = forward)) {
                SkipUnansweredSourceResult.Navigated -> return UnansweredNavResult.Navigated
                else -> Unit
            }
        }
        env.scheduleNavigationSave()
        PracticeFullAnswerIconNavDebugLog.result(forward, result.name, "globalPath final")
        return result
    }
}

/** Enabled-state queries for bottom-bar ←/→ icons. */
internal class NavigationIconCanMove(
    private val env: NavigationEnvironment,
    private val skipSource: NavigationSkipSource,
    private val multiRound: NavigationMultiRoundIconNav
) {
    fun canNavigateToPrevUnanswered(): Boolean = canMove(forward = false)

    fun canNavigateToNextUnanswered(): Boolean = canMove(forward = true)

    private fun canMove(forward: Boolean): Boolean {
        val state = env.prepareStateForUnansweredIconNav(env.sessionState.value)
        val randomOrder = env.iconNavRandomOrder()
        if (env.usesMultiRoundIconNav(state)) {
            if (PracticeFullAnswerSourceTouchPipeline.isSourceCompletelyUntouched(
                    state.questions, state.questionsWithState, state.currentIndex
                )
            ) {
                return skipSource.canSkipToUnansweredSource(forward = false)
            }
            val pendingInRound = multiRound.pendingInRoundForCanMove(state)
            if (PracticeFullAnswerRoundIconNavPipeline.canMoveInRound(
                    state.currentIndex, pendingInRound, forward = forward, randomOrder
                )
            ) {
                return true
            }
            if (PracticeFullAnswerSameSourceRoundAdvancePipeline.resolvePendingInSameSourceOtherRound(
                    state.questions,
                    state.questionsWithState,
                    state.currentIndex,
                    env.fullAnswerRequireCorrect(),
                    forward = forward,
                    randomOrder
                ) != null
            ) {
                return true
            }
            return skipSource.canSkipToUnansweredSource(forward = forward)
        }
        if (env.fullAnswerModeActive()) {
            return if (forward) {
                PracticeFullAnswerIconUnansweredPipeline.hasNext(
                    state.currentIndex, state.questionsWithState, env::isPendingAt, randomOrder
                )
            } else {
                PracticeFullAnswerIconUnansweredPipeline.hasPrev(
                    state.currentIndex, state.questionsWithState, env::isPendingAt, randomOrder
                )
            }
        }
        return if (forward) {
            PracticeUnansweredNavigation.hasNextUnanswered(
                state.currentIndex, state.questionsWithState, env::isPendingAt, env.randomPracticeEnabled()
            )
        } else {
            PracticeUnansweredNavigation.hasPrevUnanswered(
                state.currentIndex, state.questionsWithState, env::isPendingAt, env.randomPracticeEnabled()
            )
        }
    }
}
