package com.example.testapp.presentation.session.practice

import com.example.testapp.core.common.FontSettingsSnapshot

/** 从 PracticeScreenQuizInitEffect 抽出的会话启动逻辑（VM / Session 双轨复用） */
object PracticeSessionBootstrap {
    fun fillConfigVersion(snapshot: FontSettingsSnapshot): String =
        listOf(
            snapshot.fillQuestionGenerationMode.storageValue,
            snapshot.fillBlankCount,
            snapshot.fillFullAnswerRandomOrder,
            snapshot.fillFullAnswerRequireCorrect,
            snapshot.fillAnswerScoreMin,
            snapshot.fillAnswerScoreMax,
            snapshot.fillAnswerTagFilter,
        ).joinToString("|")

    fun startPractice(
        bindings: PracticeScreenBindings,
        snapshot: FontSettingsSnapshot,
        quizId: String,
        wrongBookFileName: String? = null,
        favoriteFileName: String? = null,
    ) {
        PracticeQuizInitPipeline.dispatch(
            bindings = bindings,
            params =
                PracticeQuizInitPipeline.Params(
                    quizId = quizId,
                    wrongBookFileName = wrongBookFileName,
                    favoriteFileName = favoriteFileName,
                    fillConfigVersion = fillConfigVersion(snapshot),
                    practiceCount = snapshot.practiceQuestionCount,
                    randomPractice = snapshot.randomPractice,
                ),
        )
    }
}
