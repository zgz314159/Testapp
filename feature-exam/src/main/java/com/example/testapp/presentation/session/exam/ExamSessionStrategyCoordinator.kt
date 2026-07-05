package com.example.testapp.presentation.session.exam

import com.example.testapp.core.session.strategy.SessionNavigationStrategyGate
import com.example.testapp.core.session.strategy.SessionStrategyContext
import com.example.testapp.core.session.strategy.SessionStrategyContexts
import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationResolver
import com.example.testapp.core.session.strategy.review.ReviewSessionStrategyBootstrap
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceContext
import com.example.testapp.domain.session.reveal.SessionRevealConfig

/** Exam 会话四类 Strategy 绑定与复盘进出快照 */
internal class ExamSessionStrategyCoordinator(
    private val progressId: () -> String,
    private val onStrategyApplied: (SessionStrategyContext) -> Unit,
) {
    private var strategyContext: SessionStrategyContext? = null
    private var preReviewExamKind: QuestionSessionKind.Exam? = null

    fun bindStrategy(
        kind: QuestionSessionKind,
        persistenceContext: SessionPersistenceContext = SessionPersistenceContext(),
    ) {
        applyStrategyContext(SessionStrategyContexts.forKind(kind, persistenceContext))
    }

    fun persistenceConfig(): SessionPersistenceConfig = activeContext().persistence

    fun navigationConfig(): SessionNavigationConfig = activeContext().navigation

    fun exitConfig(): SessionExitConfig = activeContext().exit

    fun revealConfig(): SessionRevealConfig = activeContext().reveal

    fun sessionStrategyConfig(): SessionStrategyContext = activeContext()

    fun navigationOrchestration() =
        strategyContext?.let { SessionNavigationOrchestrationResolver.from(it.navigation) }
            ?: SessionNavigationOrchestrationResolver.examDefault()

    fun reviewBrowseEnabled(): Boolean {
        val nav = strategyContext?.navigation ?: return false
        return SessionNavigationStrategyGate.reviewBrowseEnabled(nav)
    }

    fun capturePreReviewExamKind(
        quizFile: String,
        wrongBook: Boolean,
        favorite: Boolean,
    ) {
        if (preReviewExamKind == null) {
            preReviewExamKind =
                QuestionSessionKind.Exam(
                    quizId = quizFile,
                    wrongBookFileName = quizFile.takeIf { wrongBook },
                    favoriteFileName = quizFile.takeIf { favorite },
                )
        }
    }

    fun bindReviewStrategy(
        targetProgressId: String,
        quizFile: String,
        wrongBook: Boolean,
        favorite: Boolean,
    ) {
        bindStrategy(
            ReviewSessionStrategyBootstrap.examKind(
                targetProgressId,
                quizFile,
                wrongBook,
                favorite,
            ),
        )
    }

    fun restorePreReviewExamKindOrNull(): QuestionSessionKind.Exam? =
        preReviewExamKind.also { preReviewExamKind = null }

    private fun applyStrategyContext(context: SessionStrategyContext) {
        strategyContext = context
        onStrategyApplied(context)
    }

    private fun activeContext(): SessionStrategyContext =
        strategyContext ?: SessionStrategyContexts.forKind(QuestionSessionKind.Exam(progressId()))
}
