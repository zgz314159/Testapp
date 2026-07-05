package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/**
 * 当前轮次池已全部有输入，但同源仍有其他轮次 pending 时，
 * 在同词条各轮间前进（非跨词条）。
 */
object PracticeFullAnswerSameSourceRoundAdvancePipeline {

    fun resolvePendingInSameSourceOtherRound(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean,
        forward: Boolean,
        randomOrder: Boolean
    ): Int? {
        val current = questions.getOrNull(currentIndex) ?: return null
        val sourceId = extractSourceQuestionId(current.id)
        val currentRound = PracticeFullAnswerRoundPoolPipeline.roundOf(current.id)
        val sameSourceIndices = questions.indices.filter { index ->
            extractSourceQuestionId(questions[index].id) == sourceId
        }
        val pendingOtherRounds = sameSourceIndices.filter { index ->
            PracticeFullAnswerRoundPoolPipeline.roundOf(questions[index].id) != currentRound &&
                PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                    questionsWithState[index],
                    fullAnswerRequireCorrect
                )
        }
        if (pendingOtherRounds.isEmpty()) return null
        return if (forward) {
            PracticeFullAnswerIconNavTargetPipeline.resolveNext(currentIndex, pendingOtherRounds, randomOrder)
        } else {
            PracticeFullAnswerIconNavTargetPipeline.resolvePrev(currentIndex, pendingOtherRounds, randomOrder)
        }?.takeIf { it != currentIndex }
    }

    fun hasPendingInSameSourceOtherRound(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean
    ): Boolean = resolvePendingInSameSourceOtherRound(
        questions,
        questionsWithState,
        currentIndex,
        fullAnswerRequireCorrect,
        forward = true,
        randomOrder = false
    ) != null || resolvePendingInSameSourceOtherRound(
        questions,
        questionsWithState,
        currentIndex,
        fullAnswerRequireCorrect,
        forward = false,
        randomOrder = false
    ) != null
}
