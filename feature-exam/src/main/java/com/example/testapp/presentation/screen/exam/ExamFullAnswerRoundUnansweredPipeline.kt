package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 全答模式：当前轮次题池内未作答 / 可导航槽位 */
object ExamFullAnswerRoundUnansweredPipeline {

    fun isSlotUnanswered(questionWithState: QuestionWithState): Boolean =
        !questionWithState.showResult

    fun unansweredIndicesInPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int
    ): List<Int> = ExamFullAnswerRoundPoolPipeline.indicesInRoundPool(questions, currentIndex)
        .filter { index -> isSlotUnanswered(questionsWithState[index]) }

    fun hasUnansweredInPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int
    ): Boolean = unansweredIndicesInPool(questions, questionsWithState, currentIndex).isNotEmpty()

    fun allSlotsAnsweredInPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int
    ): Boolean = !hasUnansweredInPool(questions, questionsWithState, currentIndex)
}
