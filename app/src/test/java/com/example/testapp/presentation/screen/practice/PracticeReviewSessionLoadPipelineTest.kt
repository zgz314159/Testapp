package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeReviewSessionLoadPipelineTest {
    private val question =
        Question(id = 1, content = "q", type = "单选", options = listOf("A"), answer = "A", explanation = "")

    @Test
    fun resolve_markLoadedOnly_whenProgressMissing() {
        val outcome = PracticeReviewSessionLoadPipeline.resolve(null, listOf(question))
        assertTrue(outcome is PracticeReviewSessionLoadPipeline.Outcome.MarkLoadedOnly)
    }

    @Test
    fun resolve_loadQuestions_whenProgressPresent() {
        val progress = PracticeProgress(
            currentIndex = 0,
            answeredList = emptyList(),
            selectedOptions = emptyList(),
            showResultList = emptyList(),
            analysisList = emptyList(),
            noteList = emptyList(),
            timestamp = 42L,
        )
        val outcome =
            PracticeReviewSessionLoadPipeline.resolve(progress, listOf(question)) as
                PracticeReviewSessionLoadPipeline.Outcome.LoadQuestions
        assertEquals(42L, outcome.sessionStartTime)
        assertEquals(listOf(question), outcome.sourceQuestions)
    }
}
