package com.example.testapp.presentation.session.practice

import com.example.testapp.core.session.registry.SessionCreationContext
import com.example.testapp.core.session.registry.SessionCreator
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionEditSessionCreator
    @Inject
    constructor(
        private val practiceDeps: PracticeSessionDeps,
    ) : SessionCreator {
        override fun create(
            kind: QuestionSessionKind,
            context: SessionCreationContext,
            extensions: List<SessionExtension>,
        ): QuestionSession {
            val edit =
                kind as? QuestionSessionKind.QuestionEdit
                    ?: error("QuestionEditSessionCreator only supports QuestionEdit kind")
            return QuestionEditSession(
                editKind = edit,
                deps = practiceDeps,
                scope = context.scope,
                extensions = extensions,
            )
        }
    }
