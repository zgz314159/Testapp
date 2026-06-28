package com.example.testapp.domain.review

import com.example.testapp.domain.model.QuestionWithState

/** 复盘右滑：仅已答、按作答时间倒序；与答题卡/箭头用的全量 displayOrder 分离。 */
object ReviewAnsweredSwipePipeline {

    fun buildOrder(items: List<QuestionWithState>): List<Int> =
        AnsweredBrowseOrder.buildAnsweredIndicesByTimeDesc(items)

    fun resolveOlderIndex(orderedIndices: List<Int>, currentIndex: Int): Int? {
        if (orderedIndices.isEmpty()) return null
        val pos = orderedIndices.indexOf(currentIndex)
        return when {
            pos == -1 -> orderedIndices.first()
            pos < orderedIndices.lastIndex -> orderedIndices[pos + 1]
            else -> null
        }
    }

    fun resolveNewerIndex(orderedIndices: List<Int>, currentIndex: Int): Int? {
        if (orderedIndices.isEmpty()) return null
        val pos = orderedIndices.indexOf(currentIndex)
        return when {
            pos <= 0 -> null
            else -> orderedIndices[pos - 1]
        }
    }

    fun isAtOldest(orderedIndices: List<Int>, currentIndex: Int): Boolean {
        val pos = orderedIndices.indexOf(currentIndex)
        return pos >= 0 && pos == orderedIndices.lastIndex
    }

    fun isAtLatest(orderedIndices: List<Int>, currentIndex: Int): Boolean {
        val pos = orderedIndices.indexOf(currentIndex)
        return pos <= 0
    }
}
