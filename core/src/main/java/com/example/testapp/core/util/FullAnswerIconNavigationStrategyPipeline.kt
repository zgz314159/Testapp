package com.example.testapp.core.util

import com.example.testapp.domain.model.Question

/** @deprecated 使用 [FullAnswerMultiRoundSessionPipeline] */
object AtomicFullAnswerSessionPipeline {

    fun isAtomicRoundPoolSession(questions: List<Question>): Boolean =
        FullAnswerMultiRoundSessionPipeline.isMultiRoundSession(questions)
}

enum class FullAnswerIconTapStrategy {
    /** 多轮全答：单击轮次池 → 轮次完成后跨词条 */
    MULTI_ROUND_POOL_FIRST,

    /** 单轮/非多轮全答或普通练习：单击全局未作答题 */
    GLOBAL_UNANSWERED_FIRST
}

object FullAnswerIconNavigationStrategyPipeline {

    fun resolve(
        fullAnswerModeActive: Boolean,
        multiRoundSession: Boolean
    ): FullAnswerIconTapStrategy = when {
        fullAnswerModeActive && multiRoundSession ->
            FullAnswerIconTapStrategy.MULTI_ROUND_POOL_FIRST
        else -> FullAnswerIconTapStrategy.GLOBAL_UNANSWERED_FIRST
    }

    fun singleTapUsesRoundPool(strategy: FullAnswerIconTapStrategy): Boolean =
        strategy == FullAnswerIconTapStrategy.MULTI_ROUND_POOL_FIRST

    fun doubleTapUsesCrossSource(strategy: FullAnswerIconTapStrategy): Boolean =
        strategy == FullAnswerIconTapStrategy.MULTI_ROUND_POOL_FIRST

    fun shouldFallbackToCrossSourceOnSingleTap(
        strategy: FullAnswerIconTapStrategy,
        atBoundary: Boolean
    ): Boolean = strategy == FullAnswerIconTapStrategy.GLOBAL_UNANSWERED_FIRST && atBoundary
}
