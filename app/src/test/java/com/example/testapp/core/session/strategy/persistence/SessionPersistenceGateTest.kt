package com.example.testapp.core.session.strategy.persistence

import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionPersistenceGateTest {
    @Test
    fun practice_fullPersistence() {
        val cfg =
            SessionPersistenceConfig(
                persistProgress = true,
                restoreProgress = true,
                saveOnNavigation = true,
            )
        assertTrue(SessionPersistenceGate.shouldPersistProgress(cfg))
        assertTrue(SessionPersistenceGate.shouldRestoreProgress(cfg))
        assertTrue(SessionPersistenceGate.shouldSaveOnNavigation(cfg))
    }

    @Test
    fun browse_noPersistence() {
        val cfg =
            SessionPersistenceConfig(
                persistProgress = false,
                restoreProgress = false,
                saveOnNavigation = false,
            )
        assertFalse(SessionPersistenceGate.shouldPersistProgress(cfg))
        assertFalse(SessionPersistenceGate.shouldSaveOnNavigation(cfg))
    }

    @Test
    fun review_restoreOnly() {
        val cfg =
            SessionPersistenceConfig(
                persistProgress = false,
                restoreProgress = true,
                saveOnNavigation = false,
            )
        assertFalse(SessionPersistenceGate.shouldPersistProgress(cfg))
        assertTrue(SessionPersistenceGate.shouldRestoreProgress(cfg))
        assertFalse(SessionPersistenceGate.shouldSaveOnNavigation(cfg))
    }
}
