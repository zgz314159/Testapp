package com.example.testapp.presentation.screen.practice

import kotlin.random.Random

/** 全答底栏箭头：在题池索引内解析下一/上一目标（顺序环绕或随机）。 */
object PracticeFullAnswerIconNavTargetPipeline {

    fun resolveNext(
        currentIndex: Int,
        pool: List<Int>,
        randomOrder: Boolean,
        random: Random = Random.Default
    ): Int? {
        val sorted = pool.sorted()
        if (sorted.isEmpty()) return null
        if (randomOrder) {
            val others = sorted.filter { it != currentIndex }
            return others.randomOrNull(random) ?: sorted.firstOrNull()
        }
        return sorted.filter { it > currentIndex }.minOrNull() ?: sorted.firstOrNull()
    }

    fun resolvePrev(
        currentIndex: Int,
        pool: List<Int>,
        randomOrder: Boolean,
        random: Random = Random.Default
    ): Int? {
        val sorted = pool.sorted()
        if (sorted.isEmpty()) return null
        if (randomOrder) {
            val others = sorted.filter { it != currentIndex }
            return others.randomOrNull(random) ?: sorted.lastOrNull()
        }
        return sorted.filter { it < currentIndex }.maxOrNull() ?: sorted.lastOrNull()
    }

    fun hasNext(currentIndex: Int, pool: List<Int>, randomOrder: Boolean): Boolean =
        resolveNext(currentIndex, pool, randomOrder)?.let { it != currentIndex } == true

    fun hasPrev(currentIndex: Int, pool: List<Int>, randomOrder: Boolean): Boolean =
        resolvePrev(currentIndex, pool, randomOrder)?.let { it != currentIndex } == true
}
