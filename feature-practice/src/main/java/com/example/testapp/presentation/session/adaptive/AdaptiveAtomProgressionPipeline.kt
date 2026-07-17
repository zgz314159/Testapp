package com.example.testapp.presentation.session.adaptive

import com.example.testapp.domain.model.AdaptiveAtomPool
import com.example.testapp.domain.model.AdaptiveAtomStage
import com.example.testapp.domain.model.AdaptiveAtomState

object AdaptiveAtomProgressionPipeline {
    private const val MINUTE_MS = 60_000L
    private const val HOUR_MS = 60 * MINUTE_MS
    private const val DAY_MS = 24 * HOUR_MS

    fun next(
        current: AdaptiveAtomState,
        correct: Boolean,
        reviewedAt: Long,
    ): AdaptiveAtomState =
        if (correct) {
            advance(current, reviewedAt)
        } else {
            regress(current, reviewedAt)
        }

    private fun advance(current: AdaptiveAtomState, reviewedAt: Long): AdaptiveAtomState {
        val streak = current.correctStreak + 1
        val (stage, nextStreak, interval) =
            when (current.stage) {
                AdaptiveAtomStage.CHOICE ->
                    if (streak >= 2) {
                        Triple(AdaptiveAtomStage.HINTED, 0, DAY_MS)
                    } else {
                        Triple(current.stage, streak, 12 * HOUR_MS)
                    }
                AdaptiveAtomStage.HINTED ->
                    if (streak >= 2) {
                        Triple(AdaptiveAtomStage.RECALL, 0, 2 * DAY_MS)
                    } else {
                        Triple(current.stage, streak, DAY_MS)
                    }
                AdaptiveAtomStage.RECALL ->
                    if (streak >= 3) {
                        Triple(AdaptiveAtomStage.MATURE, 0, 7 * DAY_MS)
                    } else {
                        Triple(current.stage, streak, 3 * DAY_MS)
                    }
                AdaptiveAtomStage.MATURE -> Triple(current.stage, streak.coerceAtMost(5), 14 * DAY_MS)
            }
        return current.copy(
            stage = stage,
            correctStreak = nextStreak,
            reviewCount = current.reviewCount + 1,
            dueAt = reviewedAt + interval,
            lastReviewedAt = reviewedAt,
        )
    }

    private fun regress(current: AdaptiveAtomState, reviewedAt: Long): AdaptiveAtomState {
        val stage =
            when (current.stage) {
                AdaptiveAtomStage.MATURE -> AdaptiveAtomStage.RECALL
                AdaptiveAtomStage.RECALL -> AdaptiveAtomStage.HINTED
                AdaptiveAtomStage.HINTED,
                AdaptiveAtomStage.CHOICE,
                -> AdaptiveAtomStage.CHOICE
            }
        return current.copy(
            pool = AdaptiveAtomPool.CORE,
            stage = stage,
            correctStreak = 0,
            lapseCount = current.lapseCount + 1,
            reviewCount = current.reviewCount + 1,
            dueAt = reviewedAt + 10 * MINUTE_MS,
            lastReviewedAt = reviewedAt,
        )
    }
}
