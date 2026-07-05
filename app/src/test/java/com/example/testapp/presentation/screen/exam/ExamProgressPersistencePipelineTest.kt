package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamProgressPersistencePipelineTest {
    private val progress =
        ExamProgress(
            currentIndex = 0,
            selectedOptions = listOf(listOf(0)),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            finished = false,
            timestamp = 1L,
        )

    @Test
    fun exam_fullPersistence_enabled() {
        val cfg =
            SessionPersistenceConfig(
                persistProgress = true,
                restoreProgress = true,
                saveOnNavigation = true,
            )
        assertTrue(ExamProgressPersistencePipeline.shouldPersist(cfg))
        assertTrue(ExamProgressPersistencePipeline.shouldSaveOnNavigation(cfg))
    }

    @Test
    fun review_restoreOnly_disablesPersist() {
        val cfg =
            SessionPersistenceConfig(
                persistProgress = false,
                restoreProgress = true,
                saveOnNavigation = false,
            )
        assertFalse(ExamProgressPersistencePipeline.shouldPersist(cfg))
        assertFalse(ExamProgressPersistencePipeline.shouldSaveOnNavigation(cfg))
    }

    @Test
    fun shouldRestoreAnswersFromMap_respectsConfigAndReviewMode() {
        val full =
            SessionPersistenceConfig(
                persistProgress = true,
                restoreProgress = true,
                saveOnNavigation = true,
            )
        val browse =
            SessionPersistenceConfig(
                persistProgress = false,
                restoreProgress = false,
                saveOnNavigation = false,
            )
        assertTrue(
            ExamProgressPersistencePipeline.shouldRestoreAnswersFromMap(
                full,
                progress,
                reviewMode = true,
            ),
        )
        assertFalse(ExamProgressPersistencePipeline.shouldRestoreAnswersFromMap(browse, progress))
        assertFalse(ExamProgressPersistencePipeline.shouldRestoreAnswersFromMap(full, null))
    }
}
