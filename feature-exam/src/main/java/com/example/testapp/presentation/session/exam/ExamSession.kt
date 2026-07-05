package com.example.testapp.presentation.session.exam

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.presentation.screen.exam.ExternalExamState
import kotlinx.coroutines.CoroutineScope

class ExamSession(
    private val examKind: QuestionSessionKind.Exam,
    deps: ExamSessionDeps,
    scope: CoroutineScope,
    extensions: List<SessionExtension> = emptyList(),
) : AbstractExamQuestionSession(examKind, deps, scope, extensions) {
    override suspend fun start() {
        val snapshot = deps.fontSettings.readSettingsSnapshot()
        val reviewId = examKind.reviewProgressId
        if (!reviewId.isNullOrBlank()) {
            ExamSessionBootstrap.startReview(
                bindings = bindings,
                progressId = reviewId,
                quizFile = examKind.quizId,
                externalState =
                    ExternalExamState(
                        examCount = snapshot.examQuestionCount,
                        randomExam = snapshot.randomExam,
                        fillConfigVersion = "",
                        examMemoryMode = snapshot.examMemoryMode,
                        examMemoryBatchSize = snapshot.examMemoryBatchSize,
                        examMemoryWrongMode = snapshot.examMemoryWrongMode,
                        examMemoryPoolMode = snapshot.examMemoryPoolMode,
                        fontSize = snapshot.fontSize,
                        examDelay = snapshot.examDelay.toLong(),
                    ),
                wrongBook = examKind.wrongBookFileName != null,
                favorite = examKind.favoriteFileName != null,
            )
        } else {
            bindings.resetLoadState()
            ExamSessionBootstrap.startExam(
                bindings = bindings,
                snapshot = snapshot,
                quizId = examKind.quizId,
                wrongBookFileName = examKind.wrongBookFileName,
                favoriteFileName = examKind.favoriteFileName,
                progressLoaded = false,
            )
        }
        publishSnapshot()
        emitStarted()
        currentQuestionId()?.let { emitQuestionChanged(engine.currentIndex.value, it) }
    }

    override suspend fun destroy() = Unit

    private fun currentQuestionId(): Int? = engine.sessionState.value.currentQuestion?.question?.id
}
