package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.toUnifiedSessionState
import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeProgressSaveRequestPipelineTest {
    @Test
    fun build_includesMergedMapAndExtras() {
        val state =
            PracticeSessionState(
                questionsWithState =
                    listOf(
                        QuestionWithState(
                            question =
                                com.example.testapp.domain.model.Question(
                                    id = 1,
                                    content = "q",
                                    type = "单选",
                                    options = listOf("A"),
                                    answer = "A",
                                    explanation = "",
                                    fileName = "f",
                                ),
                        ),
                    ),
            )
        val map = mapOf(1 to UnifiedQuestionState(questionId = 1, selectedOptions = listOf(0)))
        val unified = state.toUnifiedSessionState()
        val request =
            PracticeProgressSaveRequestPipeline.build(
                state = state,
                cumulativeQuestionStateMap = map,
                fillSignature = "sig",
                unifiedState = unified,
            )
        assertEquals(unified, request.unifiedState)
        assertEquals("sig", request.fillSignature)
        assertEquals(1, request.logMapSize)
        assertEquals(1, request.logFixedOrderSize)
        assertEquals(listOf(1), request.extras["fixedQuestionOrder"])
    }
}
