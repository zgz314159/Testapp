package com.example.testapp.uicommon.design

import kotlin.math.abs

enum class QuestionSessionHistorySwipeDirection {
    Older,
    Newer
}

object QuestionSessionHistorySwipePipeline {
    const val MIN_HORIZONTAL_PX = 100f
    private const val HORIZONTAL_DOMINANCE_RATIO = 1.5f

    fun resolve(
        horizontalDrag: Float,
        verticalDrag: Float
    ): QuestionSessionHistorySwipeDirection? {
        val absH = abs(horizontalDrag)
        val absV = abs(verticalDrag)
        if (absH < MIN_HORIZONTAL_PX) return null
        if (absH <= absV * HORIZONTAL_DOMINANCE_RATIO) return null
        return when {
            horizontalDrag > 0f -> QuestionSessionHistorySwipeDirection.Older
            horizontalDrag < 0f -> QuestionSessionHistorySwipeDirection.Newer
            else -> null
        }
    }
}
