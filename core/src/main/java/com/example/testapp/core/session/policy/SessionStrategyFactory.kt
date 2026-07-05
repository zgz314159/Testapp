package com.example.testapp.core.session.policy

import com.example.testapp.core.session.policy.navigation.NavigationPolicyFactory
import com.example.testapp.core.session.policy.persistence.PersistencePolicyFactory
import com.example.testapp.core.session.policy.reveal.RevealPolicyFactory
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.exit.SessionExitContext
import com.example.testapp.domain.session.navigation.SessionNavigationPolicy
import com.example.testapp.domain.session.persistence.SessionPersistencePolicy
import com.example.testapp.domain.session.reveal.SessionRevealPolicy

/** Kind → Persistence / Navigation / Reveal / Exit 策略（ADR-002 收编） */
object SessionStrategyFactory {
    fun persistence(kind: QuestionSessionKind): SessionPersistencePolicy = PersistencePolicyFactory.forKind(kind)

    fun navigation(kind: QuestionSessionKind): SessionNavigationPolicy = NavigationPolicyFactory.forKind(kind)

    fun reveal(kind: QuestionSessionKind): SessionRevealPolicy = RevealPolicyFactory.forKind(kind)

    fun exit(
        kind: QuestionSessionKind,
        context: SessionExitContext = SessionExitContext(),
    ): SessionExitConfig = ExitPolicyFactory.configForKind(kind, context)
}
