package com.example.testapp.core.session.policy.exit

import com.example.testapp.domain.session.exit.SessionExitAction
import com.example.testapp.domain.session.exit.SessionExitPolicy
import com.example.testapp.domain.session.exit.SessionExitRequest

object BrowseExitPolicy : SessionExitPolicy {
    override fun resolve(request: SessionExitRequest): SessionExitAction = SessionExitAction.ExitWithoutAnswer
}
