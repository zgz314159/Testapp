package com.example.testapp.presentation.session.adaptive

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.presentation.session.practice.AbstractPracticeQuestionSession
import com.example.testapp.presentation.session.practice.PracticeSessionDeps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

class AdaptiveFadingSession(
    private val adaptiveKind: QuestionSessionKind.AdaptiveFading,
    deps: PracticeSessionDeps,
    scope: CoroutineScope,
    extensions: List<SessionExtension> = emptyList(),
) : AbstractPracticeQuestionSession(adaptiveKind, deps, scope, extensions) {
    override suspend fun start() {
        val sourceQuestions = deps.facade.questions.get(adaptiveKind.quizId).first()
        val storedStates = deps.adaptiveAtoms.getStates(adaptiveKind.quizId)
        val settings = deps.fontSettings.readSettingsSnapshot()
        val prepared =
            AdaptiveFadingQuestionPipeline.prepare(
                bankId = adaptiveKind.quizId,
                sourceQuestions = sourceQuestions,
                storedStates = storedStates,
                requestedCount = settings.practiceQuestionCount,
                now = System.currentTimeMillis(),
            )
        deps.adaptiveAtoms.upsertStates(prepared.states)
        bindings.loadPreparedAdaptiveQuestions(adaptiveKind.quizId, prepared.questions)
        publishSnapshot()
        emitStarted()
    }

    override suspend fun destroy() = Unit
}
