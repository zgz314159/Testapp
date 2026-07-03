package com.example.testapp.presentation.screen.practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.yield

/** 批改区晚一帧展示，避免与选项态/跳题同帧重组卡顿。 */
@Composable
fun rememberPracticeResultDisplayReady(showResult: Boolean, questionIndex: Int): Boolean {
    var ready by remember(questionIndex) { mutableStateOf(false) }
    LaunchedEffect(showResult, questionIndex) {
        if (!showResult) {
            ready = false
        } else {
            yield()
            ready = true
        }
    }
    return ready
}
