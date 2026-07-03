package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.extractDerivedFillQuestionRound
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState

/** 当前轮次池完成后，进入会话内下一/上一「轮次号」池中的 pending 题。 */
object PracticeFullAnswerNextRoundPoolPipeline {

    fun resolvePendingInAdjacentRoundPool(
        questions: List<Question>,
        questionsWithState: List<QuestionWithState>,
        currentIndex: Int,
        fullAnswerRequireCorrect: Boolean,
        forward: Boolean,
        randomOrder: Boolean
    ): Int? {
        val current = questions.getOrNull(currentIndex) ?: return null
        val currentRound = PracticeFullAnswerRoundPoolPipeline.roundOf(current.id)
        val targetRound = if (forward) currentRound + 1 else currentRound - 1
        if (targetRound < 1) return null
        val pool = questions.indices.filter { index ->
            PracticeFullAnswerRoundPoolPipeline.roundOf(questions[index].id) == targetRound
        }
        val pending = pool.filter { index ->
            PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(
                questionsWithState[index],
                fullAnswerRequireCorrect
            )
        }
        if (pending.isEmpty()) return null
        return if (forward) pending.minOrNull() else pending.maxOrNull()
            ?: pending.firstOrNull()
    }

    fun maxRoundInSession(questions: List<Question>): Int =
        questions.maxOfOrNull { extractDerivedFillQuestionRound(it.id) ?: 1 } ?: 1
}
