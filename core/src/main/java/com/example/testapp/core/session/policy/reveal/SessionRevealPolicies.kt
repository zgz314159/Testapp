package com.example.testapp.core.session.policy.reveal

import com.example.testapp.domain.session.reveal.SessionRevealConfig
import com.example.testapp.domain.session.reveal.SessionRevealMode
import com.example.testapp.domain.session.reveal.SessionRevealPolicy

object BrowseRevealPolicy : SessionRevealPolicy {
    override fun config() =
        SessionRevealConfig(
            mode = SessionRevealMode.READ_ONLY,
            autoAdvanceAfterReveal = false,
        )
}

object PracticeRevealPolicy : SessionRevealPolicy {
    override fun config() =
        SessionRevealConfig(
            mode = SessionRevealMode.IMMEDIATE_ON_ANSWER,
            autoAdvanceAfterReveal = true,
        )
}

object ReviewRevealPolicy : SessionRevealPolicy {
    override fun config() =
        SessionRevealConfig(
            mode = SessionRevealMode.READ_ONLY,
            autoAdvanceAfterReveal = false,
        )
}

object ExamRevealPolicy : SessionRevealPolicy {
    override fun config() =
        SessionRevealConfig(
            mode = SessionRevealMode.ON_SESSION_SUBMIT,
            autoAdvanceAfterReveal = false,
        )
}
