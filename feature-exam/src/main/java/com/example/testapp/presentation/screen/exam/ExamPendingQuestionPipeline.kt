package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.QuestionWithState

/** 考试未作答判定 — 无状态管道 */
object ExamPendingQuestionPipeline {

    fun isPending(selectedOptions: List<Int>): Boolean = selectedOptions.isEmpty()

    fun isQuestionPending(questionWithState: QuestionWithState): Boolean =
        isPending(questionWithState.selectedOptions)

    fun hasPending(questionsWithState: List<QuestionWithState>): Boolean =
        questionsWithState.any(::isQuestionPending)

    fun hasOtherPending(
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int
    ): Boolean = questionsWithState.indices.any { index ->
        index != currentIndex && isQuestionPending(questionsWithState[index])
    }
}
