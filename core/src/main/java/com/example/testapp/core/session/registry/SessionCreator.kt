package com.example.testapp.core.session.registry

import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension

fun interface SessionCreator {
    fun create(
        kind: QuestionSessionKind,
        context: SessionCreationContext,
        extensions: List<SessionExtension>,
    ): QuestionSession
}
