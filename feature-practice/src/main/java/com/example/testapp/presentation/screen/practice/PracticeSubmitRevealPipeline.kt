package com.example.testapp.presentation.screen.practice

/** 提交后批改区须立即 reveal；副作用与持久化延后。 */
object PracticeSubmitRevealPipeline {
    fun revealImmediately(answeredIndex: Int, revealShowResult: (Int) -> Unit) {
        revealShowResult(answeredIndex)
    }
}
