package com.example.testapp.core.session.strategy.reveal

import com.example.testapp.domain.session.reveal.SessionRevealConfig
import com.example.testapp.domain.session.reveal.SessionRevealMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRevealGateTest {
    @Test
    fun practice_immediateRevealAndAutoAdvance() {
        val cfg = SessionRevealConfig(SessionRevealMode.IMMEDIATE_ON_ANSWER, autoAdvanceAfterReveal = true)
        assertTrue(SessionRevealGate.revealsOnAnswer(cfg))
        assertTrue(SessionRevealGate.allowsExplicitReveal(cfg))
        assertTrue(SessionRevealGate.shouldAutoAdvanceAfterReveal(cfg))
    }

    @Test
    fun exam_submitOnly_blocksExplicitReveal() {
        val cfg = SessionRevealConfig(SessionRevealMode.ON_SESSION_SUBMIT, autoAdvanceAfterReveal = false)
        assertTrue(SessionRevealGate.revealsOnSessionSubmit(cfg))
        assertFalse(SessionRevealGate.allowsExplicitReveal(cfg))
        assertFalse(SessionRevealGate.shouldAutoAdvanceAfterReveal(cfg))
    }

    @Test
    fun review_readOnly() {
        val cfg = SessionRevealConfig(SessionRevealMode.READ_ONLY, autoAdvanceAfterReveal = false)
        assertTrue(SessionRevealGate.isReadOnlyReveal(cfg))
        assertTrue(SessionRevealGate.allowsExplicitReveal(cfg))
    }
}
