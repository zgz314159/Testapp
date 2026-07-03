package com.example.testapp.presentation.screen.practice

/** 练习页 QuizInit 是否需 reloadForFillConfig（避免 overlay 返回误重载）。 */
object PracticeQuizInitReloadPipeline {

    fun buildInitKey(fillConfigVersion: String, practiceCount: Int, randomPractice: Boolean): String =
        listOf(fillConfigVersion, practiceCount, randomPractice).joinToString("|")

    fun shouldReloadFillConfig(
        sessionActive: Boolean,
        appliedInitKey: String?,
        currentInitKey: String
    ): Boolean = !sessionActive || appliedInitKey != currentInitKey
}
