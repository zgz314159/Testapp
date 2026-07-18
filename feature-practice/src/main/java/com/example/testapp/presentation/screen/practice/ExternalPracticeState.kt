package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.model.Question

data class ExternalPracticeState(
    val randomPractice: Boolean = false,
    val practiceCount: Int = 0,
    val fillConfigVersion: String = "",
    val fillGenerationMode: FillQuestionGenerationMode? = null,
    val adaptiveFading: Boolean = false,
    val fontSize: Float = 16f,
    val correctDelay: Int = 0,
    val wrongDelay: Int = 0,
    val soundEnabled: Boolean = false,
    val settingsReady: Boolean = false,
    val isFavorite: Boolean = false,
    val onToggleFavorite: () -> Unit = {},
    val analysisPair: Pair<Int, String>? = null,
    val sparkPair: Pair<Int, String>? = null,
    val baiduPair: Pair<Int, String>? = null,
    val playCorrect: () -> Unit = {},
    val playWrong: () -> Unit = {},
    val onWrongAnswer: (Question, List<Int>) -> Unit = { _, _ -> },
    val onClearDeepSeek: () -> Unit = {},
    val onClearSpark: () -> Unit = {},
    val onClearBaidu: () -> Unit = {},
    val chatGptLoading: Boolean = false,
    val chatGptResult: Pair<Int, LocalizedResult>? = null,
)
