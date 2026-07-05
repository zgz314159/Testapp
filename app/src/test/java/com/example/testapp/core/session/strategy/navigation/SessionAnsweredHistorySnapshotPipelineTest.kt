package com.example.testapp.core.session.strategy.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionAnsweredHistorySnapshotPipelineTest {
    @Test
    fun shouldCapture_onlyWhenShowResult() {
        assertTrue(SessionAnsweredHistorySnapshotPipeline.shouldCapture(showResult = true))
        assertFalse(SessionAnsweredHistorySnapshotPipeline.shouldCapture(showResult = false))
    }

    @Test
    fun shouldReplaceExisting_whenMissingOrNewer() {
        assertTrue(SessionAnsweredHistorySnapshotPipeline.shouldReplaceExisting(null, 100L))
        assertTrue(SessionAnsweredHistorySnapshotPipeline.shouldReplaceExisting(100L, 100L))
        assertTrue(SessionAnsweredHistorySnapshotPipeline.shouldReplaceExisting(100L, 200L))
        assertFalse(SessionAnsweredHistorySnapshotPipeline.shouldReplaceExisting(200L, 100L))
    }

    @Test
    fun shouldKeepLiveStateOnApply_whenAnsweredAndNotPreferSnapshot() {
        assertTrue(
            SessionAnsweredHistorySnapshotPipeline.shouldKeepLiveStateOnApply(
                preferSnapshot = false,
                liveShowResult = true,
                isQuestionAnswered = true,
            ),
        )
        assertFalse(
            SessionAnsweredHistorySnapshotPipeline.shouldKeepLiveStateOnApply(
                preferSnapshot = true,
                liveShowResult = true,
                isQuestionAnswered = true,
            ),
        )
    }
}
