package com.example.testapp.core.session.strategy.edit

import com.example.testapp.domain.session.QuestionSessionKind
import org.junit.Assert.assertEquals
import org.junit.Test

class QuestionEditSessionStrategyBootstrapTest {
    @Test
    fun kind_carriesQuizAndQuestionId() {
        val kind = QuestionEditSessionStrategyBootstrap.kind("quiz.json", questionId = 42)
        assertEquals(QuestionSessionKind.QuestionEdit("quiz.json", 42), kind)
    }
}
