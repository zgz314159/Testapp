package com.example.testapp.uicommon.design

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuestionSessionHistorySwipePipelineTest {

    @Test
    fun resolve_ignoresMostlyVerticalDrag() {
        assertNull(QuestionSessionHistorySwipePipeline.resolve(120f, 200f))
    }

    @Test
    fun resolve_detectsHorizontalOlder() {
        assertEquals(
            QuestionSessionHistorySwipeDirection.Older,
            QuestionSessionHistorySwipePipeline.resolve(150f, 20f)
        )
    }

    @Test
    fun resolve_detectsHorizontalNewer() {
        assertEquals(
            QuestionSessionHistorySwipeDirection.Newer,
            QuestionSessionHistorySwipePipeline.resolve(-150f, 10f)
        )
    }
}
