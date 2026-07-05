package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.practice.PracticeQuizInitReloadPipeline

/** QuizInit 单管道：Bootstrap 与 PracticeScreenQuizInitEffect 共用 */
object PracticeQuizInitPipeline {
    data class Params(
        val quizId: String,
        val wrongBookFileName: String?,
        val favoriteFileName: String?,
        val fillConfigVersion: String,
        val practiceCount: Int,
        val randomPractice: Boolean,
    )

    fun dispatch(
        bindings: PracticeScreenBindings,
        params: Params,
    ) {
        val count = params.practiceCount
        val random = params.randomPractice
        val targetProgressId =
            when {
                params.wrongBookFileName != null -> "practice_wrongbook_${params.wrongBookFileName}"
                params.favoriteFileName != null -> "practice_favorite_${params.favoriteFileName}"
                else -> "practice_${params.quizId}"
            }
        PracticeSessionCommandHandler.handle(
            bindings,
            SessionCommand.SetRandomPractice(random),
        )
        val initKey =
            PracticeQuizInitReloadPipeline.buildInitKey(
                params.fillConfigVersion,
                count,
                random,
            )
        if (targetProgressId == bindings.currentProgressId && bindings.currentProgressId.isNotBlank()) {
            if (bindings.shouldReloadForQuizInit(initKey)) {
                PracticeSessionCommandHandler.handle(
                    bindings,
                    SessionCommand.ReloadForFillConfig(count, initKey),
                )
            }
            return
        }
        when {
            params.wrongBookFileName != null -> {
                PracticeSessionCommandHandler.handle(
                    bindings,
                    SessionCommand.SetProgressId(
                        id = targetProgressId,
                        questionsId = params.wrongBookFileName,
                        loadQuestions = false,
                        random = random,
                    ),
                )
                PracticeSessionCommandHandler.handle(
                    bindings,
                    SessionCommand.LoadWrongQuestions(params.wrongBookFileName),
                )
            }
            params.favoriteFileName != null -> {
                PracticeSessionCommandHandler.handle(
                    bindings,
                    SessionCommand.SetProgressId(
                        id = targetProgressId,
                        questionsId = params.favoriteFileName,
                        loadQuestions = false,
                        random = random,
                    ),
                )
                PracticeSessionCommandHandler.handle(
                    bindings,
                    SessionCommand.LoadFavoriteQuestions(params.favoriteFileName),
                )
            }
            else -> {
                PracticeSessionCommandHandler.handle(
                    bindings,
                    SessionCommand.SetProgressId(
                        id = params.quizId,
                        questionsId = params.quizId,
                        questionCount = count,
                        random = random,
                    ),
                )
            }
        }
    }
}
