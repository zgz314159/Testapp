package com.example.testapp.presentation.session.adaptive

import com.example.testapp.core.session.registry.SessionCreationContext
import com.example.testapp.core.session.registry.SessionCreator
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.presentation.session.practice.PracticeSessionDeps
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdaptiveFadingSessionCreator
    @Inject
    constructor(
        private val practiceDeps: PracticeSessionDeps,
    ) : SessionCreator {
        override fun create(
            kind: QuestionSessionKind,
            context: SessionCreationContext,
            extensions: List<SessionExtension>,
        ): QuestionSession {
            val adaptive =
                kind as? QuestionSessionKind.AdaptiveFading
                    ?: error("AdaptiveFadingSessionCreator only supports AdaptiveFading kind")
            return AdaptiveFadingSession(adaptive, practiceDeps, context.scope, extensions)
        }
    }
