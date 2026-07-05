package com.example.testapp.core.session.policy.exit

import com.example.testapp.domain.session.exit.SessionExitAction
import com.example.testapp.domain.session.exit.SessionExitPolicy
import com.example.testapp.domain.session.exit.SessionExitRequest

object ExamExitPolicy : SessionExitPolicy {
    override fun resolve(request: SessionExitRequest): SessionExitAction =
        when {
            !request.answeredThisSession -> SessionExitAction.ExitWithoutAnswer
            request.hasPendingQuestions -> SessionExitAction.ShowSubmitDialog
            else -> SessionExitAction.FinishDirect
        }
}
