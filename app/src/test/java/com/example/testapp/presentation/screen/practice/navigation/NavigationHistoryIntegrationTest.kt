package com.example.testapp.presentation.screen.practice.navigation

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationHistoryIntegrationTest {
    private val history = NavigationHistory()

    @Test
    fun snapshot_remember_apply_and_resume_roundtrip() {
        val q1 = answeredQws(1, analysis = "snap", time = 100L)
        val q2 = answeredQws(2, analysis = "live", time = 200L)
        var state =
            PracticeSessionState(
                questionsWithState = listOf(q1, q2),
                currentIndex = 1,
            )

        history.rememberAnsweredHistorySnapshot(q2)
        history.updateAnsweredHistoryNavigation(
            originIndex = 1,
            historyPosition = 0,
            orderedIndices = listOf(1, 0),
        )
        assertTrue(history.isInAnsweredHistory)

        state =
            history.applyAnsweredHistorySnapshot(
                currentState = state,
                index = 0,
                isQuestionAnswered = { it.showResult },
            )
        assertEquals(0, state.currentIndex)
        assertEquals("snap", state.questionsWithState[0].analysis)

        val resumed = history.resumeFromAnsweredHistory(state)
        assertEquals(1, resumed.currentIndex)
        assertFalse(history.isInAnsweredHistory)
        assertEquals("live", resumed.questionsWithState[1].analysis)
    }

    @Test
    fun buildPreviousAnsweredIndices_sortsByAnswerTimeDesc() {
        val state =
            PracticeSessionState(
                questionsWithState =
                    listOf(
                        answeredQws(1, time = 100L),
                        answeredQws(2, time = 300L),
                        QuestionWithState(question = question(3)),
                    ),
                currentIndex = 2,
            )
        val ordered =
            history.buildPreviousAnsweredIndices(
                currentState = state,
                effectiveCurrentMemoryRoundQuestionIds = { emptySet() },
                memoryModeActive = false,
                memoryPoolMode = 0,
            )
        assertEquals(listOf(1, 0), ordered)
    }

    @Test
    fun randomNavigation_seed_and_record_origin() {
        val qws =
            listOf(
                answeredQws(1, time = 100L),
                answeredQws(2, time = 200L),
                QuestionWithState(question = question(3)),
            )
        history.seedRandomNavigationHistory(
            questionsWithState = qws,
            currentIndex = 2,
            isQuestionAnswered = { it.showResult },
            randomPracticeEnabled = true,
        )
        history.recordRandomNavigationOrigin(currentIndex = 1, randomPracticeEnabled = true)
        assertTrue(history.navigationState.randomHistory.history.isNotEmpty())
    }

    private fun question(id: Int) =
        Question(
            id = id,
            content = "q$id",
            type = "单选题",
            options = emptyList(),
            answer = "A",
            explanation = "",
        )

    private fun answeredQws(
        id: Int,
        analysis: String = "",
        time: Long,
    ) = QuestionWithState(
        question = question(id),
        showResult = true,
        analysis = analysis,
        sessionAnswerTime = time,
    )
}
