package com.example.testapp.presentation.session.exam

import com.example.testapp.presentation.screen.exam.ExamReviewSwipeOutcome

sealed class ExamCommandOutcome {
    data class ReviewHistoryOlder(val result: ExamReviewSwipeOutcome) : ExamCommandOutcome()

    data class ReviewHistoryNewer(val result: ExamReviewSwipeOutcome) : ExamCommandOutcome()
}
