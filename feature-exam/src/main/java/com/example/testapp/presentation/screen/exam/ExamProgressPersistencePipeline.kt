package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.session.strategy.persistence.SessionPersistenceGate
import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig

/** 考试进度持久化判定（对称 PracticeProgressPersistencePipeline） */
object ExamProgressPersistencePipeline {
    fun shouldRestoreAnswersFromMap(
        config: SessionPersistenceConfig,
        progress: ExamProgress?,
        reviewMode: Boolean = false,
    ): Boolean =
        SessionPersistenceGate.shouldRestoreProgress(config) &&
            (progress?.let {
                ExamSessionRestorePipeline.shouldRestoreAnswersFromStateMap(it, reviewMode)
            } ?: false)

    fun shouldPersist(config: SessionPersistenceConfig): Boolean = SessionPersistenceGate.shouldPersistProgress(config)

    fun shouldSaveOnNavigation(config: SessionPersistenceConfig): Boolean =
        SessionPersistenceGate.shouldSaveOnNavigation(config)
}
