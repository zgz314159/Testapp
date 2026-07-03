package com.example.testapp.uicommon.design

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionSessionChromeInsetsPipelineTest {

    @Test
    fun scrollInsets_matchChromeMetrics() {
        assertEquals(PracticeExamTopBarMetrics.barHeight, QuestionSessionChromeInsetsPipeline.scrollTopInset())
        assertEquals(QuestionSessionBottomNavMetrics.barHeight, QuestionSessionChromeInsetsPipeline.scrollBottomInset())
    }
}
