package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeFullAnswerIconUnansweredPipelineTest {

    private fun qws(id: Int, showResult: Boolean) = QuestionWithState(
        question = Question(id, "c", "fill", emptyList(), "a", ""),
        showResult = showResult
    )

    @Test
    fun resolveNext_sequential_wrapsWhenNoPendingAfterAnchor() {
        val states = listOf(
            qws(1, showResult = false),
            qws(2, showResult = true),
            qws(3, showResult = false)
        )
        val (result, target) = PracticeFullAnswerIconUnansweredPipeline.resolveNextIndex(
            anchorIndex = 2,
            questionsWithState = states,
            isPending = { !it.showResult },
            randomOrder = false
        )
        assertEquals(UnansweredNavResult.Navigated, result)
        assertEquals(0, target)
    }

    @Test
    fun resolvePrev_sequential_wrapsWhenNoPendingBeforeAnchor() {
        val states = listOf(
            qws(1, showResult = true),
            qws(2, showResult = false),
            qws(3, showResult = false)
        )
        val (result, target) = PracticeFullAnswerIconUnansweredPipeline.resolvePrevIndex(
            anchorIndex = 0,
            questionsWithState = states,
            isPending = { !it.showResult },
            randomOrder = false
        )
        assertEquals(UnansweredNavResult.Navigated, result)
        assertEquals(2, target)
    }
}
