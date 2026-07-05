package com.example.testapp.presentation.session.exam

import com.example.testapp.domain.session.SessionCommand

/** QuizInit 单管道：Bootstrap 与 ExamScreenQuizInitEffect 共用 */
object ExamQuizInitPipeline {
    data class Params(
        val quizId: String,
        val examCount: Int,
        val randomExam: Boolean,
        val examMemoryMode: Boolean,
        val examMemoryBatchSize: Int,
        val examMemoryWrongMode: Int,
        val examMemoryPoolMode: Int,
        val progressLoaded: Boolean,
    )

    fun dispatch(
        bindings: ExamScreenBindings,
        params: Params,
    ) {
        ExamSessionCommandHandler.handle(
            bindings,
            SessionCommand.SetRandomExam(params.randomExam),
        )
        ExamSessionCommandHandler.handle(
            bindings,
            SessionCommand.SetMemoryModeConfig(
                enabled = params.examMemoryMode,
                batchSize = params.examMemoryBatchSize,
                wrongMode = params.examMemoryWrongMode,
                poolMode = params.examMemoryPoolMode,
            ),
        )
        when {
            params.progressLoaded ->
                ExamSessionCommandHandler.handle(
                    bindings,
                    SessionCommand.ReloadForFillConfig(),
                )
            else ->
                ExamSessionCommandHandler.handle(
                    bindings,
                    SessionCommand.LoadQuestions(params.quizId, params.examCount, params.randomExam),
                )
        }
    }
}
