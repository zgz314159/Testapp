package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeSessionAnalysisMergePipelineTest {

    private fun qws(id: Int, analysis: String = "", showResult: Boolean = false) = QuestionWithState(
        question = Question(id = id, content = "c", type = "单选", options = emptyList(), answer = "A", explanation = ""),
        analysis = analysis,
        showResult = showResult
    )

    @Test
    fun mergeSupplementaryLoad_keepsLatestShowResultAndInMemoryAnalysis() {
        val latest = listOf(qws(1, analysis = "memory", showResult = true))
        val loaded = listOf(qws(1, analysis = "db"))
        val merged = PracticeSessionAnalysisMergePipeline.mergeSupplementaryLoad(latest, loaded)
        assertEquals("memory", merged[0].analysis)
        assertEquals(true, merged[0].showResult)
    }

    @Test
    fun mergeSupplementaryLoad_fillsBlankFromLoaded() {
        val latest = listOf(qws(1))
        val loaded = listOf(qws(1, analysis = "db"))
        val merged = PracticeSessionAnalysisMergePipeline.mergeSupplementaryLoad(latest, loaded)
        assertEquals("db", merged[0].analysis)
    }
}
