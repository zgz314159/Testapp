package com.example.testapp.core.session.route

import com.example.testapp.domain.session.QuestionSessionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowseSessionRoutePipelineTest {
    @Test
    fun shouldUseBrowseSession_positiveId() {
        assertTrue(BrowseSessionRoutePipeline.shouldUseBrowseSession(42))
    }

    @Test
    fun shouldUseBrowseSession_nullOrNegative() {
        assertFalse(BrowseSessionRoutePipeline.shouldUseBrowseSession(null))
        assertFalse(BrowseSessionRoutePipeline.shouldUseBrowseSession(-1))
    }

    @Test
    fun shouldUseBrowseSession_zeroMeansBrowseFromStart() {
        assertTrue(BrowseSessionRoutePipeline.shouldUseBrowseSession(0))
    }

    @Test
    fun browseKind_wrapsTargetQuestionId() {
        val kind = BrowseSessionRoutePipeline.browseKind("quiz.json", 7)
        assertEquals(QuestionSessionKind.Browse("quiz.json", targetQuestionId = 7), kind)
    }

    @Test
    fun practiceQuestionRoute_includesTargetWhenBrowse() {
        assertEquals(
            "question/encoded?targetQuestionId=3",
            BrowseSessionRoutePipeline.practiceQuestionRoute("encoded", 3),
        )
        assertEquals(
            "question/encoded",
            BrowseSessionRoutePipeline.practiceQuestionRoute("encoded", null),
        )
        assertEquals(
            "question/encoded?targetQuestionId=0",
            BrowseSessionRoutePipeline.practiceQuestionRoute("encoded", 0),
        )
    }
}
