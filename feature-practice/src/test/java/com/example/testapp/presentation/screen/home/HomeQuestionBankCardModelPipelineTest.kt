package com.example.testapp.presentation.screen.home

import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.model.HomeQuestionBankCardModelPipeline
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeQuestionBankCardModelPipelineTest {

    @Test
    fun `buildList derives display progress and visual for each file`() {
        val models = HomeQuestionBankCardModelPipeline.buildList(
            fileNames = listOf("math.xlsx", "blank.txt"),
            fileStatistics = mapOf(
                "math.xlsx" to FileStatistics(questionCount = 20, wrongCount = 2, favoriteCount = 1),
                "blank.txt" to FileStatistics(questionCount = 10, wrongCount = 0, favoriteCount = 0),
            ),
            practiceProgress = mapOf("math.xlsx" to 5),
        )

        assertEquals(2, models.size)
        assertEquals("math", models[0].displayName)
        assertEquals(25, models[0].progressPercent)
        assertEquals(5, models[0].progressCount)
        assertEquals(HomeQuestionBankVisualKind.Spreadsheet, models[0].visualKind)
        assertEquals("blank", models[1].displayName)
        assertEquals(0, models[1].progressPercent)
    }
}
