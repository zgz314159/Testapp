package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/**
 * 全答模式已答历史滑动：全局 [orderedIndices] 严格按作答时间倒序；
 * 右滑=更旧、左滑=更新；同词条轮次池内优先，池边界再沿全局时间跨词条。
 */
object PracticeFullAnswerHistoryNavigation {

    private const val TAG = "PracticeHistorySwipe"

    fun sourcePoolIndices(questions: List<Question>, anchorIndex: Int): Set<Int> =
        PracticeFullAnswerIconNavigation.sourceIndices(questions, anchorIndex).toSet()

    /** 右滑：更旧。先池内更旧，池末再全局更旧（跨词条）。 */
    fun resolveOlderTargetIndex(
        orderedIndices: List<Int>,
        poolIndices: Set<Int>,
        currentIndex: Int
    ): Int? {
        if (orderedIndices.isEmpty()) return null
        val globalPos = orderedIndices.indexOf(currentIndex)
        val poolOrdered = orderedIndices.filter { it in poolIndices }
        val poolPos = poolOrdered.indexOf(currentIndex)

        val target = when {
            globalPos < 0 -> {
                // 现场题不在已答列表：进入历史，优先池内第一项，否则全局最新已答
                poolOrdered.firstOrNull { it != currentIndex } ?: orderedIndices.firstOrNull()
            }
            poolPos >= 0 && poolPos < poolOrdered.lastIndex ->
                poolOrdered[poolPos + 1]
            globalPos < orderedIndices.lastIndex ->
                orderedIndices[globalPos + 1]
            else -> null
        }
        logStep(
            "resolveOlder",
            mapOf(
                "currentIndex" to currentIndex,
                "globalPos" to globalPos,
                "poolOrdered" to poolOrdered,
                "poolPos" to poolPos,
                "target" to target
            )
        )
        return target
    }

    /** 左滑：更新。先池内更新，池首再全局更新（跨词条）；null=应恢复现场。 */
    fun resolveNewerTargetIndex(
        orderedIndices: List<Int>,
        poolIndices: Set<Int>,
        currentIndex: Int
    ): Int? {
        if (orderedIndices.isEmpty()) return null
        val globalPos = orderedIndices.indexOf(currentIndex)
        val poolOrdered = orderedIndices.filter { it in poolIndices }
        val poolPos = poolOrdered.indexOf(currentIndex)

        val target = when {
            poolPos > 0 ->
                poolOrdered[poolPos - 1]
            globalPos > 0 ->
                orderedIndices[globalPos - 1]
            else -> null
        }
        logStep(
            "resolveNewer",
            mapOf(
                "currentIndex" to currentIndex,
                "globalPos" to globalPos,
                "poolOrdered" to poolOrdered,
                "poolPos" to poolPos,
                "target" to target
            )
        )
        return target
    }

    fun formatOrderedDebugLine(
        orderedIndices: List<Int>,
        questions: List<Question>,
        resolveAnswerTime: (Int) -> Long
    ): String = orderedIndices.joinToString(prefix = "[", postfix = "]") { idx ->
        val q = questions.getOrNull(idx) ?: return@joinToString "?$idx"
        val src = extractSourceQuestionId(q.id)
        val round = if (q.id < 0) "r${(-q.id)}" else "base"
        "idx=$idx,src=$src,$round,t=${resolveAnswerTime(idx)}"
    }

    fun logStep(step: String, data: Map<String, Any?>) {
    }
}
