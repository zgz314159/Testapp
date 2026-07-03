package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState

/**
 * 全答轮次池内单题 pending / 完成判定。
 * - 全答模式：无输入 → pending
 * - 须全对模式：无输入 → pending；有输入未批改 → 已作答；批改后答错 → pending
 */
object PracticeFullAnswerRoundSlotPendingPipeline {

    fun hasInputContent(questionWithState: QuestionWithState): Boolean =
        questionWithState.textAnswer.isNotBlank() || questionWithState.selectedOptions.isNotEmpty()

    fun isPendingInRoundSlot(
        questionWithState: QuestionWithState,
        fullAnswerRequireCorrect: Boolean
    ): Boolean {
        if (!hasInputContent(questionWithState)) return true
        if (!fullAnswerRequireCorrect) return false
        if (!questionWithState.showResult) return false
        return questionWithState.isCorrect != true
    }

    fun isCompleteInRoundSlot(
        questionWithState: QuestionWithState,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = !isPendingInRoundSlot(questionWithState, fullAnswerRequireCorrect)
}
