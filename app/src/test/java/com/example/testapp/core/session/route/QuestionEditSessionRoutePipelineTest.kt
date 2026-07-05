package com.example.testapp.core.session.route

import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionEditSessionRoutePipelineTest {
    @Test
    fun route_encodesQuizAndQuestionId() {
        assertEquals(
            "question_edit/encoded%20quiz/42",
            QuestionEditSessionRoutePipeline.route("encoded%20quiz", 42),
        )
    }
}
