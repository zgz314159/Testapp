package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.session.registry.SessionCreationContext
import com.example.testapp.core.session.registry.SessionCreator
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.presentation.session.practice.PracticeSessionDeps
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MagneticRebuildSessionCreator
    @Inject
    constructor(
        private val practiceDeps: PracticeSessionDeps,
    ) : SessionCreator {
        override fun create(
            kind: QuestionSessionKind,
            context: SessionCreationContext,
            extensions: List<SessionExtension>,
        ): QuestionSession {
            val magnetic =
                kind as? QuestionSessionKind.MagneticRebuild
                    ?: error("MagneticRebuildSessionCreator only supports MagneticRebuild kind")
            return MagneticRebuildSession(magnetic, practiceDeps, context.scope)
        }
    }
