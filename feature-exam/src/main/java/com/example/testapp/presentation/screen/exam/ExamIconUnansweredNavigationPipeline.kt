package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.FullAnswerIconNavigationStrategyPipeline
import com.example.testapp.core.util.FullAnswerIconTapStrategy

/** 考试底栏箭头：原子/非原子全答单击、双击职责划分。 */
object ExamIconUnansweredNavigationPipeline {

    fun shouldFallbackToAdjacentSource(
        navigated: Boolean,
        strategy: FullAnswerIconTapStrategy
    ): Boolean = FullAnswerIconNavigationStrategyPipeline.shouldFallbackToCrossSourceOnSingleTap(
        strategy = strategy,
        atBoundary = !navigated
    )
}
