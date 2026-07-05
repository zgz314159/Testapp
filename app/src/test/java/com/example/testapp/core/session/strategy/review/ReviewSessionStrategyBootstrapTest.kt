package com.example.testapp.core.session.strategy.review

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitMode
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSessionStrategyBootstrapTest {
    @Test
    fun practiceKind_reviewNavigationAndExit() {
        val kind = ReviewSessionStrategyBootstrap.practiceKind("practice_f__scope=q=10;r=0")
        val ctx = ReviewSessionStrategyBootstrap.contextForKind(kind)
        assertTrue(kind is QuestionSessionKind.Review)
        assertEquals(SessionNavigationMode.REVIEW_HISTORY, ctx.navigation.mode)
        assertEquals(SessionExitMode.REVIEW, ctx.exit.mode)
    }

    @Test
    fun examKind_reviewProgressUsesReviewPolicies() {
        val kind =
            ReviewSessionStrategyBootstrap.examKind(
                targetProgressId = "exam_f__scope=q=10",
                quizFile = "f.json",
                wrongBook = false,
                favorite = false,
            )
        val ctx = ReviewSessionStrategyBootstrap.contextForKind(kind)
        assertEquals("exam_f__scope=q=10", (kind as QuestionSessionKind.Exam).reviewProgressId)
        assertEquals(SessionNavigationMode.REVIEW_HISTORY, ctx.navigation.mode)
        assertEquals(SessionExitMode.REVIEW, ctx.exit.mode)
    }
}
