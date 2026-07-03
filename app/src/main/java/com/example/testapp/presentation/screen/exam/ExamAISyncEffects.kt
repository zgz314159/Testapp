package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.testapp.data.network.deepseek.SessionAnalysisInlineDisplayPipeline
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
import kotlinx.coroutines.launch

/**
 * 考试 AI 解析同步：切题 / 批改展示 / 从 AI 页返回时从 DB 刷新到会话。
 */
@Composable
fun ExamAISyncEffects(
    currentIndex: Int,
    questionId: Int?,
    questionStem: String,
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
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val parsingKeyword = pollingText.removeSuffix("...")

    suspend fun syncStored() {
        val id = questionId ?: return
        val saved = aiViewModel.getSavedAnalysis(id) ?: ""
        if (saved.isNotBlank()) {
            viewModel.updateAnalysis(
                currentIndex,
                SessionAnalysisInlineDisplayPipeline.toDisplayText(saved, questionStem)
            )
        }
        val sparkSaved = sparkViewModel.getSavedAnalysis(id) ?: ""
        if (sparkSaved.isNotBlank()) viewModel.updateSparkAnalysis(currentIndex, sparkSaved)
        val baiduSaved = baiduQianfanViewModel.getSavedAnalysis(id) ?: ""
        if (baiduSaved.isNotBlank()) viewModel.updateBaiduAnalysis(currentIndex, baiduSaved)
    }

    LaunchedEffect(currentIndex) { sparkViewModel.clear() }

    LaunchedEffect(questionId) {
        syncStored()
    }

    LaunchedEffect(currentIndex, questionId, showResultForIndex) {
        if (showResultForIndex == true) syncStored()
    }

    DisposableEffect(lifecycle, currentIndex, questionId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch { syncStored() }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(analysisPair) {
        val pair = analysisPair
        if (pair != null && pair.second != pollingText && !pair.second.orEmpty().startsWith(parsingKeyword)) {
            viewModel.updateAnalysis(pair.first, pair.second ?: "")
        }
    }

    LaunchedEffect(sparkPair) {
        val pair = sparkPair
        if (pair != null && pair.second != pollingText && !pair.second.orEmpty().startsWith(parsingKeyword)) {
            viewModel.updateSparkAnalysis(pair.first, pair.second ?: "")
        }
    }

    LaunchedEffect(chatGptResult) {
        val pair = chatGptResult
        if (pair != null && pair.second != pollingText && !pair.second.orEmpty().startsWith(parsingKeyword)) {
            viewModel.updateBaiduAnalysis(pair.first, pair.second ?: "")
        }
    }
}
