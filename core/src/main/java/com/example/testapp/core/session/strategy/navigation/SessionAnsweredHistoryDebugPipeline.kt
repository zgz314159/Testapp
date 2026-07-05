package com.example.testapp.core.session.strategy.navigation

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/** 已答历史 debug 行格式化（从 PracticeFullAnswerHistoryNavigation 收编） */
object SessionAnsweredHistoryDebugPipeline {
    fun formatOrderedDebugLine(
        orderedIndices: List<Int>,
        questions: List<Question>,
        resolveAnswerTime: (Int) -> Long,
    ): String =
        orderedIndices.joinToString(prefix = "[", postfix = "]") { idx ->
            val q = questions.getOrNull(idx) ?: return@joinToString "?$idx"
            val src = extractSourceQuestionId(q.id)
            val round = if (q.id < 0) "r${(-q.id)}" else "base"
            "idx=$idx,src=$src,$round,t=${resolveAnswerTime(idx)}"
        }

    fun formatSwipeLogLine(
        action: String,
        result: String?,
        currentIndex: Int,
        originIndex: Int?,
        historyPosition: Int?,
        anchorPoolIndices: Set<Int>?,
        orderedDebugLine: String,
        targetIndex: Int?,
    ): String =
        "$action | result=$result | currentIdx=$currentIndex | " +
            "origin=$originIndex | pos=$historyPosition | " +
            "pool=$anchorPoolIndices | ordered=$orderedDebugLine | target=$targetIndex"
}
