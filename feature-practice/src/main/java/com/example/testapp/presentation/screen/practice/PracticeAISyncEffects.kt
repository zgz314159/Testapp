package com.example.testapp.presentation.screen.practice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.screen.shared.SessionAnalysisLoader
import com.example.testapp.presentation.screen.shared.SessionAnalysisSyncPipeline
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel
import kotlinx.coroutines.launch

@Composable
fun PracticeAISyncEffects(
    question: Question?,
    currentIndex: Int,
    showResult: Boolean,
    resultDisplayReady: Boolean,
    parsingText: String,
    analysisPair: Pair<Int, String>?,
    sparkPair: Pair<Int, String>?,
    baiduPair: Pair<Int, String>?,
    aiViewModel: DeepSeekViewModel,
    sparkViewModel: SparkViewModel,
    baiduQianfanViewModel: BaiduQianfanViewModel,
    dispatchCommand: (SessionCommand) -> Unit,
    loader: SessionAnalysisLoader,
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val scope = rememberCoroutineScope()
    val parsingKeyword = parsingText.removeSuffix("...")

    suspend fun syncStored() {
        val q = question ?: return
        SessionAnalysisSyncPipeline.syncStoredForQuestion(
            questionId = q.id,
            questionStem = q.content,
            index = currentIndex,
            loader = loader,
            dispatch = dispatchCommand,
        )
    }

    LaunchedEffect(currentIndex) {
        aiViewModel.clear()
        sparkViewModel.clear()
        baiduQianfanViewModel.clearResult()
    }
    LaunchedEffect(question?.id) {
        syncStored()
    }
    LaunchedEffect(currentIndex, question?.id, showResult) {
        if (showResult) syncStored()
    }
    LaunchedEffect(currentIndex, question?.id, resultDisplayReady) {
        if (resultDisplayReady) syncStored()
    }
    DisposableEffect(lifecycle, currentIndex, question?.id) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch { syncStored() }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(analysisPair) {
        val p = analysisPair
        if (p != null && p.second != parsingText && !p.second.startsWith(parsingKeyword)) {
            dispatchCommand(SessionCommand.UpdateAnalysis(p.first, p.second))
        }
    }

    LaunchedEffect(sparkPair) {
        val p = sparkPair
        if (p != null && p.second != parsingText && !p.second.startsWith(parsingKeyword)) {
            dispatchCommand(SessionCommand.UpdateSparkAnalysis(p.first, p.second))
        }
    }

    LaunchedEffect(baiduPair) {
        val p = baiduPair
        if (p != null && p.second != parsingText && !p.second.startsWith(parsingKeyword)) {
            dispatchCommand(SessionCommand.UpdateBaiduAnalysis(p.first, p.second))
        }
    }
}
