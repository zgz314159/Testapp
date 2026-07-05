package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import kotlinx.coroutines.CoroutineScope

class QuestionEditSession(
    private val editKind: QuestionSessionKind.QuestionEdit,
    deps: PracticeSessionDeps,
    scope: CoroutineScope,
    extensions: List<SessionExtension> = emptyList(),
) : AbstractPracticeQuestionSession(editKind, deps, scope, extensions) {
    override suspend fun start() {
        QuestionEditSessionBootstrap.start(bindings, editKind)
        publishSnapshot()
        emitStarted()
    }

    override suspend fun destroy() = Unit
}
