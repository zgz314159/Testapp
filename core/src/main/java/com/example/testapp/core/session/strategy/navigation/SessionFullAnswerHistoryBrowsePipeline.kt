package com.example.testapp.core.session.strategy.navigation

/**
 * 全答模式已答历史滑动：全局 orderedIndices 按作答时间倒序；
 * 右滑=更旧、左滑=更新；同词条轮次池内优先，池边界再沿全局时间跨词条。
 */
object SessionFullAnswerHistoryBrowsePipeline {
    fun resolveOlderTargetIndex(
        orderedIndices: List<Int>,
        poolIndices: Set<Int>,
        currentIndex: Int,
    ): Int? {
        if (orderedIndices.isEmpty()) return null
        val globalPos = orderedIndices.indexOf(currentIndex)
        val poolOrdered = orderedIndices.filter { it in poolIndices }
        val poolPos = poolOrdered.indexOf(currentIndex)

        return when {
            globalPos < 0 ->
                poolOrdered.firstOrNull { it != currentIndex } ?: orderedIndices.firstOrNull()
            poolPos >= 0 && poolPos < poolOrdered.lastIndex ->
                poolOrdered[poolPos + 1]
            globalPos < orderedIndices.lastIndex ->
                orderedIndices[globalPos + 1]
            else -> null
        }
    }

    fun resolveNewerTargetIndex(
        orderedIndices: List<Int>,
        poolIndices: Set<Int>,
        currentIndex: Int,
    ): Int? {
        if (orderedIndices.isEmpty()) return null
        val globalPos = orderedIndices.indexOf(currentIndex)
        val poolOrdered = orderedIndices.filter { it in poolIndices }
        val poolPos = poolOrdered.indexOf(currentIndex)

        return when {
            poolPos > 0 -> poolOrdered[poolPos - 1]
            globalPos > 0 -> orderedIndices[globalPos - 1]
            else -> null
        }
    }
}
