package com.example.testapp.presentation.session.browse

import com.example.testapp.core.session.registry.SessionCreationContext
import com.example.testapp.core.session.registry.SessionCreator
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrowseSessionCreator
    @Inject
    constructor(
        private val browseDeps: BrowseSessionDeps,
    ) : SessionCreator {
        override fun create(
            kind: QuestionSessionKind,
            context: SessionCreationContext,
            extensions: List<SessionExtension>,
        ): QuestionSession {
            val browse =
                kind as? QuestionSessionKind.Browse
                    ?: error("BrowseSessionCreator only supports Browse kind")
            return BrowseSession(
                kind = browse,
                deps = browseDeps,
                scope = context.scope,
                extensions = extensions,
            )
        }
    }
