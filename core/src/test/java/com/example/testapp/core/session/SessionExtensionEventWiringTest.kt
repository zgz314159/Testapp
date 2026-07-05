package com.example.testapp.core.session

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionExtensionEventWiringTest {
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    @Test
    fun questionChanged_emitsOnIndexOrQuestionIdChange() =
        runBlocking {
            val state = MutableStateFlow(sampleState(index = 0, questionId = 1))
            val events = mutableListOf<Pair<Int, Int>>()
            SessionExtensionEventWiring.launchQuestionChangedEvents(scope, state) { i, id ->
                events += i to id
            }
            assertEquals(listOf(0 to 1), events)

            state.value = sampleState(index = 1, questionId = 2, questionCount = 2)
            assertEquals(listOf(0 to 1, 1 to 2), events)
        }

    @Test
    fun answerSubmitted_emitsWhenShowResultFlipsTrueOnCurrentIndex() =
        runBlocking {
            val state = MutableStateFlow(sampleState(index = 0, questionId = 5, showResult = false))
            val events = mutableListOf<Pair<Int, Int>>()
            SessionExtensionEventWiring.launchAnswerSubmittedEvents(scope, state) { i, id ->
                events += i to id
            }
            assertTrue(events.isEmpty())

            state.value = sampleState(index = 0, questionId = 5, showResult = true)
            assertEquals(listOf(0 to 5), events)

            state.value = sampleState(index = 0, questionId = 5, showResult = true)
            assertEquals(listOf(0 to 5), events)
        }

    private fun sampleState(
        index: Int,
        questionId: Int,
        showResult: Boolean = false,
        questionCount: Int = index + 1,
    ): PracticeSessionState {
        val questions =
            (1..questionCount).map { n ->
                val id = if (n - 1 == index) questionId else n
                QuestionWithState(
                    question =
                        Question(
                            id = id,
                            content = "c$id",
                            type = "single",
                            options = listOf("A"),
                            answer = "A",
                            explanation = "",
                        ),
                    showResult = if (n - 1 == index) showResult else false,
                )
            }
        return PracticeSessionState(questionsWithState = questions, currentIndex = index)
    }
}
