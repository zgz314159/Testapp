package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.core.util.FullAnswerIconNavigationStrategyPipeline
import com.example.testapp.core.util.FullAnswerIconTapStrategy
import com.example.testapp.domain.session.navigation.SessionNavigationOrchestration

enum class SessionIconTapPath {
    DISABLED,
    ROUND_POOL,
    GLOBAL_UNANSWERED,
}

enum class SessionIconDoubleTapAction {
    DISABLED,
    CROSS_SOURCE,
    ROUND_POOL,
}

object SessionIconTapNavigationPipeline {
    fun resolveIconTapStrategy(
        fullAnswerModeActive: Boolean,
        multiRoundSession: Boolean,
    ): FullAnswerIconTapStrategy =
        FullAnswerIconNavigationStrategyPipeline.resolve(
            fullAnswerModeActive = fullAnswerModeActive,
            multiRoundSession = multiRoundSession,
        )

    fun resolveSingleTapPath(
        orchestration: SessionNavigationOrchestration,
        fullAnswerModeActive: Boolean,
        multiRoundSession: Boolean,
    ): SessionIconTapPath {
        if (!SessionNavigationOrchestrationGate.allowsIconNav(orchestration)) {
            return SessionIconTapPath.DISABLED
        }
        val strategy = resolveIconTapStrategy(fullAnswerModeActive, multiRoundSession)
        return if (FullAnswerIconNavigationStrategyPipeline.singleTapUsesRoundPool(strategy)) {
            SessionIconTapPath.ROUND_POOL
        } else {
            SessionIconTapPath.GLOBAL_UNANSWERED
        }
    }

    fun resolveDoubleTapAction(
        orchestration: SessionNavigationOrchestration,
        fullAnswerModeActive: Boolean,
        multiRoundSession: Boolean,
    ): SessionIconDoubleTapAction {
        if (!SessionNavigationOrchestrationGate.allowsDoubleClickCrossSource(
                orchestration,
                fullAnswerModeActive,
            )
        ) {
            return SessionIconDoubleTapAction.DISABLED
        }
        val strategy = resolveIconTapStrategy(fullAnswerModeActive, multiRoundSession)
        return if (FullAnswerIconNavigationStrategyPipeline.doubleTapUsesCrossSource(strategy)) {
            SessionIconDoubleTapAction.CROSS_SOURCE
        } else {
            SessionIconDoubleTapAction.ROUND_POOL
        }
    }

    fun shouldFallbackToCrossSourceOnSingleTap(
        strategy: FullAnswerIconTapStrategy,
        atBoundary: Boolean,
    ): Boolean =
        FullAnswerIconNavigationStrategyPipeline.shouldFallbackToCrossSourceOnSingleTap(
            strategy = strategy,
            atBoundary = atBoundary,
        )
}
