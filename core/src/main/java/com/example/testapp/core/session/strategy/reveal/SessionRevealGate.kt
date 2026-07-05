package com.example.testapp.core.session.strategy.reveal

import com.example.testapp.domain.session.reveal.SessionRevealConfig
import com.example.testapp.domain.session.reveal.SessionRevealMode

/** Reveal 行为门禁（Strategy 层） */
object SessionRevealGate {
    fun revealsOnAnswer(config: SessionRevealConfig): Boolean = config.mode == SessionRevealMode.IMMEDIATE_ON_ANSWER

    fun revealsOnSessionSubmit(config: SessionRevealConfig): Boolean =
        config.mode == SessionRevealMode.ON_SESSION_SUBMIT

    fun isReadOnlyReveal(config: SessionRevealConfig): Boolean = config.mode == SessionRevealMode.READ_ONLY

    fun allowsExplicitReveal(config: SessionRevealConfig): Boolean =
        when (config.mode) {
            SessionRevealMode.ON_SESSION_SUBMIT -> false
            SessionRevealMode.IMMEDIATE_ON_ANSWER,
            SessionRevealMode.READ_ONLY,
            SessionRevealMode.MANUAL,
            -> true
        }

    fun shouldAutoAdvanceAfterReveal(config: SessionRevealConfig): Boolean =
        config.autoAdvanceAfterReveal && revealsOnAnswer(config)
}
