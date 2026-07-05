package com.example.testapp.domain.session.navigation

/** NavigationHistory 策略快照（Strategy 收编入口） */
data class SessionNavigationHistoryPhases(
    val allowsAnsweredHistoryBrowse: Boolean,
    val prepareForwardFromAnsweredHistory: Boolean,
    val clearHistoryOnManualJump: Boolean,
    val trackAnsweredSnapshots: Boolean
) {
    companion object {
        fun from(orchestration: SessionNavigationOrchestration): SessionNavigationHistoryPhases =
            SessionNavigationHistoryPhases(
                allowsAnsweredHistoryBrowse = orchestration.behavior.answeredHistoryBrowse,
                prepareForwardFromAnsweredHistory = orchestration.behavior.postAnswerSequentialAdvance,
                clearHistoryOnManualJump = orchestration.clearNavigationHistoryOnManualJump,
                trackAnsweredSnapshots = orchestration.behavior.answeredHistoryBrowse
            )
    }
}
