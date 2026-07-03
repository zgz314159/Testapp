package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 当前词条是否尚未对任意轮次输入过内容。 */
object PracticeFullAnswerSourceTouchPipeline {

    fun hasAnyInputInSource(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int
    ): Boolean = PracticeFullAnswerSourcePendingPipeline.indicesInSource(questions, currentIndex)
        .any { index ->
            PracticeFullAnswerRoundSlotPendingPipeline.hasInputContent(questionsWithState[index])
        }

    fun isSourceCompletelyUntouched(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int
    ): Boolean = !hasAnyInputInSource(questions, questionsWithState, currentIndex)
}
