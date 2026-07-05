package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamReviewSessionLoadPipelineTest {
    private val question =
        Question(id = 1, content = "q", type = "单选", options = listOf("A"), answer = "A", explanation = "")

    @Test
    fun resolve_markLoadedOnly_whenProgressMissing() {
        val outcome = ExamReviewSessionLoadPipeline.resolve(null, listOf(question))
        assertTrue(outcome is ExamReviewSessionLoadPipeline.Outcome.MarkLoadedOnly)
    }

    @Test
    fun resolve_loadQuestions_whenProgressPresent() {
        val progress =
            ExamProgress(
                currentIndex = 0,
                selectedOptions = emptyList(),
                showResultList = emptyList(),
                analysisList = emptyList(),
                noteList = emptyList(),
                finished = false,
                timestamp = 99L,
            )
        val outcome =
            ExamReviewSessionLoadPipeline.resolve(progress, listOf(question)) as
                ExamReviewSessionLoadPipeline.Outcome.LoadQuestions
        assertEquals(99L, outcome.sessionStartTime)
        assertEquals(listOf(question), outcome.sourceQuestions)
    }
}
