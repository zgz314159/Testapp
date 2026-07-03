package com.example.testapp.presentation.screen.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsExportRequestPipelineTest {

    @Test
    fun `single file returns direct export name`() {
        assertEquals("quiz-a", resolveDirectExportFileName(listOf("quiz-a")))
    }

    @Test
    fun `empty or multiple files require picker`() {
        assertNull(resolveDirectExportFileName(emptyList()))
        assertNull(resolveDirectExportFileName(listOf("a", "b")))
    }

    @Test
    fun `buildExportOutputName adds timestamp prefix`() {
        assertEquals("20260101_1200_quiz.xlsx", buildExportOutputName("quiz", "20260101_1200"))
    }
}
