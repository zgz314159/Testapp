package com.example.testapp.domain.session.navigation

enum class SessionNavigationMode {
    BROWSE_LINEAR,
    PRACTICE_INTERACTIVE,
    REVIEW_HISTORY,
    EXAM_LINEAR
}

data class SessionNavigationConfig(
    val mode: SessionNavigationMode,
    val swipeAnsweredHistory: Boolean
)

fun interface SessionNavigationPolicy {
    fun config(): SessionNavigationConfig
}
