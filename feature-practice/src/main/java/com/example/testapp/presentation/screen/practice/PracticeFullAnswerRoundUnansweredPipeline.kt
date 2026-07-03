package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

object PracticeFullAnswerRoundUnansweredPipeline {

    fun hasUnansweredInPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = PracticeFullAnswerRoundPoolPipeline.indicesInRoundPool(questions, currentIndex)
        .any { index ->
            PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                questionsWithState[index],
                fullAnswerRequireCorrect
            )
        }

    fun allAnsweredInPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = !hasUnansweredInPool(
        questions,
        questionsWithState,
        currentIndex,
        fullAnswerRequireCorrect
    )
}
