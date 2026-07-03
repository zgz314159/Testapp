package com.example.testapp.uicommon.design

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AnswerChoiceCorrectColorPipelineTest {

    @Test
    fun resolveAnswerChoicePalette_lightMode_usesDistinctGreenAndRed() {
        val palette = resolveAnswerChoicePalette(
            darkTheme = false,
            surface = Color.White,
            secondaryContainer = Color.LightGray
        )
        assertEquals(AnswerChoiceCorrectColorTokens.correctContainerLight, palette.correct)
        assertEquals(AnswerChoiceCorrectColorTokens.wrongContainerLight, palette.wrong)
        assertNotEquals(palette.correct, palette.wrong)
    }
}
