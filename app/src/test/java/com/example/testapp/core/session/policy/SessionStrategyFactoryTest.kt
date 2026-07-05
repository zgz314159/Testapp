package com.example.testapp.core.session.policy

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.navigation.SessionNavigationMode
import com.example.testapp.domain.session.persistence.SessionPersistenceContext
import com.example.testapp.domain.session.reveal.SessionRevealMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionStrategyFactoryTest {
    @Test
    fun browse_persistence_disablesSaveAndRestore() {
        val config =
            SessionStrategyFactory.persistence(QuestionSessionKind.Browse("f", 1))
                .config(SessionPersistenceContext())
        assertFalse(config.persistProgress)
        assertFalse(config.restoreProgress)
    }

    @Test
    fun questionEdit_matchesBrowsePersistence() {
        val config =
            SessionStrategyFactory.persistence(
                QuestionSessionKind.QuestionEdit("f", questionId = 1),
            ).config(SessionPersistenceContext())
        assertFalse(config.persistProgress)
        assertFalse(config.restoreProgress)
    }

    @Test
    fun review_navigation_usesHistoryMode() {
        val config =
            SessionStrategyFactory.navigation(
                QuestionSessionKind.Review("practice_f", null, null),
            ).config()
        assertEquals(SessionNavigationMode.REVIEW_HISTORY, config.mode)
        assertTrue(config.swipeAnsweredHistory)
    }

    @Test
    fun exam_reveal_waitsForSessionSubmit() {
        val config =
            SessionStrategyFactory.reveal(
                QuestionSessionKind.Exam("f", null, null, null),
            ).config()
        assertEquals(SessionRevealMode.ON_SESSION_SUBMIT, config.mode)
        assertFalse(config.autoAdvanceAfterReveal)
    }

    @Test
    fun examReview_reveal_isReadOnly() {
        val config =
            SessionStrategyFactory.reveal(
                QuestionSessionKind.Exam("f", null, null, reviewProgressId = "exam_f"),
            ).config()
        assertEquals(SessionRevealMode.READ_ONLY, config.mode)
    }
}
