package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 轮次池全部完成时，允许单击箭头跨词条。 */
object PracticeFullAnswerRoundCrossSourcePipeline {

    fun maySingleTapExitRoundPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = !PracticeFullAnswerSourcePendingPipeline.hasPendingInSource(
        questions,
        questionsWithState,
        currentIndex,
        fullAnswerRequireCorrect
    )
}
