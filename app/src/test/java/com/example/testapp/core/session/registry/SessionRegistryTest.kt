package com.example.testapp.core.session.registry

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCapabilitiesPresets
import kotlinx.coroutines.test.TestScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionRegistryTest {
    private val testScope = TestScope()

    @Test
    fun create_returnsRegisteredSession() {
        val registry =
            SessionRegistry.builder()
                .register(QuestionSessionKind.Browse::class) { kind, context, _ ->
                    StubQuestionSession(kind, scope = context.scope)
                }
                .build()

        val kind = QuestionSessionKind.Browse(quizId = "quiz.json", targetQuestionId = 42)
        val session =
            registry.create(
                kind,
                SessionCreationContext(EmptySessionDeps, testScope),
            )

        assertEquals(kind, session.kind)
        assertEquals(SessionCapabilitiesPresets.browse, session.capabilities)
        assertTrue(registry.hasCreator(kind))
    }

    @Test
    fun create_questionEdit_returnsRegisteredSession() {
        val registry =
            SessionRegistry.builder()
                .register(QuestionSessionKind.QuestionEdit::class) { kind, context, _ ->
                    StubQuestionSession(kind, scope = context.scope)
                }
                .build()

        val kind = QuestionSessionKind.QuestionEdit(quizId = "quiz.json", questionId = 7)
        val session =
            registry.create(
                kind,
                SessionCreationContext(EmptySessionDeps, testScope),
            )

        assertEquals(kind, session.kind)
        assertEquals(SessionCapabilitiesPresets.questionEdit, session.capabilities)
        assertTrue(registry.hasCreator(kind))
    }

    @Test(expected = IllegalStateException::class)
    fun create_throwsWhenCreatorMissing() {
        SessionRegistry.builder().build()
            .create(
                QuestionSessionKind.Practice("quiz.json"),
                SessionCreationContext(EmptySessionDeps, testScope),
            )
    }

    @Test
    fun hasCreator_falseForUnregistered() {
        val registry = SessionRegistry.builder().build()
        assertFalse(registry.hasCreator(QuestionSessionKind.Exam("quiz.json")))
    }
}
