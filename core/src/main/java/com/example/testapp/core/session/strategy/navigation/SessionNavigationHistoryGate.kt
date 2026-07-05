package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionNavigationOrchestration

/** NavigationHistory 行为门禁（Strategy 层） */
object SessionNavigationHistoryGate {
    fun allowsAnsweredHistoryBrowse(orchestration: SessionNavigationOrchestration): Boolean =
        SessionNavigationOrchestrationGate.allowsAnsweredHistoryBrowse(orchestration)

    fun shouldPrepareForwardFromAnsweredHistory(orchestration: SessionNavigationOrchestration): Boolean =
        orchestration.behavior.postAnswerSequentialAdvance

    fun shouldClearHistoryOnManualJump(orchestration: SessionNavigationOrchestration): Boolean =
        orchestration.clearNavigationHistoryOnManualJump

    fun shouldTrackAnsweredSnapshots(orchestration: SessionNavigationOrchestration): Boolean =
        orchestration.behavior.answeredHistoryBrowse

    /** 复盘模式：postAnswer prev/next 仅浏览历史，不做 general 答后回退 */
    fun isReviewPostAnswerNavOnly(orchestration: SessionNavigationOrchestration): Boolean =
        !orchestration.behavior.postAnswerSequentialAdvance &&
            orchestration.behavior.answeredHistoryBrowse
}
