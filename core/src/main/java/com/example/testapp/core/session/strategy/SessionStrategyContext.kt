package com.example.testapp.core.session.strategy

import com.example.testapp.core.session.policy.SessionStrategyFactory
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.exit.SessionExitContext
import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceContext
import com.example.testapp.domain.session.reveal.SessionRevealConfig

/** Kind + 上下文 → 四类 Policy 快照（Coordinator 收编入口） */
data class SessionStrategyContext(
    val persistence: SessionPersistenceConfig,
    val navigation: SessionNavigationConfig,
    val reveal: SessionRevealConfig,
    val exit: SessionExitConfig,
)

object SessionStrategyContexts {
    fun forKind(
        kind: QuestionSessionKind,
        persistenceContext: SessionPersistenceContext = SessionPersistenceContext(),
        exitContext: SessionExitContext = SessionExitContext(),
    ): SessionStrategyContext =
        SessionStrategyContext(
            persistence = SessionStrategyFactory.persistence(kind).config(persistenceContext),
            navigation = SessionStrategyFactory.navigation(kind).config(),
            reveal = SessionStrategyFactory.reveal(kind).config(),
            exit = SessionStrategyFactory.exit(kind, exitContext),
        )
}
