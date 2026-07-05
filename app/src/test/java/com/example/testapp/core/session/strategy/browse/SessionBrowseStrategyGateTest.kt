package com.example.testapp.core.session.strategy.browse

import com.example.testapp.core.session.strategy.SessionStrategyContexts
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import com.example.testapp.domain.session.reveal.SessionRevealMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionBrowseStrategyGateTest {
    @Test
    fun browseKind_linearNavigation_readOnlyReveal() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.Browse("f", targetQuestionId = 1),
            )
        assertTrue(SessionBrowseStrategyGate.isLinearBrowseNavigation(ctx))
        assertTrue(SessionBrowseStrategyGate.disablesProgressPersistence(ctx))
        assertTrue(SessionBrowseStrategyGate.isReadOnlyReveal(ctx))
    }

    @Test
    fun questionEdit_matchesBrowsePolicies() {
        val ctx =
            SessionStrategyContexts.forKind(
                QuestionSessionKind.QuestionEdit("f", questionId = 1),
            )
        assertEquals(SessionNavigationMode.BROWSE_LINEAR, ctx.navigation.mode)
        assertFalse(ctx.navigation.swipeAnsweredHistory)
        assertEquals(SessionRevealMode.READ_ONLY, ctx.reveal.mode)
        assertTrue(SessionBrowseStrategyGate.disablesProgressPersistence(ctx))
    }
}
