package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import kotlinx.coroutines.CoroutineScope

class PracticeSession(
    private val practiceKind: QuestionSessionKind.Practice,
    deps: PracticeSessionDeps,
    scope: CoroutineScope,
    extensions: List<SessionExtension> = emptyList(),
) : AbstractPracticeQuestionSession(practiceKind, deps, scope, extensions) {
    override suspend fun start() {
        val snapshot = deps.fontSettings.readSettingsSnapshot()
        PracticeSessionBootstrap.startPractice(
            bindings = bindings,
            snapshot = snapshot,
            quizId = practiceKind.quizId,
            wrongBookFileName = practiceKind.wrongBookFileName,
            favoriteFileName = practiceKind.favoriteFileName,
        )
        publishSnapshot()
        emitStarted()
        currentQuestionId()?.let { emitQuestionChanged(engine.currentIndex.value, it) }
    }

    override suspend fun destroy() {
        engine.saveProgress()
    }

    private fun currentQuestionId(): Int? = engine.sessionState.value.currentQuestion?.question?.id
}
