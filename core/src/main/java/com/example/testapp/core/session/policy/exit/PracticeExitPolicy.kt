package com.example.testapp.core.session.policy.exit

import com.example.testapp.domain.session.exit.SessionExitAction
import com.example.testapp.domain.session.exit.SessionExitPolicy
import com.example.testapp.domain.session.exit.SessionExitRequest

object PracticeExitPolicy : SessionExitPolicy {
    override fun resolve(request: SessionExitRequest): SessionExitAction =
        when {
            !request.answeredThisSession && !request.hasSessionInput ->
                SessionExitAction.ExitWithoutAnswer
            request.sessionAnsweredCount >= request.totalCount ->
                SessionExitAction.FinishWithStats(
                    sessionScore = request.sessionScore,
                    sessionAnsweredCount = request.sessionAnsweredCount,
                    realUnanswered = request.realUnanswered,
                )
            else -> SessionExitAction.ShowSubmitDialog
        }
}
