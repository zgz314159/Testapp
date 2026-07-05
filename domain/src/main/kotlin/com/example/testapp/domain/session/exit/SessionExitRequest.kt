package com.example.testapp.domain.session.exit

data class SessionExitRequest(
    val answeredThisSession: Boolean = false,
    val hasSessionInput: Boolean = false,
    val sessionAnsweredCount: Int = 0,
    val totalCount: Int = 0,
    val sessionScore: Int = 0,
    val realUnanswered: Int = 0,
    val hasPendingQuestions: Boolean = true
)
