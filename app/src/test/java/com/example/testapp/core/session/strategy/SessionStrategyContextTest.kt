package com.example.testapp.core.session.strategy

import com.example.testapp.core.session.policy.navigation.NavigationPolicyFactory
import com.example.testapp.core.session.policy.navigation.postAnswerPhases
import com.example.testapp.core.session.strategy.exit.SessionExitGate
import com.example.testapp.core.session.strategy.navigation.SessionNavigationBehaviorResolver
import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationGate
import com.example.testapp.core.session.strategy.navigation.SessionNavigationOrchestrationResolver
import com.example.testapp.core.session.strategy.persistence.SessionPersistenceGate
import com.example.testapp.core.session.strategy.reveal.SessionRevealGate
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitMode
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import com.example.testapp.domain.session.reveal.SessionRevealMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionStrategyContextTest {
    @Test
    fun review_persistence_disablesSaveOnNavigation() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Review("practice_f", null, null),
            )
        assertFalse(ctx.persistence.persistProgress)
        assertTrue(ctx.persistence.restoreProgress)
        assertFalse(ctx.persistence.saveOnNavigation)
    }

    @Test
    fun questionEdit_matchesBrowsePersistence() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.QuestionEdit("f", questionId = 1),
            )
        assertFalse(ctx.persistence.persistProgress)
        assertFalse(ctx.persistence.saveOnNavigation)
    }

    @Test
    fun navigationGate_reviewMode() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Review("practice_f", null, null),
            )
        assertTrue(SessionNavigationStrategyGate.reviewBrowseEnabled(ctx.navigation))
        assertEquals(SessionNavigationMode.REVIEW_HISTORY, ctx.navigation.mode)
    }

    @Test
    fun navigationBehavior_examLinear() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Exam("f", null, null, null),
            )
        val behavior = SessionNavigationBehaviorResolver.from(ctx.navigation)
        assertTrue(behavior.iconUnansweredNav)
        assertTrue(behavior.postAnswerSequentialAdvance)
        assertFalse(behavior.answeredHistoryBrowse)
        assertTrue(behavior.sequentialIndexNav)
        assertTrue(behavior.iconDoubleClickCrossSource)
    }

    @Test
    fun navigationBehavior_reviewHistory_respectsSwipeFlag() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Review("practice_f", null, null),
            )
        val behavior = SessionNavigationBehaviorResolver.from(ctx.navigation)
        assertFalse(behavior.iconUnansweredNav)
        assertFalse(behavior.postAnswerSequentialAdvance)
        assertTrue(behavior.answeredHistoryBrowse)
        assertFalse(behavior.sequentialIndexNav)
    }

    @Test
    fun navigationBehavior_examReview_disablesSequentialNav() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Exam("f", reviewProgressId = "rev_1"),
            )
        val behavior = SessionNavigationBehaviorResolver.from(ctx.navigation)
        assertFalse(behavior.sequentialIndexNav)
        assertTrue(behavior.answeredHistoryBrowse)
    }

    @Test
    fun navigationGate_behaviorMatchesResolver() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Review("practice_f", null, null),
            )
        val resolved = SessionNavigationBehaviorResolver.from(ctx.navigation)
        val gated = SessionNavigationStrategyGate.behavior(ctx.navigation)
        assertEquals(resolved, gated)
        assertEquals(
            resolved.answeredHistoryBrowse,
            SessionNavigationStrategyGate.swipeAnsweredHistoryEnabled(ctx.navigation),
        )
    }

    @Test
    fun navigationOrchestration_practiceInteractive() {
        val ctx = SessionStrategyContexts.forKind(QuestionSessionKind.Practice("f"))
        val orch = SessionNavigationOrchestrationResolver.from(ctx.navigation)
        assertTrue(orch.exitAnsweredHistoryBeforeIconNav)
        assertTrue(orch.clearNavigationHistoryOnManualJump)
        assertTrue(orch.doubleClickRequiresFullAnswerMode)
        assertTrue(orch.resumePendingAfterExitingAnsweredHistory)
        assertTrue(orch.usesFullAnswerSourceStayAdvance)
        assertTrue(orch.usesAdjacentDerivedAdvance)
        assertEquals(
            SessionNavigationBehaviorResolver.from(ctx.navigation),
            orch.behavior,
        )
    }

    @Test
    fun navigationOrchestrationGate_doubleClickRequiresFullAnswerMode() {
        val orch = SessionNavigationOrchestrationResolver.practiceDefault()
        assertFalse(
            SessionNavigationOrchestrationGate.allowsDoubleClickCrossSource(
                orch,
                fullAnswerModeActive = false,
            ),
        )
        assertTrue(
            SessionNavigationOrchestrationGate.allowsDoubleClickCrossSource(
                orch,
                fullAnswerModeActive = true,
            ),
        )
    }

    @Test
    fun postAnswerPhases_practicePolicy() {
        val phases = NavigationPolicyFactory.practice.postAnswerPhases()
        assertTrue(phases.resumePendingAfterExitingAnsweredHistory)
        assertTrue(phases.usesFullAnswerSourceStayAdvance)
        assertTrue(phases.usesReopenOnPostAnswerAdvance)
    }

    @Test
    fun persistenceGate_questionEdit() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.QuestionEdit("f", questionId = 1),
            )
        assertFalse(SessionPersistenceGate.shouldPersistProgress(ctx.persistence))
        assertFalse(SessionPersistenceGate.shouldSaveOnNavigation(ctx.persistence))
    }

    @Test
    fun revealGate_examSubmitOnly() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Exam("f", null, null, null),
            )
        assertTrue(SessionRevealGate.revealsOnSessionSubmit(ctx.reveal))
        assertFalse(SessionRevealGate.allowsExplicitReveal(ctx.reveal))
    }

    @Test
    fun exitGate_questionEdit_noSubmitDialog() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.QuestionEdit("f", questionId = 1),
            )
        assertEquals(SessionExitMode.BROWSE, ctx.exit.mode)
        assertFalse(SessionExitGate.allowsSubmitDialogOnExit(ctx.exit))
    }

    @Test
    fun exitGate_examReview() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Exam("f", reviewProgressId = "rev_1"),
            )
        assertEquals(SessionExitMode.REVIEW, ctx.exit.mode)
        assertTrue(SessionExitGate.isReviewBackExit(ctx.exit))
    }

    @Test
    fun questionEdit_matchesBrowseNavigationAndReveal() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.QuestionEdit("f", questionId = 1),
            )
        assertEquals(SessionNavigationMode.BROWSE_LINEAR, ctx.navigation.mode)
        assertFalse(ctx.navigation.swipeAnsweredHistory)
        assertEquals(SessionRevealMode.READ_ONLY, ctx.reveal.mode)
    }

    @Test
    fun navigationBehavior_practiceInteractive() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Practice("f"),
            )
        val behavior = SessionNavigationBehaviorResolver.from(ctx.navigation)
        assertTrue(behavior.iconUnansweredNav)
        assertTrue(behavior.answeredHistoryBrowse)
        assertFalse(behavior.sequentialIndexNav)
        assertTrue(behavior.iconDoubleClickCrossSource)
    }
}
