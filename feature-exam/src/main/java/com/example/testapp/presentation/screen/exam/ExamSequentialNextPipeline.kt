package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.QuestionWithState

/** 顺序考试下一题目标：后 pending → 前 pending → 顺序下一格 */
object ExamSequentialNextPipeline {

    fun resolveNextIndex(
        currentIndex: Int,
        questionsWithState: List<QuestionWithState>,
        isPending: (QuestionWithState) -> Boolean
    ): Int? {
        val indices = questionsWithState.indices
        val unansweredAfter = indices.firstOrNull { index ->
            index > currentIndex && isPending(questionsWithState[index])
        }
        val unansweredBefore = indices.firstOrNull { index ->
            index < currentIndex && isPending(questionsWithState[index])
        }
        val nextSequential = indices.firstOrNull { it > currentIndex }
        return unansweredAfter ?: unansweredBefore ?: nextSequential
    }
}
