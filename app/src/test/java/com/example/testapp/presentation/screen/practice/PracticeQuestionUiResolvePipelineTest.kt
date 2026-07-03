package com.example.testapp.presentation.screen.practice

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeQuestionUiResolvePipelineTest {

    @Test
    fun selectedOptions_usesFallbackWhenUiIndexMismatch() {
        val ui = PracticeCurrentQuestionUi(
            index = 0,
            selectedOptions = listOf(2),
            textAnswer = "",
            showResult = true,
            analysis = "",
            sparkAnalysis = "",
            baiduAnalysis = "",
            note = ""
        )
        assertEquals(
            listOf(1),
            PracticeQuestionUiResolvePipeline.selectedOptions(ui, 1, listOf(listOf(), listOf(1)))
        )
    }
}
