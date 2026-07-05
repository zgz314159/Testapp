package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeProgressLocalResetPipelineTest {
    @Test
    fun resetQuestions_clearsAnswersAndIndex() {
        val question = Question(id = 1, content = "q", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f")
        val state =
            PracticeSessionState(
                currentIndex = 3,
                progressLoaded = true,
                questionsWithState =
                    listOf(
                        QuestionWithState(
                            question = question,
                            selectedOptions = listOf(0),
                            showResult = true,
                            analysis = "a",
                            sparkAnalysis = "s",
                            baiduAnalysis = "b",
                            note = "n",
                        ),
                    ),
            )
        val reset = PracticeProgressLocalResetPipeline.resetQuestions(state)
        assertEquals(0, reset.currentIndex)
        assertFalse(reset.progressLoaded)
        val item = reset.questionsWithState.single()
        assertTrue(item.selectedOptions.isEmpty())
        assertFalse(item.showResult)
        assertEquals("", item.analysis)
        assertEquals("", item.note)
    }
}
