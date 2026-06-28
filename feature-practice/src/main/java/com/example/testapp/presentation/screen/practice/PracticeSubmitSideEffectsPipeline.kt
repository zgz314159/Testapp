package com.example.testapp.presentation.screen.practice

import kotlinx.coroutines.yield

/** 批改区 reveal 之后再播放音效、回调与错题本，避免阻塞首帧。 */
object PracticeSubmitSideEffectsPipeline {

    suspend fun apply(
        allCorrect: Boolean,
        soundEnabled: Boolean,
        playCorrect: () -> Unit,
        playWrong: () -> Unit,
        onSubmit: (Boolean) -> Unit,
        onWrongAnswer: suspend () -> Unit
    ) {
        yield()
        if (soundEnabled) {
            if (allCorrect) playCorrect() else playWrong()
        }
        onSubmit(allCorrect)
        if (!allCorrect) onWrongAnswer()
    }
}
