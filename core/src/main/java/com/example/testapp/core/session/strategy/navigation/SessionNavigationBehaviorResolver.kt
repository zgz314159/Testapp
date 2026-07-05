package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.domain.session.navigation.SessionNavigationBehavior
import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.navigation.SessionNavigationMode

object SessionNavigationBehaviorResolver {
    fun from(config: SessionNavigationConfig): SessionNavigationBehavior =
        when (config.mode) {
            SessionNavigationMode.BROWSE_LINEAR ->
                SessionNavigationBehavior(
                    iconUnansweredNav = false,
                    postAnswerSequentialAdvance = true,
                    answeredHistoryBrowse = false,
                    sequentialIndexNav = true,
                    iconDoubleClickCrossSource = false,
                )
            SessionNavigationMode.PRACTICE_INTERACTIVE ->
                SessionNavigationBehavior(
                    iconUnansweredNav = true,
                    postAnswerSequentialAdvance = true,
                    answeredHistoryBrowse = config.swipeAnsweredHistory,
                    sequentialIndexNav = false,
                    iconDoubleClickCrossSource = true,
                )
            SessionNavigationMode.REVIEW_HISTORY ->
                SessionNavigationBehavior(
                    iconUnansweredNav = false,
                    postAnswerSequentialAdvance = false,
                    answeredHistoryBrowse = config.swipeAnsweredHistory,
                    sequentialIndexNav = false,
                    iconDoubleClickCrossSource = false,
                )
            SessionNavigationMode.EXAM_LINEAR ->
                SessionNavigationBehavior(
                    iconUnansweredNav = true,
                    postAnswerSequentialAdvance = true,
                    answeredHistoryBrowse = false,
                    sequentialIndexNav = true,
                    iconDoubleClickCrossSource = true,
                )
        }

    fun practiceDefault(): SessionNavigationBehavior =
        from(
            SessionNavigationConfig(
                mode = SessionNavigationMode.PRACTICE_INTERACTIVE,
                swipeAnsweredHistory = true,
            ),
        )

    fun examDefault(): SessionNavigationBehavior =
        from(
            SessionNavigationConfig(
                mode = SessionNavigationMode.EXAM_LINEAR,
                swipeAnsweredHistory = false,
            ),
        )
}
