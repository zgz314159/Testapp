package com.example.testapp.core.session.strategy.persistence

import com.example.testapp.domain.session.persistence.SessionPersistenceConfig

/** Persistence 行为门禁（Strategy 层） */
object SessionPersistenceGate {
    fun shouldPersistProgress(config: SessionPersistenceConfig): Boolean = config.persistProgress

    fun shouldRestoreProgress(config: SessionPersistenceConfig): Boolean = config.restoreProgress

    fun shouldSaveOnNavigation(config: SessionPersistenceConfig): Boolean =
        config.persistProgress && config.saveOnNavigation
}
