package com.example.testapp.core.session.policy.navigation

import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import com.example.testapp.domain.session.navigation.SessionNavigationPolicy

object BrowseNavigationPolicy : SessionNavigationPolicy {
    override fun config() =
        SessionNavigationConfig(
            mode = SessionNavigationMode.BROWSE_LINEAR,
            swipeAnsweredHistory = false,
        )
}

object PracticeNavigationPolicy : SessionNavigationPolicy {
    override fun config() =
        SessionNavigationConfig(
            mode = SessionNavigationMode.PRACTICE_INTERACTIVE,
            swipeAnsweredHistory = true,
        )
}

object ReviewNavigationPolicy : SessionNavigationPolicy {
    override fun config() =
        SessionNavigationConfig(
            mode = SessionNavigationMode.REVIEW_HISTORY,
            swipeAnsweredHistory = true,
        )
}

object ExamNavigationPolicy : SessionNavigationPolicy {
    override fun config() =
        SessionNavigationConfig(
            mode = SessionNavigationMode.EXAM_LINEAR,
            swipeAnsweredHistory = false,
        )
}
