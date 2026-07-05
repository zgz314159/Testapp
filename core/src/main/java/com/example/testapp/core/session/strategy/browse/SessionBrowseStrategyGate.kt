package com.example.testapp.core.session.strategy.browse

import com.example.testapp.core.session.strategy.SessionStrategyContext
import com.example.testapp.core.session.strategy.persistence.SessionPersistenceGate
import com.example.testapp.core.session.strategy.reveal.SessionRevealGate
import com.example.testapp.domain.session.navigation.SessionNavigationMode

/** Browse / drawer-browse 行为门禁 */
object SessionBrowseStrategyGate {
    fun isLinearBrowseNavigation(ctx: SessionStrategyContext): Boolean =
        ctx.navigation.mode == SessionNavigationMode.BROWSE_LINEAR

    fun disablesProgressPersistence(ctx: SessionStrategyContext): Boolean =
        !SessionPersistenceGate.shouldPersistProgress(ctx.persistence)

    fun isReadOnlyReveal(ctx: SessionStrategyContext): Boolean = SessionRevealGate.isReadOnlyReveal(ctx.reveal)
}
