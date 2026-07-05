package com.example.testapp.presentation.session.practice

import com.example.testapp.core.session.strategy.navigation.SessionNavigationHistoryGate
import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationGate
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.presentation.screen.practice.PracticeFillConfig
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerIconNavDebugLog
import com.example.testapp.presentation.screen.practice.PracticeFullAnswerModeActivePipeline
import com.example.testapp.presentation.screen.practice.PracticeJumpDebugLog
import com.example.testapp.presentation.screen.practice.PracticeNavigationCoordinator
import com.example.testapp.presentation.screen.practice.PracticeReviewSessionCoordinator
import com.example.testapp.presentation.screen.practice.SkipUnansweredSourceResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryBackwardResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryForwardResult
import kotlinx.coroutines.flow.StateFlow

/** Practice 导航：Strategy 门禁 + NavigationCoordinator 委托 */
internal class PracticeSessionNavigationDelegate(
    private val strategyCoordinator: PracticeSessionStrategyCoordinator,
    private val reviewCoordinator: PracticeReviewSessionCoordinator,
    private val navigationCoordinator: PracticeNavigationCoordinator,
    private val sessionState: StateFlow<PracticeSessionState>,
    private val activeFillConfig: () -> PracticeFillConfig,
) {
    val isFullAnswerMode: Boolean
        get() =
            PracticeFullAnswerModeActivePipeline.isActive(
                activeFillConfig(),
                sessionState.value.questions,
            )

    fun nextQuestion() {
        if (tryReviewNavigate(1)) return
        if (!SessionNavigationOrchestrationGate.allowsPostAnswerAdvance(orchestration())) return
        PracticeJumpDebugLog.vmNextQuestion(sessionState.value.currentIndex)
        navigationCoordinator.nextQuestion()
    }

    fun prevQuestionViaIcon(): UnansweredNavResult {
        if (tryReviewNavigate(-1)) return UnansweredNavResult.Navigated
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration())) {
            return UnansweredNavResult.AtFirstUnanswered
        }
        logIconTap(forward = false, source = "VM.prevQuestionViaIcon")
        return navigationCoordinator.prevQuestionViaIcon()
    }

    fun nextQuestionViaIcon(): UnansweredNavResult {
        if (tryReviewNavigate(1)) return UnansweredNavResult.Navigated
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration())) {
            return UnansweredNavResult.AtLastUnanswered
        }
        logIconTap(forward = true, source = "VM.nextQuestionViaIcon")
        return navigationCoordinator.nextQuestionViaIcon()
    }

    fun prevQuestionViaIconDoubleClick(): Boolean {
        if (!SessionNavigationOrchestrationGate.allowsDoubleClickCrossSource(
                orchestration(),
                isFullAnswerMode,
            )
        ) {
            return false
        }
        return navigationCoordinator.prevQuestionViaIconDoubleClick()
    }

    fun nextQuestionViaIconDoubleClick(): Boolean {
        if (!SessionNavigationOrchestrationGate.allowsDoubleClickCrossSource(
                orchestration(),
                isFullAnswerMode,
            )
        ) {
            return false
        }
        return navigationCoordinator.nextQuestionViaIconDoubleClick()
    }

    fun canNavigateToPrevUnanswered(): Boolean {
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration())) return false
        return navigationCoordinator.canNavigateToPrevUnanswered() ||
            (isFullAnswerMode && navigationCoordinator.canSkipToUnansweredSource(forward = false))
    }

    fun canNavigateToNextUnanswered(): Boolean {
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration())) return false
        return navigationCoordinator.canNavigateToNextUnanswered() ||
            (isFullAnswerMode && navigationCoordinator.canSkipToUnansweredSource(forward = true))
    }

    fun prevQuestion() {
        if (tryReviewNavigate(-1)) return
        if (SessionNavigationHistoryGate.isReviewPostAnswerNavOnly(orchestration())) return
        if (!SessionNavigationOrchestrationGate.allowsPostAnswerAdvance(orchestration())) return
        navigationCoordinator.prevQuestion()
    }

    fun isInAnsweredHistory(): Boolean = navigationCoordinator.isInAnsweredHistory

    fun browseAnsweredHistoryOlder(): AnsweredHistoryBackwardResult {
        if (!SessionNavigationHistoryGate.allowsAnsweredHistoryBrowse(orchestration())) {
            return AnsweredHistoryBackwardResult.NoMoreHistory
        }
        reviewCoordinator.browseAnsweredHistoryOlder()?.let { return it }
        val result = navigationCoordinator.browseAnsweredHistoryOlder()
        android.util.Log.d(
            "PracticeHistorySwipe",
            "VM.browseOlder | idx=${sessionState.value.currentIndex} | fullAnswer=$isFullAnswerMode | result=$result",
        )
        return result
    }

    fun browseAnsweredHistoryNewer(): AnsweredHistoryForwardResult {
        if (!SessionNavigationHistoryGate.allowsAnsweredHistoryBrowse(orchestration())) {
            return AnsweredHistoryForwardResult.NotInHistory
        }
        reviewCoordinator.browseAnsweredHistoryNewer()?.let { return it }
        val result = navigationCoordinator.browseAnsweredHistoryNewer()
        android.util.Log.d(
            "PracticeHistorySwipe",
            "VM.browseNewer | idx=${sessionState.value.currentIndex} | fullAnswer=$isFullAnswerMode | inHistory=${navigationCoordinator.isInAnsweredHistory} | result=$result",
        )
        return result
    }

    fun goToQuestion(
        index: Int,
        source: String,
    ) {
        val from = sessionState.value.currentIndex
        if (from != index) {
            PracticeJumpDebugLog.vmGoToQuestion(from, index, source)
        }
        navigationCoordinator.goToQuestion(index)
    }

    fun canSkipToUnansweredSource(forward: Boolean): Boolean = navigationCoordinator.canSkipToUnansweredSource(forward)

    fun skipToUnansweredSource(forward: Boolean): SkipUnansweredSourceResult =
        navigationCoordinator.skipToUnansweredSource(forward)

    private fun orchestration() = strategyCoordinator.navigationOrchestration()

    private fun tryReviewNavigate(delta: Int): Boolean {
        if (!strategyCoordinator.reviewBrowseEnabled()) return false
        return reviewCoordinator.tryNavigateReviewBrowse(delta)
    }

    private fun logIconTap(
        forward: Boolean,
        source: String,
    ) {
        val idx = sessionState.value.currentIndex
        val qws = sessionState.value.questionsWithState.getOrNull(idx)
        PracticeFullAnswerIconNavDebugLog.tapEntry(
            forward = forward,
            source = source,
            detail =
                "idx=$idx fullAnswer=$isFullAnswerMode textLen=${qws?.textAnswer?.length ?: 0} " +
                    "showResult=${qws?.showResult} id=${sessionState.value.questions.getOrNull(idx)?.id}",
        )
    }
}
