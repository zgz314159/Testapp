package com.example.testapp.uicommon.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AnswerCorrectHighlightColorPipelineTest {

    @Test
    fun resolve_lightMode_usesCursorStyleGreen() {
        val colors = AnswerCorrectHighlightColorPipeline.resolve(darkTheme = false)
        assertEquals(AnswerCorrectHighlightTokens.containerLight, colors.container)
        assertEquals(AnswerCorrectHighlightTokens.contentLight, colors.content)
    }

    @Test
    fun resolve_darkMode_differsFromLight() {
        val light = AnswerCorrectHighlightColorPipeline.resolve(darkTheme = false)
        val dark = AnswerCorrectHighlightColorPipeline.resolve(darkTheme = true)
        assertNotEquals(light.container, dark.container)
        assertEquals(AnswerCorrectHighlightTokens.contentDark, dark.content)
    }
}
