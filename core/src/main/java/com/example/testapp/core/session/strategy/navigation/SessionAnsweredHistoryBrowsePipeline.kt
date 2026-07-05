package com.example.testapp.core.session.strategy.navigation

/** 标准（非全答）已答历史滑动目标解析（从 NavigationHistory 收编） */
object SessionAnsweredHistoryBrowsePipeline {
    enum class BackwardStop {
        AtOldestAnswered,
        NoMoreHistory,
    }

    /** 右滑：更旧。orderedIndices 为时间倒序（最新在前）。 */
    fun resolveOlderTargetIndex(
        orderedIndices: List<Int>,
        currentIndex: Int,
        activeHistoryPosition: Int?,
    ): Int? {
        val targetPos =
            if (activeHistoryPosition != null) {
                activeHistoryPosition + 1
            } else {
                when (val pos = orderedIndices.indexOf(currentIndex)) {
                    -1 -> 0
                    else -> if (pos < orderedIndices.lastIndex) pos + 1 else null
                }
            }
        return targetPos?.let { orderedIndices.getOrNull(it) }
    }

    /** 左滑：更新。null 表示应恢复现场或已到最新。 */
    fun resolveNewerTargetIndex(
        orderedIndices: List<Int>,
        currentIndex: Int,
        activeHistoryPosition: Int?,
    ): Int? =
        if (activeHistoryPosition != null) {
            if (activeHistoryPosition > 0) orderedIndices[activeHistoryPosition - 1] else null
        } else {
            val pos = orderedIndices.indexOf(currentIndex)
            if (pos > 0) orderedIndices[pos - 1] else null
        }

    fun resolveBackwardStopWhenNoTarget(
        orderedIndices: List<Int>,
        currentIndex: Int,
        inActiveHistoryMode: Boolean,
    ): BackwardStop {
        val globalPos = orderedIndices.indexOf(currentIndex)
        val atGlobalOldest = globalPos >= 0 && globalPos == orderedIndices.lastIndex
        return if (atGlobalOldest || inActiveHistoryMode) {
            BackwardStop.AtOldestAnswered
        } else {
            BackwardStop.NoMoreHistory
        }
    }

    fun historyPositionForTarget(
        orderedIndices: List<Int>,
        targetIndex: Int,
    ): Int = orderedIndices.indexOf(targetIndex).coerceAtLeast(0)
}
