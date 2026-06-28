package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 全答模式：当前轮次题池是否已全部作答（showResult） */
object ExamFullAnswerRoundCompletePipeline {

    fun isComplete(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerModeActive: Boolean
    ): Boolean {
        if (!fullAnswerModeActive) return true
        return ExamFullAnswerRoundUnansweredPipeline.allSlotsAnsweredInPool(
            questions,
            questionsWithState,
            currentIndex
        )
    }
}
