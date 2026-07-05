package com.example.testapp.presentation.screen

import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.navigation.AiOverlayParentSessions
import com.example.testapp.presentation.navigation.AppNavAiWritebackPipeline
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavAiWritebackPipelineTest {
    @Test
    fun updateAnalysis_routesThroughSessionCommand() =
        runBlocking {
            runWithEngine(
                FakeQuestionRepository(listOf(singleChoiceQuestion(1))),
                FakePracticeProgressRepository(),
            ) { engine ->
                engine.setProgressId(id = "file1", questionsId = "file1")
                engine.awaitLoaded(questionCount = 1)

                AppNavAiWritebackPipeline.updateAnalysis(
                    AiOverlayParentSessions(examBindings = null, practiceBindings = engine),
                    index = 0,
                    text = "parsed",
                )

                assertEquals(
                    "parsed",
                    engine.sessionState.value.questionsWithState.first().analysis,
                )
            }
        }

    @Test
    fun appendNote_commandShape() {
        val command = SessionCommand.AppendNote(questionId = 3, index = 1, text = "【Spark问答】\nbody")
        assertEquals(3, command.questionId)
        assertEquals("【Spark问答】\nbody", command.text)
    }
}
