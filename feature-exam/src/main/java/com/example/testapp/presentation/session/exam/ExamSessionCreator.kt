package com.example.testapp.presentation.session.exam

import com.example.testapp.core.session.registry.SessionCreationContext
import com.example.testapp.core.session.registry.SessionCreator
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamSessionCreator
    @Inject
    constructor(
        private val examDeps: ExamSessionDeps,
    ) : SessionCreator {
        override fun create(
            kind: QuestionSessionKind,
            context: SessionCreationContext,
            extensions: List<SessionExtension>,
        ): QuestionSession {
            val exam =
                kind as? QuestionSessionKind.Exam
                    ?: error("ExamSessionCreator only supports Exam kind")
            return ExamSession(
                examKind = exam,
                deps = examDeps,
                scope = context.scope,
                extensions = extensions,
            )
        }
    }
