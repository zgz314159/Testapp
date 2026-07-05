package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.presentation.screen.practice.PracticeStateUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSessionGradeDelegateTest {
    @Test
    fun updateShowResult_true_notifiesAnsweredSnapshot() {
        val fixture = fixture()
        fixture.delegate.updateShowResult(0, true)
        assertEquals(listOf(0), fixture.answeredSnapshots)
        assertTrue(fixture.state.value.questionsWithState[0].showResult)
    }

    @Test
    fun revealShowResult_persistsAndNotifies() =
        runBlocking {
            val fixture = fixture(scope = CoroutineScope(Dispatchers.Unconfined))
            fixture.delegate.revealShowResult(0)
            assertEquals(listOf(0), fixture.answeredSnapshots)
            assertEquals(2, fixture.saveCount)
            assertTrue(fixture.state.value.questionsWithState[0].showResult)
        }

    @Test
    fun gradeSessionOnSubmit_revealsInputAnswers() =
        runBlocking {
            val fixture =
                fixture(
                    initial =
                        sampleState(
                            questions =
                                listOf(
                                    questionWithState(id = 1, selected = listOf(0)),
                                    questionWithState(id = 2),
                                ),
                        ),
                )
            val snapshot = fixture.delegate.gradeSessionOnSubmit()
            assertEquals(1, snapshot.sessionAnsweredCount)
            assertTrue(fixture.state.value.questionsWithState[0].showResult)
            assertFalse(fixture.state.value.questionsWithState[1].showResult)
        }

    private fun fixture(
        initial: PracticeSessionState = sampleState(),
        scope: kotlinx.coroutines.CoroutineScope =
            kotlinx.coroutines.CoroutineScope(
                kotlinx.coroutines.Dispatchers.Unconfined,
            ),
    ): Fixture {
        val state = MutableStateFlow(initial)
        var saveCount = 0
        val answered = mutableListOf<Int>()
        val updater = PracticeStateUpdater(state) { saveCount++ }
        val delegate =
            PracticeSessionGradeDelegate(
                stateUpdater = updater,
                sessionState = state,
                scope = scope,
                onAnsweredSnapshot = { answered += it },
                saveProgress = { saveCount++ },
            )
        return Fixture(state, delegate, answered) { saveCount }
    }

    private class Fixture(
        val state: MutableStateFlow<PracticeSessionState>,
        val delegate: PracticeSessionGradeDelegate,
        val answeredSnapshots: MutableList<Int>,
        private val saveCounter: () -> Int,
    ) {
        val saveCount: Int get() = saveCounter()
    }

    private fun sampleState(
        questions: List<QuestionWithState> =
            listOf(
                questionWithState(id = 1),
            ),
    ): PracticeSessionState = PracticeSessionState(questionsWithState = questions, currentIndex = 0)

    private fun questionWithState(
        id: Int,
        selected: List<Int> = emptyList(),
    ): QuestionWithState {
        val q =
            Question(
                id = id,
                content = "q",
                type = "single",
                options = listOf("A"),
                answer = "A",
                explanation = "",
            )
        return QuestionWithState(question = q, selectedOptions = selected)
    }
}
