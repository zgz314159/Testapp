package com.example.testapp.core.session.strategy.exit

import com.example.testapp.core.session.policy.ExitPolicyFactory
import com.example.testapp.domain.session.exit.SessionExitAction
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.exit.SessionExitMode
import com.example.testapp.domain.session.exit.SessionExitRequest

/** Exit 行为门禁（Strategy 层） */
object SessionExitGate {
    fun resolveAction(
        config: SessionExitConfig,
        request: SessionExitRequest,
    ): SessionExitAction = ExitPolicyFactory.policyForConfig(config).resolve(request)

    fun allowsSubmitDialogOnExit(config: SessionExitConfig): Boolean =
        config.mode == SessionExitMode.PRACTICE || config.mode == SessionExitMode.EXAM

    fun isReviewBackExit(config: SessionExitConfig): Boolean = config.mode == SessionExitMode.REVIEW
}
