package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/**
 * 全答模式底栏**单击**：在当前词条轮次池内跳转（含已答轮次）；词条跳转由
 * [PracticeFullAnswerNavigation] 双击或题池全部答完后全局未答导航处理。
 */
object PracticeFullAnswerIconNavigation {

    fun sourceIndices(questions: List<Question>, currentIndex: Int): List<Int> {
        val current = questions.getOrNull(currentIndex) ?: return emptyList()
        val sourceId = extractSourceQuestionId(current.id)
        return questions.indices.filter { extractSourceQuestionId(questions[it].id) == sourceId }
    }

    fun resolveNextInSourcePool(
        currentIndex: Int,
        questions: List<Question>,
        sourceComplete: Boolean
    ): Int? {
        val pool = sourceIndices(questions, currentIndex).sorted()
        if (pool.isEmpty()) return null
        if (pool.size == 1) return if (!sourceComplete) pool.first() else null
        return pool.filter { it > currentIndex }.minOrNull()
            ?: if (!sourceComplete) pool.first() else null
    }

    fun resolvePrevInSourcePool(
        currentIndex: Int,
        questions: List<Question>,
        sourceComplete: Boolean
    ): Int? {
        val pool = sourceIndices(questions, currentIndex).sorted()
        if (pool.isEmpty()) return null
        if (pool.size == 1) return if (!sourceComplete) pool.first() else null
        return pool.filter { it < currentIndex }.maxOrNull()
            ?: if (!sourceComplete) pool.last() else null
    }

    fun hasNextInSourcePool(
        currentIndex: Int,
        questions: List<Question>,
        sourceComplete: Boolean
    ): Boolean {
        val pool = sourceIndices(questions, currentIndex)
        if (pool.size == 1) return !sourceComplete
        return resolveNextInSourcePool(currentIndex, questions, sourceComplete) != null
    }

    fun hasPrevInSourcePool(
        currentIndex: Int,
        questions: List<Question>,
        sourceComplete: Boolean
    ): Boolean {
        val pool = sourceIndices(questions, currentIndex)
        if (pool.size == 1) return !sourceComplete
        return resolvePrevInSourcePool(currentIndex, questions, sourceComplete) != null
    }
}
