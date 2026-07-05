package com.example.testapp.presentation.session.exam

import com.example.testapp.core.session.strategy.navigation.SessionNavigationHistoryGate
import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationGate
import com.example.testapp.presentation.screen.exam.ExamNavigationCoordinator
import com.example.testapp.presentation.screen.exam.ExamReviewSessionCoordinator
import com.example.testapp.presentation.screen.exam.ExamReviewSwipeOutcome

/** Exam 导航：Strategy 门禁 + NavigationCoordinator 委托 */
internal class ExamSessionNavigationDelegate(
    private val strategyCoordinator: ExamSessionStrategyCoordinator,
    private val reviewCoordinator: ExamReviewSessionCoordinator,
    private val navigationCoordinator: ExamNavigationCoordinator,
    private val isFullAnswerMode: () -> Boolean,
) {
    fun nextQuestion() {
        if (tryReviewNavigate(1)) return
        if (!SessionNavigationOrchestrationGate.allowsPostAnswerAdvance(orchestration())) return
        navigationCoordinator.nextQuestion()
    }

    fun prevQuestion() {
        if (tryReviewNavigate(-1)) return
        if (SessionNavigationHistoryGate.isReviewPostAnswerNavOnly(orchestration())) return
        if (!SessionNavigationOrchestrationGate.allowsPostAnswerAdvance(orchestration())) return
        navigationCoordinator.prevQuestion()
    }

    fun prevQuestionViaIcon() {
        if (tryReviewNavigate(-1)) return
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration())) return
        navigationCoordinator.prevQuestionViaIcon()
    }

    fun nextQuestionViaIcon() {
        if (tryReviewNavigate(1)) return
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration())) return
        navigationCoordinator.nextQuestionViaIcon()
    }

    fun prevQuestionViaIconDoubleClick(): Boolean {
        if (!SessionNavigationOrchestrationGate.allowsDoubleClickCrossSource(
                orchestration(),
                isFullAnswerMode(),
            )
        ) {
            return false
        }
        return navigationCoordinator.prevQuestionViaIconDoubleClick()
    }

    fun nextQuestionViaIconDoubleClick(): Boolean {
        if (!SessionNavigationOrchestrationGate.allowsDoubleClickCrossSource(
                orchestration(),
                isFullAnswerMode(),
            )
        ) {
            return false
        }
        return navigationCoordinator.nextQuestionViaIconDoubleClick()
    }

    fun canNavigateToNextUnanswered(): Boolean {
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration())) return false
        return navigationCoordinator.canNavigateToNextUnanswered() ||
            (isFullAnswerMode() && canSkipToAdjacentSource(forward = true))
    }

    fun canNavigateToPrevUnanswered(): Boolean {
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration())) return false
        return navigationCoordinator.canNavigateToPrevUnanswered() ||
            (isFullAnswerMode() && canSkipToAdjacentSource(forward = false))
    }

    fun canSkipToAdjacentSource(forward: Boolean): Boolean = navigationCoordinator.canSkipToAdjacentSource(forward)

    fun skipToAdjacentSource(forward: Boolean) = navigationCoordinator.skipToAdjacentSource(forward)

    fun hasPendingQuestions(): Boolean = navigationCoordinator.hasPendingQuestions()

    fun prevQuestionSequential() {
        if (!SessionNavigationOrchestrationGate.allowsSequentialIndexNav(orchestration())) return
        navigationCoordinator.prevQuestionSequential()
    }

    fun nextQuestionSequential() {
        if (!SessionNavigationOrchestrationGate.allowsSequentialIndexNav(orchestration())) return
        navigationCoordinator.nextQuestionSequential()
    }

    fun canGoPrevSequential(): Boolean =
        SessionNavigationOrchestrationGate.allowsSequentialIndexNav(orchestration()) &&
            navigationCoordinator.canGoPrevSequential()

    fun canGoNextSequential(): Boolean =
        SessionNavigationOrchestrationGate.allowsSequentialIndexNav(orchestration()) &&
            navigationCoordinator.canGoNextSequential()

    fun goToQuestion(index: Int) {
        // Exam has no NavigationHistory state; gate retained for symmetric Strategy wiring.
        if (SessionNavigationHistoryGate.shouldClearHistoryOnManualJump(orchestration())) {
            // no-op — swipeAnsweredHistory=false for EXAM_LINEAR
        }
        navigationCoordinator.goToQuestion(index)
    }

    fun browseReviewAnsweredOlder(): ExamReviewSwipeOutcome {
        if (!SessionNavigationOrchestrationGate.allowsAnsweredHistoryBrowse(orchestration())) {
            return ExamReviewSwipeOutcome.NoHistory
        }
        return reviewCoordinator.browseReviewAnsweredOlder()
    }

    fun browseReviewAnsweredNewer(): ExamReviewSwipeOutcome {
        if (!SessionNavigationOrchestrationGate.allowsAnsweredHistoryBrowse(orchestration())) {
            return ExamReviewSwipeOutcome.AtLatest
        }
        return reviewCoordinator.browseReviewAnsweredNewer()
    }

    private fun orchestration() = strategyCoordinator.navigationOrchestration()

    private fun tryReviewNavigate(delta: Int): Boolean {
        if (!strategyCoordinator.reviewBrowseEnabled()) return false
        return reviewCoordinator.tryNavigateReviewBrowse(delta)
    }
}
