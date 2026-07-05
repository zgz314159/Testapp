package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionNavigationOrchestration

object SessionNavigationOrchestrationGate {
    fun allowsIconNav(orch: SessionNavigationOrchestration): Boolean = orch.behavior.iconUnansweredNav

    fun allowsPostAnswerAdvance(orch: SessionNavigationOrchestration): Boolean =
        orch.behavior.postAnswerSequentialAdvance

    fun allowsAnsweredHistoryBrowse(orch: SessionNavigationOrchestration): Boolean = orch.behavior.answeredHistoryBrowse

    fun allowsSequentialIndexNav(orch: SessionNavigationOrchestration): Boolean = orch.behavior.sequentialIndexNav

    fun allowsDoubleClickCrossSource(
        orch: SessionNavigationOrchestration,
        fullAnswerModeActive: Boolean,
    ): Boolean =
        orch.behavior.iconDoubleClickCrossSource &&
            (!orch.doubleClickRequiresFullAnswerMode || fullAnswerModeActive)

    fun shouldExitAnsweredHistoryBeforeIconNav(
        orch: SessionNavigationOrchestration,
        inAnsweredHistory: Boolean,
    ): Boolean = inAnsweredHistory && orch.exitAnsweredHistoryBeforeIconNav
}
