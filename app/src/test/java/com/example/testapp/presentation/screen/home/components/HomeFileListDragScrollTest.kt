package com.example.testapp.presentation.screen.home.components

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeFileListDragScrollTest {
    @Test
    fun scrollDelta_negative_whenAboveTopEdge() {
        assertEquals(-36f, HomeFileListDragScroll.scrollDelta(50f, 100f, 500f))
    }

    @Test
    fun scrollDelta_positive_whenBelowBottomEdge() {
        assertEquals(36f, HomeFileListDragScroll.scrollDelta(450f, 100f, 400f))
    }

    @Test
    fun scrollDelta_zero_whenInsideBounds() {
        assertEquals(0f, HomeFileListDragScroll.scrollDelta(250f, 100f, 400f))
    }
}
