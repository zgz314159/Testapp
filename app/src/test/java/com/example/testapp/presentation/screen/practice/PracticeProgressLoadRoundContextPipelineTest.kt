package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeProgressLoadRoundContextPipelineTest {
    private val coordinator = PracticeProgressCoordinator()
    private val questions =
        listOf(
            Question(id = 1, content = "q1", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f"),
            Question(id = 2, content = "q2", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f"),
        )

    @Test
    fun shouldWriteNewRoundProgress_whenNoSavedOrder() {
        val context =
            PracticeProgressLoadRoundContextPipeline.build(
                existingProgress = null,
                originalQuestions = questions,
                questionCount = 0,
                canReuseByFill = true,
                curFillSignature = "sig",
                newSessionStartTime = 100L,
                progressCoordinator = coordinator,
            )
        assertTrue(PracticeProgressLoadRoundContextPipeline.shouldWriteNewRoundProgress(context))
    }

    @Test
    fun canReuseSavedOrder_whenPartialProgress() {
        val progress =
            PracticeProgress(
                id = "practice_p",
                currentIndex = 0,
                answeredList = emptyList(),
                selectedOptions = emptyList(),
                showResultList = emptyList(),
                analysisList = emptyList(),
                noteList = emptyList(),
                fixedQuestionOrder = listOf(1, 2),
                questionStateMap =
                    mapOf(
                        1 to UnifiedQuestionState(questionId = 1, selectedOptions = listOf(0)),
                    ),
                timestamp = 1L,
            )
        val context =
            PracticeProgressLoadRoundContextPipeline.build(
                existingProgress = progress,
                originalQuestions = questions,
                questionCount = 2,
                canReuseByFill = true,
                curFillSignature = "sig",
                newSessionStartTime = 100L,
                progressCoordinator = coordinator,
            )
        assertTrue(context.canReuseSavedOrder)
        assertFalse(PracticeProgressLoadRoundContextPipeline.shouldWriteNewRoundProgress(context))
    }

    @Test
    fun maybeUpgradeFillSignature_whenLegacySessionId() {
        val progress =
            PracticeProgress(
                id = "practice_p",
                currentIndex = 0,
                answeredList = emptyList(),
                selectedOptions = emptyList(),
                showResultList = emptyList(),
                analysisList = emptyList(),
                noteList = emptyList(),
                sessionId = "practice_p_99",
                timestamp = 99L,
            )
        val upgrade =
            PracticeProgressLoadRoundContextPipeline.maybeUpgradeFillSignature(
                existingProgress = progress,
                canReuseByFill = true,
                curFillSignature = "mode|sig",
                progressId = "practice_p",
                newSessionStartTime = 100L,
                progressCoordinator = coordinator,
            )
        assertTrue(upgrade?.progress?.sessionId?.contains("|fill=mode|sig") == true)
    }
}
