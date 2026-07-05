package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.session.strategy.persistence.SessionPersistenceGate
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig

/** 进度持久化判定（从 PracticeProgressLifecycleCoordinator 收编） */
object PracticeProgressPersistencePipeline {
    fun shouldRestoreAnswersFromMap(
        config: SessionPersistenceConfig,
        progress: PracticeProgress?,
    ): Boolean =
        SessionPersistenceGate.shouldRestoreProgress(config) &&
            PracticeSessionRestorePipeline.shouldRestoreAnswersFromMap(progress)

    fun shouldPersist(config: SessionPersistenceConfig): Boolean = SessionPersistenceGate.shouldPersistProgress(config)

    fun shouldSaveOnNavigation(config: SessionPersistenceConfig): Boolean =
        SessionPersistenceGate.shouldSaveOnNavigation(config)
}
