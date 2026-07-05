package com.example.testapp.core.session.policy.exit

import com.example.testapp.domain.session.exit.SessionExitAction
import com.example.testapp.domain.session.exit.SessionExitPolicy
import com.example.testapp.domain.session.exit.SessionExitRequest

object ReviewExitPolicy : SessionExitPolicy {
    override fun resolve(request: SessionExitRequest): SessionExitAction = SessionExitAction.ReviewBack
}
