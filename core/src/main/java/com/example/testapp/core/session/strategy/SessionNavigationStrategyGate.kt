package com.example.testapp.core.session.strategy

import com.example.testapp.core.session.strategy.navigation.SessionNavigationBehaviorResolver
import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationResolver
import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.navigation.SessionNavigationMode

object SessionNavigationStrategyGate {
    fun behavior(config: SessionNavigationConfig) = SessionNavigationBehaviorResolver.from(config)

    fun orchestration(config: SessionNavigationConfig) = SessionNavigationOrchestrationResolver.from(config)

    fun swipeAnsweredHistoryEnabled(config: SessionNavigationConfig): Boolean = behavior(config).answeredHistoryBrowse

    fun reviewBrowseEnabled(config: SessionNavigationConfig): Boolean =
        config.mode == SessionNavigationMode.REVIEW_HISTORY

    fun reviewHistorySwipeEnabled(config: SessionNavigationConfig): Boolean =
        reviewBrowseEnabled(config) && swipeAnsweredHistoryEnabled(config)

    fun examSequentialNavigationEnabled(config: SessionNavigationConfig): Boolean = behavior(config).sequentialIndexNav

    fun practiceInteractiveNavigationEnabled(config: SessionNavigationConfig): Boolean =
        config.mode == SessionNavigationMode.PRACTICE_INTERACTIVE
}
