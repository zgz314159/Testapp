package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.UnifiedQuestionState

/** 练习新轮次出题顺序 */
object PracticeQuestionOrderPipeline {

    fun answeredQuestionIds(questionStateMap: Map<Int, UnifiedQuestionState>): Set<Int> =
        PracticeSourceQuestionPipeline.answeredSourceIds(questionStateMap)

    fun orderForNewRound(
        originalQuestions: List<Question>,
        questionCount: Int,
        random: Boolean,
        answeredSourceIds: Set<Int>,
        seed: Long,
        lastRoundSourceIds: Set<Int> = emptySet()
    ): List<Question> {
        val unanswered = originalQuestions.filter { it.id !in answeredSourceIds }
        val answered = originalQuestions.filter { it.id in answeredSourceIds }
        val ordered = when {
            unanswered.isNotEmpty() -> {
                if (random) {
                    unanswered.shuffled(java.util.Random(seed)) +
                        answered.shuffled(java.util.Random(seed + 1000))
                } else {
                    unanswered + answered
                }
            }
            else -> recyclePool(originalQuestions, lastRoundSourceIds, random, seed)
        }
        return PracticeQuestionCountPolicy.limitQuestions(ordered, questionCount)
    }

    private fun recyclePool(
        originalQuestions: List<Question>,
        lastRoundSourceIds: Set<Int>,
        random: Boolean,
        seed: Long
    ): List<Question> {
        val preferred = originalQuestions.filter { it.id !in lastRoundSourceIds }
        val pool = preferred.ifEmpty { originalQuestions }
        return if (random) pool.shuffled(java.util.Random(seed)) else pool
    }
}
