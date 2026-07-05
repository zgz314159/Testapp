package com.example.testapp.presentation.session.browse

import com.example.testapp.core.session.strategy.SessionStrategyContext
import com.example.testapp.core.session.strategy.SessionStrategyContexts
import com.example.testapp.domain.session.QuestionSessionKind

/** BrowseSession 启动时绑定四类 Strategy 快照 */
object BrowseSessionStrategyBootstrap {
    fun bind(kind: QuestionSessionKind.Browse): SessionStrategyContext = SessionStrategyContexts.forKind(kind)
}
