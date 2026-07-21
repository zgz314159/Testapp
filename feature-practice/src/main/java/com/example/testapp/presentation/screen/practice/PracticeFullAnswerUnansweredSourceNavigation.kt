package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/**
 * 全答模式双击左/右：退出历史后跳到**仍有未作答题**的上一/下一词条（顺序或随机）。
 * 「未作答词条」= 该词条轮次池内至少有一题 pending。
 */
object PracticeFullAnswerUnansweredSourceNavigation {

    private const val TAG = "PracticeHistorySwipe"

    fun isSourceIncomplete(
        questions: List<Question>,
        sourceEntryIndex: Int,
        isPendingAtIndex: (Int) -> Boolean
    ): Boolean {
        val pool = PracticeFullAnswerIconNavigation.sourceIndices(questions, sourceEntryIndex)
        return if (pool.isEmpty()) isPendingAtIndex(sourceEntryIndex)
        else pool.any(isPendingAtIndex)
    }

    fun resolveFirstPendingInSource(
        questions: List<Question>,
        sourceEntryIndex: Int,
        isPendingAtIndex: (Int) -> Boolean
    ): Int? {
        val pool = PracticeFullAnswerIconNavigation.sourceIndices(questions, sourceEntryIndex).sorted()
        return pool.firstOrNull(isPendingAtIndex)
            ?: sourceEntryIndex.takeIf(isPendingAtIndex)
    }

    fun resolveUnansweredSourceEntryIndex(
        questions: List<Question>,
        currentIndex: Int,
        forward: Boolean,
        randomOrder: Boolean,
        isSourceIncomplete: (Int) -> Boolean,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): Int? {
        val entries = PracticeFullAnswerNavigation.listSourceEntryIndices(questions)
        if (entries.isEmpty()) return null
        val currentQ = questions.getOrNull(currentIndex) ?: return null
        val currentSourceId = extractSourceQuestionId(currentQ.id)
        val currentEntryPos = entries.indexOfFirst {
            extractSourceQuestionId(questions[it].id) == currentSourceId
        }
        val candidateEntries = if (forward) {
            if (currentEntryPos >= 0) entries.drop(currentEntryPos + 1) else entries
        } else {
            if (currentEntryPos > 0) entries.take(currentEntryPos).asReversed() else emptyList()
        }
        val incomplete = candidateEntries.filter(isSourceIncomplete)
        val picked = if (randomOrder) incomplete.randomOrNull(random) else incomplete.firstOrNull()
        return picked
    }
}
