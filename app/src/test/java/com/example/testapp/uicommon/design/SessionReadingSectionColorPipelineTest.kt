package com.example.testapp.uicommon.design

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionReadingSectionColorPipelineTest {

    @Test
    fun resolveSessionReadingSectionColors_distinctTonesInLightMode() {
        val explanation = resolveSessionReadingSectionColors(AnalysisSectionTone.Explanation, darkTheme = false)
        val deepSeek = resolveSessionReadingSectionColors(AnalysisSectionTone.DeepSeek, darkTheme = false)
        assertNotEquals(explanation.container, deepSeek.container)
        assertEquals(SessionReadingSectionTokens.explanationContainerLight, explanation.container)
    }

    @Test
    fun resolveSessionReadingAnswerFeedbackColors_usesMutedResultContainer() {
        val colors = resolveSessionReadingAnswerFeedbackColors(darkTheme = false)
        assertEquals(SessionReadingSectionTokens.resultContainerLight, colors.resultContainer)
        assertEquals(SessionReadingSectionTokens.correctTextLight, colors.correctText)
        assertEquals(SessionReadingSectionTokens.correctFieldLight, colors.correctFieldBackground)
    }
}
