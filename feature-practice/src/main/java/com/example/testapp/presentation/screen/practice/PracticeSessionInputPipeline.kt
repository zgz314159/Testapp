package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState

/** 练习会话内是否已有作答输入（含未批改）。 */
object PracticeSessionInputPipeline {

    fun hasAnyInput(questionsWithState: List<QuestionWithState>): Boolean =
        questionsWithState.any { PracticeFullAnswerRoundSlotPendingPipeline.hasInputContent(it) }

    fun inputCount(questionsWithState: List<QuestionWithState>): Int =
        questionsWithState.count { PracticeFullAnswerRoundSlotPendingPipeline.hasInputContent(it) }
}
