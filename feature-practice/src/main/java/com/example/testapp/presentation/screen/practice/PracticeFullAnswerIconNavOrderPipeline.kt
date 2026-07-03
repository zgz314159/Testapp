package com.example.testapp.presentation.screen.practice

/** 全答底栏箭头：随机/顺序由全答设置决定，否则沿用练习随机开关。 */
object PracticeFullAnswerIconNavOrderPipeline {

    fun usesRandomOrder(
        fullAnswerModeActive: Boolean,
        fullAnswerRandomOrder: Boolean,
        randomPractice: Boolean
    ): Boolean = if (fullAnswerModeActive) fullAnswerRandomOrder else randomPractice
}
