package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionExtension
import kotlinx.coroutines.CoroutineScope

class ReviewPracticeSession(
    private val reviewKind: QuestionSessionKind.Review,
    deps: PracticeSessionDeps,
    scope: CoroutineScope,
    extensions: List<SessionExtension> = emptyList(),
) : AbstractPracticeQuestionSession(reviewKind, deps, scope, extensions) {
    override suspend fun start() {
        PracticeSessionCommandHandler.handle(
            bindings,
            SessionCommand.EnterReviewSession(reviewKind.progressId),
        )
        publishSnapshot()
        emitStarted()
        currentQuestionId()?.let { emitQuestionChanged(engine.currentIndex.value, it) }
    }

    override suspend fun destroy() = Unit

    private fun currentQuestionId(): Int? = engine.sessionState.value.currentQuestion?.question?.id
}
