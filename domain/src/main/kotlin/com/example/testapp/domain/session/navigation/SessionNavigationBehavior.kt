package com.example.testapp.domain.session.navigation

/** NavigationController / Engine 行为开关（由 SessionNavigationPolicy 推导） */
data class SessionNavigationBehavior(
    val iconUnansweredNav: Boolean,
    val postAnswerSequentialAdvance: Boolean,
    val answeredHistoryBrowse: Boolean,
    val sequentialIndexNav: Boolean,
    val iconDoubleClickCrossSource: Boolean
)
