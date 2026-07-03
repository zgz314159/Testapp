package com.example.testapp.presentation.screen.exam

import org.junit.Assert.assertEquals
import org.junit.Test

class ExamSessionExitPipelineTest {

    @Test
    fun resolve_reviewBack() {
        assertEquals(
            ExamSessionExitPipeline.Action.ReviewBack,
            ExamSessionExitPipeline.resolve(isReviewMode = true, answeredThisSession = false, hasPendingQuestions = true)
        )
    }

    @Test
    fun resolve_exitWhenNotAnswered() {
        assertEquals(
            ExamSessionExitPipeline.Action.ExitWithoutAnswer,
            ExamSessionExitPipeline.resolve(isReviewMode = false, answeredThisSession = false, hasPendingQuestions = true)
        )
    }

    @Test
    fun resolve_submitDialogWhenPending() {
        assertEquals(
            ExamSessionExitPipeline.Action.ShowSubmitDialog,
            ExamSessionExitPipeline.resolve(isReviewMode = false, answeredThisSession = true, hasPendingQuestions = true)
        )
    }

    @Test
    fun resolve_finishWhenComplete() {
        assertEquals(
            ExamSessionExitPipeline.Action.FinishDirect,
            ExamSessionExitPipeline.resolve(isReviewMode = false, answeredThisSession = true, hasPendingQuestions = false)
        )
    }
}
