package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.Question

/**
 * 全答模式底栏**单击**：在当前词条**当前轮次**题池内跳转（未作答 / 须全对答错）；
 * 轮次池全部作答完后才允许跨池；双击跳词条。
 */
object ExamFullAnswerIconNavigation {

    fun roundPoolIndices(questions: List<Question>, currentIndex: Int): List<Int> =
        ExamFullAnswerRoundPoolPipeline.indicesInRoundPool(questions, currentIndex)

    fun resolveNextInRoundPool(
        currentIndex: Int,
        navigableInPool: List<Int>
    ): Int? {
        val pool = navigableInPool.sorted()
        if (pool.isEmpty()) return null
        return pool.filter { it > currentIndex }.minOrNull() ?: pool.firstOrNull()
    }

    fun resolvePrevInRoundPool(
        currentIndex: Int,
        navigableInPool: List<Int>
    ): Int? {
        val pool = navigableInPool.sorted()
        if (pool.isEmpty()) return null
        return pool.filter { it < currentIndex }.maxOrNull() ?: pool.lastOrNull()
    }

    fun hasNextInRoundPool(
        currentIndex: Int,
        navigableInPool: List<Int>
    ): Boolean = resolveNextInRoundPool(currentIndex, navigableInPool)?.let { it != currentIndex } == true

    fun hasPrevInRoundPool(
        currentIndex: Int,
        navigableInPool: List<Int>
    ): Boolean = resolvePrevInRoundPool(currentIndex, navigableInPool)?.let { it != currentIndex } == true
}
