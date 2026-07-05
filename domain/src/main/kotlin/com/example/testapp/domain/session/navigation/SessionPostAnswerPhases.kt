package com.example.testapp.domain.session.navigation

/** Policy 答后 advance 阶段编排（从 SessionNavigationOrchestration 投影） */
data class SessionPostAnswerPhases(
    val resumePendingAfterExitingAnsweredHistory: Boolean,
    val usesFullAnswerSourceStayAdvance: Boolean,
    val usesNextSourceEntryAdvance: Boolean,
    val usesAdjacentDerivedAdvance: Boolean,
    val usesReopenOnPostAnswerAdvance: Boolean
) {
    companion object {
        fun from(orchestration: SessionNavigationOrchestration): SessionPostAnswerPhases =
            SessionPostAnswerPhases(
                resumePendingAfterExitingAnsweredHistory =
                    orchestration.resumePendingAfterExitingAnsweredHistory,
                usesFullAnswerSourceStayAdvance = orchestration.usesFullAnswerSourceStayAdvance,
                usesNextSourceEntryAdvance = orchestration.usesNextSourceEntryAdvance,
                usesAdjacentDerivedAdvance = orchestration.usesAdjacentDerivedAdvance,
                usesReopenOnPostAnswerAdvance = orchestration.usesReopenOnPostAnswerAdvance
            )
    }
}
