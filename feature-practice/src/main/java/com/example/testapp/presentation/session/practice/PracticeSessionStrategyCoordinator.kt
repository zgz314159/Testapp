package com.example.testapp.presentation.session.practice

import com.example.testapp.core.session.strategy.SessionNavigationStrategyGate
import com.example.testapp.core.session.strategy.SessionStrategyContext
import com.example.testapp.core.session.strategy.SessionStrategyContexts
import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationResolver
import com.example.testapp.core.session.strategy.review.ReviewSessionStrategyBootstrap
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import com.example.testapp.domain.session.reveal.SessionRevealConfig

/** Practice 会话四类 Strategy 绑定与复盘进出快照 */
internal class PracticeSessionStrategyCoordinator(
    private val progressId: () -> String,
    private val onStrategyApplied: (SessionStrategyContext) -> Unit,
) {
    private var strategyContext: SessionStrategyContext? = null
    private var sessionKind: QuestionSessionKind? = null
    private var preReviewSnapshot: StrategyBindSnapshot? = null

    internal data class StrategyBindSnapshot(val kind: QuestionSessionKind)

    fun bindStrategy(kind: QuestionSessionKind) {
        sessionKind = kind
        applyStrategyContext(SessionStrategyContexts.forKind(kind))
    }

    fun persistenceConfig(): SessionPersistenceConfig = activeContext().persistence

    fun navigationConfig(): SessionNavigationConfig = activeContext().navigation

    fun exitConfig(): SessionExitConfig = activeContext().exit

    fun revealConfig(): SessionRevealConfig = activeContext().reveal

    fun sessionStrategyConfig(): SessionStrategyContext = activeContext()

    fun navigationOrchestration() =
        strategyContext?.let { SessionNavigationOrchestrationResolver.from(it.navigation) }
            ?: SessionNavigationOrchestrationResolver.practiceDefault()

    fun reviewBrowseEnabled(): Boolean {
        val nav = strategyContext?.navigation ?: return false
        return SessionNavigationStrategyGate.reviewBrowseEnabled(nav)
    }

    fun capturePreReviewIfNeeded() {
        if (preReviewSnapshot == null && sessionKind != null) {
            preReviewSnapshot = StrategyBindSnapshot(sessionKind!!)
        }
    }

    fun bindReviewStrategy(targetProgressId: String) {
        bindStrategy(ReviewSessionStrategyBootstrap.practiceKind(targetProgressId))
    }

    fun restorePreReviewOrNull(): StrategyBindSnapshot? = preReviewSnapshot.also { preReviewSnapshot = null }

    private fun applyStrategyContext(context: SessionStrategyContext) {
        strategyContext = context
        onStrategyApplied(context)
    }

    private fun activeContext(): SessionStrategyContext =
        strategyContext ?: SessionStrategyContexts.forKind(
            sessionKind ?: QuestionSessionKind.Practice(progressId()),
        )
}
