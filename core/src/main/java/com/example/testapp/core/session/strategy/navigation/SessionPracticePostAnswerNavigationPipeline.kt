package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionNavigationOrchestration

/** Practice `NavigationSequentialNext` / `NavigationSequentialPrev` 答后 auto-advance 阶段门禁 */
object SessionPracticePostAnswerNavigationPipeline {
    fun shouldResumePendingAfterHistoryExit(
        orchestration: SessionNavigationOrchestration,
        randomPracticeEnabled: Boolean,
        exitedAnsweredHistory: Boolean,
    ): Boolean =
        exitedAnsweredHistory &&
            !randomPracticeEnabled &&
            orchestration.resumePendingAfterExitingAnsweredHistory

    fun shouldTryFullAnswerSourceStay(
        orchestration: SessionNavigationOrchestration,
        fullAnswerModeActive: Boolean,
    ): Boolean = fullAnswerModeActive && orchestration.usesFullAnswerSourceStayAdvance

    fun shouldTryReopenOnPostAnswerAdvance(orchestration: SessionNavigationOrchestration): Boolean =
        orchestration.usesReopenOnPostAnswerAdvance

    fun shouldTryNextSourceEntry(
        orchestration: SessionNavigationOrchestration,
        fullAnswerModeActive: Boolean,
        isCurrentSourceComplete: Boolean,
    ): Boolean =
        fullAnswerModeActive &&
            orchestration.usesNextSourceEntryAdvance &&
            isCurrentSourceComplete

    fun shouldTryAdjacentDerived(orchestration: SessionNavigationOrchestration): Boolean =
        orchestration.usesAdjacentDerivedAdvance

    fun shouldTryMultiRoundPostAnswerPrev(
        orchestration: SessionNavigationOrchestration,
        fullAnswerModeActive: Boolean,
        multiRoundSession: Boolean,
    ): Boolean =
        multiRoundSession &&
            shouldTryFullAnswerSourceStay(orchestration, fullAnswerModeActive)

    fun resolveFinalAdvanceRoute(
        orchestration: SessionNavigationOrchestration,
        randomPracticeEnabled: Boolean,
        fullAnswerModeActive: Boolean,
    ): SessionPostAnswerAdvanceRoute =
        SessionPostAnswerNavigationPipeline.routeAfterRoundPoolChecks(
            fullAnswerModeActive = fullAnswerModeActive,
            randomEnabled = randomPracticeEnabled,
        )

    fun resolveBackwardAdvanceRoute(
        orchestration: SessionNavigationOrchestration,
        randomPracticeEnabled: Boolean,
        fullAnswerModeActive: Boolean,
    ): SessionPostAnswerAdvanceRoute =
        SessionPostAnswerNavigationPipeline.routeAfterRoundPoolChecks(
            fullAnswerModeActive = fullAnswerModeActive,
            randomEnabled = randomPracticeEnabled,
            direction = SessionPostAnswerAdvanceDirection.BACKWARD,
        )
}
