package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeProgressSavePayloadPipelineTest {
    @Test
    fun buildExtras_containsMapAndOrder() {
        val q1 = Question(id = 1, content = "q1", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f")
        val q2 = Question(id = 2, content = "q2", type = "单选", options = listOf("A"), answer = "A", explanation = "", fileName = "f")
        val map =
            mapOf(
                1 to UnifiedQuestionState(questionId = 1, selectedOptions = listOf(0)),
            )
        val extras =
            PracticeProgressSavePayloadPipeline.buildExtras(
                questionStateMap = map,
                questionsWithState = listOf(QuestionWithState(q1), QuestionWithState(q2)),
            )
        @Suppress("UNCHECKED_CAST")
        val order = extras["fixedQuestionOrder"] as List<Int>
        assertEquals(listOf(1, 2), order)
        assertEquals(map, extras["questionStateMap"])
    }
}
