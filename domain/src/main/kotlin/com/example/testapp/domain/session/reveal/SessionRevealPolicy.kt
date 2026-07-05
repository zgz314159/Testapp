package com.example.testapp.domain.session.reveal

enum class SessionRevealMode {
    IMMEDIATE_ON_ANSWER,
    ON_SESSION_SUBMIT,
    READ_ONLY,
    MANUAL
}

data class SessionRevealConfig(
    val mode: SessionRevealMode,
    val autoAdvanceAfterReveal: Boolean
)

fun interface SessionRevealPolicy {
    fun config(): SessionRevealConfig
}
