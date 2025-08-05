package com.example.testapp.util

import android.content.Context
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.R

class SoundEffects(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(2).build()
    private val correctId = soundPool.load(context, R.raw.correct, 1)
    private val wrongId = soundPool.load(context, R.raw.wrong, 1)

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
fun rememberSoundEffects(): SoundEffects {
    val context = LocalContext.current
    val effects = remember { SoundEffects(context) }
    DisposableEffect(Unit) {
        onDispose { effects.release() }
    }
    return effects
}