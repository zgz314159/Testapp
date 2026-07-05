package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.review.ReviewAnsweredSwipePipeline
import com.example.testapp.domain.review.ReviewBrowseSession
import com.example.testapp.domain.review.SessionReviewPresentation

data class PracticeReviewPresentationResult(
    val questionsWithState: List<QuestionWithState>,
    val currentIndex: Int,
    val reviewBrowseSession: ReviewBrowseSession,
    val reviewAnsweredSwipeOrder: List<Int>
)

object PracticeReviewPresentationPipeline {
    fun prepare(state: PracticeSessionState): PracticeReviewPresentationResult {
        val presentation = SessionReviewPresentation.prepare(state.questionsWithState)
        val browseSession = ReviewBrowseSession(presentation.displayOrder)
        return PracticeReviewPresentationResult(
            questionsWithState = presentation.questionsWithState,
            currentIndex = browseSession.currentIndex,
            reviewBrowseSession = browseSession,
            reviewAnsweredSwipeOrder = ReviewAnsweredSwipePipeline.buildOrder(presentation.questionsWithState)
        )
    }
}
