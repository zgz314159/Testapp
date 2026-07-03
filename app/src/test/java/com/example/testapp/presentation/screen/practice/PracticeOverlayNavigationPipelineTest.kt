package com.example.testapp.presentation.screen.practice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PracticeOverlayNavigationPipelineTest {

    @Test
    fun restoreIndex_whenDrifted_returnsAnchorIndex() {
        val anchor = PracticeOverlayNavigationPipeline.capture(5, 100)
        assertEquals(5, PracticeOverlayNavigationPipeline.restoreIndex(anchor, 6))
    }

    @Test
    fun restoreIndex_whenUnchanged_returnsNull() {
        val anchor = PracticeOverlayNavigationPipeline.capture(5, 100)
        assertNull(PracticeOverlayNavigationPipeline.restoreIndex(anchor, 5))
    }

    @Test
    fun restoreIndex_withoutAnchor_returnsNull() {
        assertNull(PracticeOverlayNavigationPipeline.restoreIndex(null, 3))
    }
}
