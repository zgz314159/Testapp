package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeProgressLoadQuestionsPipelineTest {
    private val coordinator = PracticeProgressCoordinator()
    private val questions =
        listOf(
            Question(id = 1, content = "q1", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f"),
            Question(id = 2, content = "q2", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f"),
        )

    @Test
    fun prepare_emptyCatalog_whenNoQuestions() = runBlocking {
        val outcome =
            PracticeProgressLoadQuestionsPipeline.prepare(
                originalQuestions = emptyList(),
                existingProgress = null,
                questionCount = 0,
                newSessionStartTime = 100L,
                fillConfig = PracticeFillConfig.default,
                sourceId = "f",
                randomPracticeEnabled = false,
                pinnedQuestionId = null,
                preserveCurrentIndex = null,
                progressCoordinator = coordinator,
                persistenceConfig = SessionPersistenceConfig(persistProgress = true, restoreProgress = true),
                progressId = "practice_p",
            )
        assertTrue(outcome is PracticeProgressLoadQuestionsPipeline.Outcome.Empty)
        val empty = outcome as PracticeProgressLoadQuestionsPipeline.Outcome.Empty
        assertEquals("practice_p", empty.catalog.progressId)
    }

    @Test
    fun prepare_ready_withFreshQuestions() = runBlocking {
        val outcome =
            PracticeProgressLoadQuestionsPipeline.prepare(
                originalQuestions = questions,
                existingProgress = null,
                questionCount = 2,
                newSessionStartTime = 200L,
                fillConfig = PracticeFillConfig.default,
                sourceId = "f",
                randomPracticeEnabled = false,
                pinnedQuestionId = null,
                preserveCurrentIndex = null,
                progressCoordinator = coordinator,
                persistenceConfig = SessionPersistenceConfig(persistProgress = true, restoreProgress = true),
                progressId = "practice_p",
            )
        assertTrue(outcome is PracticeProgressLoadQuestionsPipeline.Outcome.Ready)
        val loaded = (outcome as PracticeProgressLoadQuestionsPipeline.Outcome.Ready).loaded
        assertEquals(2, loaded.questionsWithState.size)
        assertEquals(2, loaded.sourceCatalogQuestions.size)
        assertTrue(loaded.newRoundProgress != null)
    }
}
