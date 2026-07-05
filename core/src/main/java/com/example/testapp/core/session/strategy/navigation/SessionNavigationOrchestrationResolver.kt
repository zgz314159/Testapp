package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import com.example.testapp.domain.session.navigation.SessionNavigationOrchestration

object SessionNavigationOrchestrationResolver {
    fun from(config: SessionNavigationConfig): SessionNavigationOrchestration {
        val behavior = SessionNavigationBehaviorResolver.from(config)

        return when (config.mode) {
            SessionNavigationMode.BROWSE_LINEAR ->
                SessionNavigationOrchestration(
                    behavior = behavior,
                    exitAnsweredHistoryBeforeIconNav = false,
                    clearNavigationHistoryOnManualJump = true,
                    doubleClickRequiresFullAnswerMode = false,
                    resumePendingAfterExitingAnsweredHistory = false,
                    usesFullAnswerSourceStayAdvance = false,
                    usesNextSourceEntryAdvance = false,
                    usesAdjacentDerivedAdvance = false,
                    usesReopenOnPostAnswerAdvance = false,
                )

            SessionNavigationMode.PRACTICE_INTERACTIVE ->
                SessionNavigationOrchestration(
                    behavior = behavior,
                    exitAnsweredHistoryBeforeIconNav = true,
                    clearNavigationHistoryOnManualJump = true,
                    doubleClickRequiresFullAnswerMode = true,
                    resumePendingAfterExitingAnsweredHistory = true,
                    usesFullAnswerSourceStayAdvance = true,
                    usesNextSourceEntryAdvance = true,
                    usesAdjacentDerivedAdvance = true,
                    usesReopenOnPostAnswerAdvance = true,
                )

            SessionNavigationMode.REVIEW_HISTORY ->
                SessionNavigationOrchestration(
                    behavior = behavior,
                    exitAnsweredHistoryBeforeIconNav = false,
                    clearNavigationHistoryOnManualJump = true,
                    doubleClickRequiresFullAnswerMode = false,
                    resumePendingAfterExitingAnsweredHistory = false,
                    usesFullAnswerSourceStayAdvance = false,
                    usesNextSourceEntryAdvance = false,
                    usesAdjacentDerivedAdvance = false,
                    usesReopenOnPostAnswerAdvance = false,
                )

            SessionNavigationMode.EXAM_LINEAR ->
                SessionNavigationOrchestration(
                    behavior = behavior,
                    exitAnsweredHistoryBeforeIconNav = false,
                    clearNavigationHistoryOnManualJump = true,
                    doubleClickRequiresFullAnswerMode = true,
                    resumePendingAfterExitingAnsweredHistory = false,
                    usesFullAnswerSourceStayAdvance = false,
                    usesNextSourceEntryAdvance = false,
                    usesAdjacentDerivedAdvance = false,
                    usesReopenOnPostAnswerAdvance = false,
                )
        }
    }

    fun practiceDefault(): SessionNavigationOrchestration =
        from(
            SessionNavigationConfig(
                mode = SessionNavigationMode.PRACTICE_INTERACTIVE,
                swipeAnsweredHistory = true,
            ),
        )

    fun examDefault(): SessionNavigationOrchestration =
        from(
            SessionNavigationConfig(
                mode = SessionNavigationMode.EXAM_LINEAR,
                swipeAnsweredHistory = false,
            ),
        )
}
