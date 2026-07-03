package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.FullAnswerIconNavigationStrategyPipeline
import com.example.testapp.core.util.FullAnswerIconTapStrategy

/** 练习底栏箭头：原子/非原子全答单击、双击职责划分。 */
object PracticeIconUnansweredNavigationPipeline {

    fun shouldFallbackToUnansweredSource(
        navResult: UnansweredNavResult,
        strategy: FullAnswerIconTapStrategy,
        forward: Boolean
    ): Boolean {
        val atBoundary = when (forward) {
            true -> navResult == UnansweredNavResult.AtLastUnanswered
            false -> navResult == UnansweredNavResult.AtFirstUnanswered
        }
        return FullAnswerIconNavigationStrategyPipeline.shouldFallbackToCrossSourceOnSingleTap(
            strategy = strategy,
            atBoundary = atBoundary
        )
    }
}
