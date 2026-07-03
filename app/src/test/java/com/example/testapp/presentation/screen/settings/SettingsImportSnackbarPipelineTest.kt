package com.example.testapp.presentation.screen.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsImportSnackbarPipelineTest {

    private val messages = ImportSnackbarMessages(
        success = "ok",
        failed = "fail",
        partial = { s, e, r -> "partial:$s:$e:$r" },
        failedWithReasons = { r -> "fail:$r" }
    )

    @Test
    fun `full success navigates home`() {
        val result = resolveImportSnackbarResult(2, success = true, errorFiles = emptyList(), messages)
        assertTrue(result.shouldNavigateHome)
        assertEquals("ok", result.message)
    }

    @Test
    fun `partial success navigates home when at least one succeeded`() {
        val result = resolveImportSnackbarResult(3, success = true, errorFiles = listOf("a.json"), messages)
        assertTrue(result.shouldNavigateHome)
    }

    @Test
    fun `total failure does not navigate home`() {
        val result = resolveImportSnackbarResult(1, success = false, errorFiles = listOf("bad"), messages)
        assertFalse(result.shouldNavigateHome)
    }
}
