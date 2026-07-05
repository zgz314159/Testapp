package com.example.testapp.core.session.strategy.navigation

/** 已答历史滑动目标统一路由（标准 / 全答） */
object SessionAnsweredHistoryTargetPipeline {
    fun resolveOlderTargetIndex(
        fullAnswerModeActive: Boolean,
        orderedIndices: List<Int>,
        anchorPoolIndices: Set<Int>,
        currentIndex: Int,
        activeHistoryPosition: Int?,
    ): Int? =
        if (fullAnswerModeActive) {
            SessionFullAnswerHistoryBrowsePipeline.resolveOlderTargetIndex(
                orderedIndices,
                anchorPoolIndices,
                currentIndex,
            )
        } else {
            SessionAnsweredHistoryBrowsePipeline.resolveOlderTargetIndex(
                orderedIndices,
                currentIndex,
                activeHistoryPosition,
            )
        }

    fun resolveNewerTargetIndex(
        fullAnswerModeActive: Boolean,
        orderedIndices: List<Int>,
        anchorPoolIndices: Set<Int>,
        currentIndex: Int,
        activeHistoryPosition: Int?,
    ): Int? =
        if (fullAnswerModeActive) {
            SessionFullAnswerHistoryBrowsePipeline.resolveNewerTargetIndex(
                orderedIndices,
                anchorPoolIndices,
                currentIndex,
            )
        } else {
            SessionAnsweredHistoryBrowsePipeline.resolveNewerTargetIndex(
                orderedIndices,
                currentIndex,
                activeHistoryPosition,
            )
        }
}
