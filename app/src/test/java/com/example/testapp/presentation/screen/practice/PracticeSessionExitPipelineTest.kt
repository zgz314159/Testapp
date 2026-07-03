package com.example.testapp.presentation.screen.practice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSessionExitPipelineTest {

    @Test
    fun resolve_reviewMode_returnsReviewBack() {
        assertEquals(
            PracticeSessionExitPipeline.Action.ReviewBack,
            PracticeSessionExitPipeline.resolve(
                isReviewMode = true,
                answeredThisSession = true,
                hasSessionInput = true,
                sessionAnsweredCount = 5,
                totalCount = 10,
                sessionScore = 3,
                realUnanswered = 2
            )
        )
    }

    @Test
    fun resolve_noInput_returnsExitWithoutAnswer() {
        assertEquals(
            PracticeSessionExitPipeline.Action.ExitWithoutAnswer,
            PracticeSessionExitPipeline.resolve(
                isReviewMode = false,
                answeredThisSession = false,
                hasSessionInput = false,
                sessionAnsweredCount = 0,
                totalCount = 10,
                sessionScore = 0,
                realUnanswered = 10
            )
        )
    }

    @Test
    fun resolve_allAnswered_returnsFinishDirect() {
        val action = PracticeSessionExitPipeline.resolve(
            isReviewMode = false,
            answeredThisSession = true,
            hasSessionInput = true,
            sessionAnsweredCount = 10,
            totalCount = 10,
            sessionScore = 8,
            realUnanswered = 2
        )
        assertTrue(action is PracticeSessionExitPipeline.Action.FinishDirect)
        action as PracticeSessionExitPipeline.Action.FinishDirect
        assertEquals(8, action.sessionScore)
        assertEquals(10, action.sessionAnsweredCount)
        assertEquals(2, action.realUnanswered)
    }

    @Test
    fun resolve_partialProgress_returnsShowSubmitDialog() {
        assertEquals(
            PracticeSessionExitPipeline.Action.ShowSubmitDialog,
            PracticeSessionExitPipeline.resolve(
                isReviewMode = false,
                answeredThisSession = false,
                hasSessionInput = true,
                sessionAnsweredCount = 3,
                totalCount = 10,
                sessionScore = 2,
                realUnanswered = 5
            )
        )
    }
}

class PracticeSessionExitConfirmPipelineTest {

    @Test
    fun buildQuizEndParams_coercesDisplayCountFromInput() {
        val params = PracticeSessionExitConfirmPipeline.buildQuizEndParams(
            graded = PracticeSessionGradeSnapshot(
                sessionCorrectCount = 2,
                sessionAnsweredCount = 1,
                answeredCount = 4
            ),
            sessionInputCount = 3,
            totalCount = 10
        )
        assertEquals(2, params.sessionScore)
        assertEquals(3, params.sessionAnsweredForDisplay)
        assertEquals(6, params.realUnanswered)
        assertTrue(params.shouldRecordHistory)
    }
}
