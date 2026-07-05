package com.example.testapp.presentation.screen

import com.example.testapp.domain.model.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeSessionEngineTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setProgressId loads questions and marks progress loaded`() =
        runBlocking {
            runWithEngine(
                FakeQuestionRepository(listOf(singleChoiceQuestion(1), singleChoiceQuestion(2))),
                FakePracticeProgressRepository(),
            ) { engine ->
                engine.setProgressId(id = "file1", questionsId = "file1")
                engine.awaitLoaded(questionCount = 2)

                assertTrue(engine.sessionState.value.progressLoaded)
                assertEquals(listOf(1, 2), engine.sessionState.value.questions.map { it.id })
                assertEquals("practice_file1", engine.currentProgressId)
            }
        }

    @Test
    fun `answerQuestion updates state and saves progress`() =
        runBlocking {
            val progressRepository = FakePracticeProgressRepository()
            runWithEngine(
                FakeQuestionRepository(listOf(singleChoiceQuestion(1), singleChoiceQuestion(2))),
                progressRepository,
            ) { engine ->
                engine.setProgressId(id = "file1", questionsId = "file1")
                engine.awaitLoaded(questionCount = 2)
                engine.answerQuestion(0)
                progressRepository.awaitProgress("practice_file1") {
                    it?.questionStateMap?.get(1)?.selectedOptions == listOf(0)
                }

                val currentQuestion = engine.sessionState.value.questionsWithState.first()
                val saved = progressRepository.savedProgress("practice_file1")
                assertEquals(listOf(0), currentQuestion.selectedOptions)
                assertEquals(listOf(0), saved?.questionStateMap?.get(1)?.selectedOptions)
            }
        }

    @Test
    fun `navigation uses public state only`() =
        runBlocking {
            runWithEngine(
                FakeQuestionRepository(
                    listOf(singleChoiceQuestion(1), singleChoiceQuestion(2), singleChoiceQuestion(3)),
                ),
                FakePracticeProgressRepository(),
            ) { engine ->
                engine.setProgressId(id = "file1", questionsId = "file1")
                engine.awaitLoaded(questionCount = 3)
                engine.nextQuestion()
                engine.nextQuestion()
                engine.prevQuestion()
                engine.goToQuestion(2)

                assertEquals(2, engine.sessionState.value.currentIndex)
                assertEquals(3, engine.sessionState.value.currentQuestion?.question?.id)
            }
        }

    @Test
    fun `clearProgress clears persisted and local answer state`() =
        runBlocking {
            val progressRepository = FakePracticeProgressRepository()
            runWithEngine(
                FakeQuestionRepository(listOf(singleChoiceQuestion(1))),
                progressRepository,
            ) { engine ->
                engine.setProgressId(id = "file1", questionsId = "file1")
                engine.awaitLoaded(questionCount = 1)
                engine.answerQuestion(0)
                progressRepository.awaitProgress("practice_file1") { it != null }
                engine.clearProgress()
                engine.awaitUntil {
                    it.questionsWithState.first().selectedOptions.isEmpty() &&
                        !it.questionsWithState.first().showResult
                }

                assertFalse(engine.sessionState.value.questionsWithState.first().showResult)
                assertEquals(emptyList<Int>(), engine.sessionState.value.questionsWithState.first().selectedOptions)
                progressRepository.awaitProgress("practice_file1") { it == null }
            }
        }

    @Test
    fun `prepare and save edited question use public edit API`() =
        runBlocking {
            val questionRepository = FakeQuestionRepository(listOf(singleChoiceQuestion(1)))
            runWithEngine(questionRepository, FakePracticeProgressRepository()) { engine ->
                engine.setProgressId(id = "file1", questionsId = "file1")
                engine.awaitLoaded(questionCount = 1)
                engine.prepareEditableQuestion(1)
                val edited = engine.editableQuestion.value!!.copy(content = "edited")
                val saved = engine.saveEditedQuestion(edited)

                assertEquals("Question 1", engine.editableQuestion.value?.content)
                assertTrue(saved)
                assertTrue(engine.saveSuccess.value)
                assertEquals("edited", questionRepository.savedQuestionsFor("file1").single().content)
            }
        }

    @Test
    fun `setProgressId handles empty question file without stale questions`() =
        runBlocking {
            runWithEngine(FakeQuestionRepository(emptyList()), FakePracticeProgressRepository()) { engine ->
                engine.setProgressId(id = "empty", questionsId = "empty")
                engine.awaitLoaded(questionCount = 0)

                assertEquals(emptyList<Question>(), engine.sessionState.value.questions)
                assertTrue(engine.sessionState.value.progressLoaded)
            }
        }
}
