package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 多轮全答：底栏单击仅在当前轮次池 pending 槽位间跳转；池内无 pending 才允许出池。 */
object PracticeFullAnswerRoundIconNavPipeline {

    fun pendingIndicesInRound(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): List<Int> = PracticeFullAnswerSourceRoundPoolPipeline.indicesInPool(questions, currentIndex)
        .filter { index ->
            PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                questionsWithState[index],
                fullAnswerRequireCorrect
            )
        }

    fun hasPendingInRound(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = pendingIndicesInRound(
        questions,
        questionsWithState,
        currentIndex,
        fullAnswerRequireCorrect
    ).isNotEmpty()

    fun resolveTargetIndex(
        currentIndex: Int,
        pendingInRound: List<Int>,
        forward: Boolean,
        randomOrder: Boolean
    ): Int? {
        if (pendingInRound.isEmpty()) return null
        return if (forward) {
            PracticeFullAnswerIconNavTargetPipeline.resolveNext(currentIndex, pendingInRound, randomOrder)
        } else {
            PracticeFullAnswerIconNavTargetPipeline.resolvePrev(currentIndex, pendingInRound, randomOrder)
        }
    }

    fun canMoveInRound(
        currentIndex: Int,
        pendingInRound: List<Int>,
        forward: Boolean,
        randomOrder: Boolean
    ): Boolean = resolveTargetIndex(currentIndex, pendingInRound, forward, randomOrder)
        ?.let { it != currentIndex } == true
}
