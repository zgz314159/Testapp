package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question

/**
 * 全答模式底栏箭头：非原子用词条池；原子用**当前轮次**池（见 [PracticeFullAnswerRoundPoolPipeline]）。
 */
object PracticeFullAnswerIconNavigation {

    fun roundPoolIndices(questions: List<Question>, currentIndex: Int): List<Int> =
        PracticeFullAnswerRoundPoolPipeline.indicesInRoundPool(questions, currentIndex)

    fun resolveNextInRoundPool(
        currentIndex: Int,
        navigableInPool: List<Int>,
        randomOrder: Boolean = false
    ): Int? = PracticeFullAnswerIconNavTargetPipeline.resolveNext(currentIndex, navigableInPool, randomOrder)

    fun resolvePrevInRoundPool(
        currentIndex: Int,
        navigableInPool: List<Int>,
        randomOrder: Boolean = false
    ): Int? = PracticeFullAnswerIconNavTargetPipeline.resolvePrev(currentIndex, navigableInPool, randomOrder)

    fun hasNextInRoundPool(
        currentIndex: Int,
        navigableInPool: List<Int>,
        randomOrder: Boolean = false
    ): Boolean = PracticeFullAnswerIconNavTargetPipeline.hasNext(currentIndex, navigableInPool, randomOrder)

    fun hasPrevInRoundPool(
        currentIndex: Int,
        navigableInPool: List<Int>,
        randomOrder: Boolean = false
    ): Boolean = PracticeFullAnswerIconNavTargetPipeline.hasPrev(currentIndex, navigableInPool, randomOrder)

    fun sourceIndices(questions: List<Question>, currentIndex: Int): List<Int> {
        val current = questions.getOrNull(currentIndex) ?: return emptyList()
        val sourceId = extractSourceQuestionId(current.id)
        return questions.indices.filter { extractSourceQuestionId(questions[it].id) == sourceId }
    }

    fun resolveNextInSourcePool(
        currentIndex: Int,
        questions: List<Question>,
        sourceComplete: Boolean,
        randomOrder: Boolean = false
    ): Int? {
        val pool = sourceIndices(questions, currentIndex)
        if (pool.isEmpty()) return null
        if (sourceComplete) return null
        return PracticeFullAnswerIconNavTargetPipeline.resolveNext(currentIndex, pool, randomOrder)
    }

    fun resolvePrevInSourcePool(
        currentIndex: Int,
        questions: List<Question>,
        sourceComplete: Boolean,
        randomOrder: Boolean = false
    ): Int? {
        val pool = sourceIndices(questions, currentIndex)
        if (pool.isEmpty()) return null
        if (sourceComplete) return null
        return PracticeFullAnswerIconNavTargetPipeline.resolvePrev(currentIndex, pool, randomOrder)
    }

    fun hasNextInSourcePool(
        currentIndex: Int,
        questions: List<Question>,
        sourceComplete: Boolean,
        randomOrder: Boolean = false
    ): Boolean {
        if (sourceComplete) return false
        val pool = sourceIndices(questions, currentIndex)
        return PracticeFullAnswerIconNavTargetPipeline.hasNext(currentIndex, pool, randomOrder)
    }

    fun hasPrevInSourcePool(
        currentIndex: Int,
        questions: List<Question>,
        sourceComplete: Boolean,
        randomOrder: Boolean = false
    ): Boolean {
        if (sourceComplete) return false
        val pool = sourceIndices(questions, currentIndex)
        return PracticeFullAnswerIconNavTargetPipeline.hasPrev(currentIndex, pool, randomOrder)
    }
}
