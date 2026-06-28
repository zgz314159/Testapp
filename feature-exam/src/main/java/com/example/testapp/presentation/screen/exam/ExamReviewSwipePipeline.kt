package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.review.ReviewAnsweredSwipePipeline

enum class ExamReviewSwipeOutcome {
    Navigated,
    AtOldest,
    AtLatest,
    NoHistory
}

/** 考试答题详情：右滑更旧 / 左滑更新（与练习复盘一致） */
object ExamReviewSwipePipeline {

    fun browseOlder(
        orderedIndices: List<Int>,
        currentIndex: Int
    ): Pair<ExamReviewSwipeOutcome, Int?> {
        if (orderedIndices.isEmpty()) return ExamReviewSwipeOutcome.NoHistory to null
        val target = ReviewAnsweredSwipePipeline.resolveOlderIndex(orderedIndices, currentIndex)
        if (target != null) return ExamReviewSwipeOutcome.Navigated to target
        val outcome = if (ReviewAnsweredSwipePipeline.isAtOldest(orderedIndices, currentIndex)) {
            ExamReviewSwipeOutcome.AtOldest
        } else {
            ExamReviewSwipeOutcome.NoHistory
        }
        return outcome to null
    }

    fun browseNewer(
        orderedIndices: List<Int>,
        currentIndex: Int
    ): Pair<ExamReviewSwipeOutcome, Int?> {
        if (orderedIndices.isEmpty()) return ExamReviewSwipeOutcome.AtLatest to null
        val target = ReviewAnsweredSwipePipeline.resolveNewerIndex(orderedIndices, currentIndex)
        if (target != null) return ExamReviewSwipeOutcome.Navigated to target
        return ExamReviewSwipeOutcome.AtLatest to null
    }
}
