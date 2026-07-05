package com.example.testapp.presentation.session.practice

import com.example.testapp.core.session.registry.SessionCreationContext
import com.example.testapp.core.session.registry.SessionCreator
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewPracticeSessionCreator
    @Inject
    constructor(
        private val practiceDeps: PracticeSessionDeps,
    ) : SessionCreator {
        override fun create(
            kind: QuestionSessionKind,
            context: SessionCreationContext,
            extensions: List<SessionExtension>,
        ): QuestionSession {
            val review =
                kind as? QuestionSessionKind.Review
                    ?: error("ReviewPracticeSessionCreator only supports Review kind")
            return ReviewPracticeSession(
                reviewKind = review,
                deps = practiceDeps,
                scope = context.scope,
                extensions = extensions,
            )
        }
    }
