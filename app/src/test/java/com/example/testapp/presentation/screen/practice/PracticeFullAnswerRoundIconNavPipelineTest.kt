package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.buildDerivedFillQuestionId
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PracticeFullAnswerRoundIconNavPipelineTest {

    private fun question(id: Int) = Question(id, "c", "fill", emptyList(), "a", "")

    private fun qws(id: Int, text: String) = QuestionWithState(
        question = question(id),
        textAnswer = text,
        selectedOptions = if (text.isNotBlank()) listOf(-1) else emptyList()
    )

    @Test
    fun pendingIndices_scopedToSameSourceSameRound() {
        val q1r1 = buildDerivedFillQuestionId(1, 0)
        val q2r1 = buildDerivedFillQuestionId(2, 0)
        val questions = listOf(question(q1r1), question(q2r1))
        val states = listOf(qws(q1r1, ""), qws(q2r1, ""))
        val pending = PracticeFullAnswerRoundIconNavPipeline.pendingIndicesInRound(
            questions, states, currentIndex = 0, fullAnswerRequireCorrect = false
        )
        assertEquals(listOf(0), pending)
    }

    @Test
    fun pendingIndices_excludesCurrentAfterInput() {
        val q1r1 = buildDerivedFillQuestionId(1, 0)
        val questions = listOf(question(q1r1))
        val states = listOf(qws(q1r1, "x"))
        val pending = PracticeFullAnswerRoundIconNavPipeline.pendingIndicesInRound(
            questions, states, currentIndex = 0, fullAnswerRequireCorrect = false
        )
        assertEquals(emptyList<Int>(), pending)
    }

    @Test
    fun resolveTarget_movesToOtherPendingInSameRound() {
        val pending = listOf(0, 2)
        assertEquals(
            2,
            PracticeFullAnswerRoundIconNavPipeline.resolveTargetIndex(
                currentIndex = 0, pendingInRound = pending, forward = true, randomOrder = false
            )
        )
    }

    @Test
    fun hasPendingInRound_falseWhenOnlyOtherSourcePending() {
        val q1r1 = buildDerivedFillQuestionId(1, 0)
        val q2r1 = buildDerivedFillQuestionId(2, 0)
        val questions = listOf(question(q1r1), question(q2r1))
        val states = listOf(qws(q1r1, "answered"), qws(q2r1, ""))
        assertFalse(
            PracticeFullAnswerRoundIconNavPipeline.hasPendingInRound(
                questions, states, 0, fullAnswerRequireCorrect = false
            )
        )
    }

    @Test
    fun advanceFromLaterRoundWhenCurrentRoundSingleSlotPending() {
        val q1r1 = buildDerivedFillQuestionId(90, 0)
        val q1r2 = buildDerivedFillQuestionId(90, 1)
        val q1r4 = buildDerivedFillQuestionId(90, 3)
        val questions = listOf(question(q1r1), question(q1r2), question(q1r4))
        val states = listOf(
            qws(q1r1, ""),
            qws(q1r2, ""),
            qws(q1r4, "partial")
        )
        assertEquals(
            0,
            PracticeFullAnswerSameSourceRoundAdvancePipeline.resolvePendingInSameSourceOtherRound(
                questions, states, currentIndex = 2,
                fullAnswerRequireCorrect = true, forward = true, randomOrder = false
            )
        )
    }

    @Test
    fun sameSourceAdvance_goesToRound2WhenRound1Complete() {
        val q1r1 = buildDerivedFillQuestionId(1, 0)
        val q1r2 = buildDerivedFillQuestionId(1, 1)
        val questions = listOf(question(q1r1), question(q1r2))
        val states = listOf(qws(q1r1, "x"), qws(q1r2, ""))
        assertEquals(
            1,
            PracticeFullAnswerSameSourceRoundAdvancePipeline.resolvePendingInSameSourceOtherRound(
                questions, states, 0, fullAnswerRequireCorrect = false, forward = true, randomOrder = false
            )
        )
    }
}
