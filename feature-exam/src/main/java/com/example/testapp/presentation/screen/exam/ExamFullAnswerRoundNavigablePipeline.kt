package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 全答模式：轮次题池内可单击导航的目标（未作答 + 须全对时的答错重开） */
object ExamFullAnswerRoundNavigablePipeline {

    fun isNavigableSlot(
        questionWithState: QuestionWithState,
        fullAnswerRequireCorrect: Boolean
    ): Boolean {
        if (!questionWithState.showResult) return true
        return fullAnswerRequireCorrect && questionWithState.isCorrect != true
    }

    fun navigableIndicesInPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): List<Int> = ExamFullAnswerRoundPoolPipeline.indicesInRoundPool(questions, currentIndex)
        .filter { index ->
            isNavigableSlot(questionsWithState[index], fullAnswerRequireCorrect)
        }

    fun hasNavigableInPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = navigableIndicesInPool(
        questions,
        questionsWithState,
        currentIndex,
        fullAnswerRequireCorrect
    ).isNotEmpty()
}
