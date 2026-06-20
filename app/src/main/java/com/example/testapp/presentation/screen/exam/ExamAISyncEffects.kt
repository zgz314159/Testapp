package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel

/**
 * Grouped AI-analysis sync LaunchedEffects extracted from ExamScreen.
 * - Clear Spark on question change
 * - Load saved analyses (DeepSeek/Spark/Baidu) on show-result
 * - Sync streaming result pairs back to ExamViewModel on completion
 */
@Composable
fun ExamAISyncEffects(
    currentIndex: Int,
    questionId: Int?,
    showResultForIndex: Boolean?,
    pollingText: String,
    analysisPair: Pair<Int, String?>?,
    sparkPair: Pair<Int, String?>?,
    chatGptResult: Pair<Int, String?>?,
    viewModel: ExamViewModel,
    aiViewModel: DeepSeekViewModel,
    sparkViewModel: SparkViewModel,
    baiduQianfanViewModel: BaiduQianfanViewModel,
    resolveLocalized: (com.example.testapp.core.common.LocalizedResult?) -> String
) {
    LaunchedEffect(currentIndex) { sparkViewModel.clear() }

    LaunchedEffect(currentIndex, questionId, showResultForIndex) {
        if (showResultForIndex != true) return@LaunchedEffect
        val id = questionId ?: return@LaunchedEffect
        val saved = aiViewModel.getSavedAnalysis(id) ?: ""
        if (saved.isNotBlank()) viewModel.updateAnalysis(currentIndex, saved)
        val sparkSaved = sparkViewModel.getSavedAnalysis(id) ?: ""
        if (sparkSaved.isNotBlank()) viewModel.updateSparkAnalysis(currentIndex, sparkSaved)
        val baiduSaved = baiduQianfanViewModel.getSavedAnalysis(id) ?: ""
        if (baiduSaved.isNotBlank()) viewModel.updateBaiduAnalysis(currentIndex, baiduSaved)
    }

    LaunchedEffect(analysisPair) {
        val pair = analysisPair
        if (pair != null && pair.second != pollingText)
            viewModel.updateAnalysis(pair.first, pair.second ?: "")
    }

    LaunchedEffect(sparkPair) {
        val pair = sparkPair
        if (pair != null && pair.second != pollingText)
            viewModel.updateSparkAnalysis(pair.first, pair.second ?: "")
    }

    LaunchedEffect(chatGptResult) {
        val pair = chatGptResult
        if (pair != null && pair.second != pollingText)
            viewModel.updateBaiduAnalysis(pair.first, pair.second ?: "")
    }
}


