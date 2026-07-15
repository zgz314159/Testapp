package com.example.testapp.presentation.screen.exam

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.screen.shared.SessionAnalysisLoader
import com.example.testapp.presentation.screen.shared.SessionAnalysisSyncPipeline
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
import kotlinx.coroutines.launch

/** 考试 AI 解析同步：切题 / 批改展示 / 从 AI 页返回时从 DB 刷新到会话。 */
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
    aiViewModel: DeepSeekViewModel,
    sparkViewModel: SparkViewModel,
    baiduQianfanViewModel: BaiduQianfanViewModel,
    dispatchCommand: (SessionCommand) -> Unit,
    loader: SessionAnalysisLoader,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val parsingKeyword = pollingText.removeSuffix("...")

    suspend fun syncStored() {
        val id = questionId ?: return
        SessionAnalysisSyncPipeline.syncStoredForQuestion(
            questionId = id,
            questionStem = questionStem,
            index = currentIndex,
            loader = loader,
            dispatch = dispatchCommand,
        )
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
            dispatchCommand(SessionCommand.UpdateAnalysis(pair.first, pair.second.orEmpty()))
        }
    }

    LaunchedEffect(sparkPair) {
        val pair = sparkPair
        if (pair != null && pair.second != pollingText && !pair.second.orEmpty().startsWith(parsingKeyword)) {
            dispatchCommand(SessionCommand.UpdateSparkAnalysis(pair.first, pair.second.orEmpty()))
        }
    }

    LaunchedEffect(chatGptResult) {
        val pair = chatGptResult
        if (pair != null && pair.second != pollingText && !pair.second.orEmpty().startsWith(parsingKeyword)) {
            dispatchCommand(SessionCommand.UpdateBaiduAnalysis(pair.first, pair.second.orEmpty()))
        }
    }
}
