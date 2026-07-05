package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeProgressPersistencePipelineTest {
    private val full =
        SessionPersistenceConfig(
            persistProgress = true,
            restoreProgress = true,
            saveOnNavigation = true,
        )
    private val browse =
        SessionPersistenceConfig(
            persistProgress = false,
            restoreProgress = false,
            saveOnNavigation = false,
        )

    private fun progressWithPartialAnswers() =
        PracticeProgress(
            id = "practice_p",
            currentIndex = 0,
            answeredList = listOf(0),
            selectedOptions = listOf(listOf(0), emptyList()),
            showResultList = listOf(false, false),
            analysisList = listOf("", ""),
            noteList = listOf("", ""),
            timestamp = 1L,
            fixedQuestionOrder = listOf(1, 2),
            questionStateMap =
                mapOf(
                    1 to
                        UnifiedQuestionState(
                            questionId = 1,
                            selectedOptions = listOf(0),
                            showResult = false,
                        ),
                ),
        )

    @Test
    fun shouldRestore_requiresGateAndProgressMap() {
        val progress = progressWithPartialAnswers()
        assertTrue(PracticeProgressPersistencePipeline.shouldRestoreAnswersFromMap(full, progress))
        assertFalse(PracticeProgressPersistencePipeline.shouldRestoreAnswersFromMap(browse, progress))
        assertFalse(PracticeProgressPersistencePipeline.shouldRestoreAnswersFromMap(full, null))
    }

    @Test
    fun browse_disablesPersistAndNavigationSave() {
        assertFalse(PracticeProgressPersistencePipeline.shouldPersist(browse))
        assertFalse(PracticeProgressPersistencePipeline.shouldSaveOnNavigation(browse))
        assertTrue(PracticeProgressPersistencePipeline.shouldPersist(full))
    }
}
