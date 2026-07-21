package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.core.util.FullAnswerMultiRoundSessionPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerRoundSlotPendingPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerSourcePendingPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerSourceTouchPipeline
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerUnansweredSourceNavigation
import com.example.testapp.presentation.screen.practice.SkipUnansweredSourceResult

/** Cross-source skip for full-answer icon navigation. */
internal class NavigationSkipSource(
    private val env: NavigationEnvironment,
    private val targets: NavigationTargetNavigator
) {
    fun canSkipToUnansweredSource(forward: Boolean): Boolean {
        if (!env.fullAnswerModeActive()) return false
        val state = env.prepareStateForUnansweredIconNav(env.sessionState.value)
        val isPendingAt: (Int) -> Boolean = { idx ->
            state.questionsWithState.getOrNull(idx)?.let { env.isQuestionPendingForCurrentMode(it) } ?: false
        }
        val entryIndex = PracticeFullAnswerUnansweredSourceNavigation.resolveUnansweredSourceEntryIndex(
            questions = state.questions,
            currentIndex = state.currentIndex,
            forward = forward,
            randomOrder = env.iconNavRandomOrder(),
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

    fun skipToUnansweredSource(
        forward: Boolean,
        forceCrossSource: Boolean = false
    ): SkipUnansweredSourceResult {
        if (!env.fullAnswerModeActive()) {
            return if (forward) SkipUnansweredSourceResult.NoNextSource
            else SkipUnansweredSourceResult.NoPrevSource
        }
        val guardState = env.sessionState.value
        val requireCorrect = env.fullAnswerRequireCorrect()
        if (!forceCrossSource &&
            FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(guardState.questions) &&
            PracticeFullAnswerSourcePendingPipeline.hasPendingInSource(
                guardState.questions,
                guardState.questionsWithState,
                guardState.currentIndex,
                requireCorrect
            ) &&
            PracticeFullAnswerSourceTouchPipeline.hasAnyInputInSource(
                guardState.questions,
                guardState.questionsWithState,
                guardState.currentIndex
            )
        ) {
            val pending = PracticeFullAnswerSourcePendingPipeline.indicesInSource(
                guardState.questions,
                guardState.currentIndex
            ).filter { index ->
                PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                    guardState.questionsWithState[index],
                    requireCorrect
                )
            }
            return if (forward) SkipUnansweredSourceResult.NoNextSource
            else SkipUnansweredSourceResult.NoPrevSource
        }
        var state = guardState
        if (env.history.isInAnsweredHistory) {
            state = env.history.exitAnsweredHistoryBrowsing(state)
            env.sessionState.value = state
        }
        val isPendingAt: (Int) -> Boolean = { idx ->
            state.questionsWithState.getOrNull(idx)?.let { env.isQuestionPendingForCurrentMode(it) } ?: false
        }
        val entryIndex = PracticeFullAnswerUnansweredSourceNavigation.resolveUnansweredSourceEntryIndex(
            questions = state.questions,
            currentIndex = state.currentIndex,
            forward = forward,
            randomOrder = env.iconNavRandomOrder(),
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
            return if (forward) SkipUnansweredSourceResult.NoNextSource
            else SkipUnansweredSourceResult.NoPrevSource
        }

        env.history.clearAnsweredHistoryNavigation()
        targets.navigateToQuestion(targetIndex, reopenWrongFullAnswerRetry = true)
        if (forceCrossSource) {
        }
        return SkipUnansweredSourceResult.Navigated
    }
}
