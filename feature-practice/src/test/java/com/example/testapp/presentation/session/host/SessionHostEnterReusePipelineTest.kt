package com.example.testapp.presentation.session.host

import com.example.testapp.domain.session.QuestionSessionKind
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionHostEnterReusePipelineTest {

    @Test
    fun samePracticeKind_reuses() {
        val kind = QuestionSessionKind.Practice(quizId = "bank.xlsx")
        assertTrue(SessionHostEnterReusePipeline.shouldReuseExisting(kind, kind))
    }

    @Test
    fun differentQuiz_doesNotReuse() {
        val a = QuestionSessionKind.Practice(quizId = "a.xlsx")
        val b = QuestionSessionKind.Practice(quizId = "b.xlsx")
        assertFalse(SessionHostEnterReusePipeline.shouldReuseExisting(a, b))
    }

    @Test
    fun nullExisting_doesNotReuse() {
        assertFalse(
            SessionHostEnterReusePipeline.shouldReuseExisting(
                null,
                QuestionSessionKind.Practice(quizId = "a.xlsx"),
            ),
        )
    }
}
