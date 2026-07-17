package com.example.testapp.presentation.session.adaptive

import com.example.testapp.domain.repository.AdaptiveAtomRepository
import com.example.testapp.domain.session.FeatureExtension
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdaptiveFadingProgressExtension
    @Inject
    constructor(
        private val repository: AdaptiveAtomRepository,
    ) : FeatureExtension {
        override fun supports(kind: QuestionSessionKind): Boolean = kind is QuestionSessionKind.AdaptiveFading

        override suspend fun onEvent(
            event: SessionEvent,
            snapshot: SessionSnapshot,
            dispatch: (SessionCommand) -> Unit,
        ) {
            if (event !is SessionEvent.AnswerSubmitted) return
            val kind = snapshot.kind as? QuestionSessionKind.AdaptiveFading ?: return
            val question = snapshot.questions.getOrNull(event.index) ?: return
            val correct = question.isCorrect ?: return
            val current = repository.getState(kind.quizId, question.id) ?: return
            repository.upsertStates(
                listOf(
                    AdaptiveAtomProgressionPipeline.next(
                        current = current,
                        correct = correct,
                        reviewedAt = System.currentTimeMillis(),
                    ),
                ),
            )
        }
    }
