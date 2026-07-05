package com.example.testapp.presentation.session.exam

import com.example.testapp.core.common.FontSettingsSnapshot
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.exam.ExternalExamState

/** 从 ExamScreenQuizInitEffect 抽出的会话启动逻辑 */
object ExamSessionBootstrap {
    fun startExam(
        bindings: ExamScreenBindings,
        snapshot: FontSettingsSnapshot,
        quizId: String,
        wrongBookFileName: String? = null,
        favoriteFileName: String? = null,
        progressLoaded: Boolean = false,
    ) {
        ExamSessionCommandHandler.handle(
            bindings,
            SessionCommand.SetRandomExam(snapshot.randomExam),
        )
        ExamSessionCommandHandler.handle(
            bindings,
            SessionCommand.SetMemoryModeConfig(
                enabled = snapshot.examMemoryMode,
                batchSize = snapshot.examMemoryBatchSize,
                wrongMode = snapshot.examMemoryWrongMode,
                poolMode = snapshot.examMemoryPoolMode,
            ),
        )
        when {
            wrongBookFileName != null -> {
                if (progressLoaded) {
                    ExamSessionCommandHandler.handle(bindings, SessionCommand.ReloadForFillConfig())
                } else {
                    ExamSessionCommandHandler.handle(
                        bindings,
                        SessionCommand.LoadWrongQuestions(
                            wrongBookFileName,
                            snapshot.examQuestionCount,
                            snapshot.randomExam,
                        ),
                    )
                }
            }
            favoriteFileName != null -> {
                if (progressLoaded) {
                    ExamSessionCommandHandler.handle(bindings, SessionCommand.ReloadForFillConfig())
                } else {
                    ExamSessionCommandHandler.handle(
                        bindings,
                        SessionCommand.LoadFavoriteQuestions(
                            favoriteFileName,
                            snapshot.examQuestionCount,
                            snapshot.randomExam,
                        ),
                    )
                }
            }
            else ->
                ExamQuizInitPipeline.dispatch(
                    bindings = bindings,
                    params =
                        ExamQuizInitPipeline.Params(
                            quizId = quizId,
                            examCount = snapshot.examQuestionCount,
                            randomExam = snapshot.randomExam,
                            examMemoryMode = snapshot.examMemoryMode,
                            examMemoryBatchSize = snapshot.examMemoryBatchSize,
                            examMemoryWrongMode = snapshot.examMemoryWrongMode,
                            examMemoryPoolMode = snapshot.examMemoryPoolMode,
                            progressLoaded = progressLoaded,
                        ),
                )
        }
    }

    fun startReview(
        bindings: ExamScreenBindings,
        progressId: String,
        quizFile: String,
        externalState: ExternalExamState,
        wrongBook: Boolean,
        favorite: Boolean,
    ) {
        ExamSessionCommandHandler.handle(
            bindings,
            SessionCommand.EnterExamReviewSession(
                targetProgressId = progressId,
                quizFile = quizFile,
                questionCount = externalState.examCount,
                random = externalState.randomExam,
                wrongBook = wrongBook,
                favorite = favorite,
            ),
        )
    }
}
