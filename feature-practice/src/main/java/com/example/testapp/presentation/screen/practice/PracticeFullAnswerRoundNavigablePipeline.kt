package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 全答轮次池内可单击跳转的目标（未输入 / 须全对未答对）。 */
object PracticeFullAnswerRoundNavigablePipeline {

    fun isNavigableSlot(
        questionWithState: QuestionWithState,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
        questionWithState,
        fullAnswerRequireCorrect
    )

    fun navigableIndicesInPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): List<Int> = PracticeFullAnswerRoundPoolPipeline.indicesInRoundPool(questions, currentIndex)
        .filter { index ->
            isNavigableSlot(questionsWithState[index], fullAnswerRequireCorrect)
        }
}
