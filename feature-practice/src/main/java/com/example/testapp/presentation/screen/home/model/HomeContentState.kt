package com.example.testapp.presentation.screen.home.model

import com.example.testapp.domain.usecase.FileStatistics

data class HomeContentState(
    val fileNames: List<String> = emptyList(),
    val fileStatistics: Map<String, FileStatistics> = emptyMap(),
    val practiceProgress: Map<String, Int> = emptyMap(),
    val isReady: Boolean = false,
)
