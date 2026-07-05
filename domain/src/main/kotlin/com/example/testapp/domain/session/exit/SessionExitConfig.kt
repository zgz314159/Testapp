package com.example.testapp.domain.session.exit

enum class SessionExitMode {
    BROWSE,
    REVIEW,
    PRACTICE,
    EXAM
}

data class SessionExitConfig(
    val mode: SessionExitMode
)

data class SessionExitContext(
    val isReviewMode: Boolean = false
)
