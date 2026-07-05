package com.example.testapp.uicommon.util

import android.content.Context
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class SoundEffects(
    context: Context,
    correctResId: Int,
    wrongResId: Int,
) {
    private val soundPool = SoundPool.Builder().setMaxStreams(2).build()
    private val correctId = soundPool.load(context, correctResId, 1)
    private val wrongId = soundPool.load(context, wrongResId, 1)

    fun playCorrect() {
        soundPool.play(correctId, 1f, 1f, 1, 0, 1f)
    }

    fun playWrong() {
        soundPool.play(wrongId, 2f, 2f, 2, 0, 2f)
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
fun rememberSoundEffects(correctResId: Int, wrongResId: Int): SoundEffects {
    val context = LocalContext.current
    val effects = remember(correctResId, wrongResId) { SoundEffects(context, correctResId, wrongResId) }
    DisposableEffect(effects) {
        onDispose { effects.release() }
    }
    return effects
}
