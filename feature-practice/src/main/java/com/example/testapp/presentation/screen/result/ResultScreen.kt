package com.example.testapp.presentation.screen.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.result.components.ResultAccuracyChartSection
import com.example.testapp.presentation.screen.result.components.ResultBottomActionBar
import com.example.testapp.presentation.screen.result.components.ResultCurrentScoreCard
import com.example.testapp.presentation.screen.result.components.ResultDashboardColors
import com.example.testapp.presentation.screen.result.components.ResultOverallScoreCard
import com.example.testapp.presentation.screen.result.components.ResultQuestionBankHeader
import com.example.testapp.presentation.screen.result.components.ResultTopBar

@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    unanswered: Int,
    quizId: String,
    cumulativeCorrect: Int? = null,
    cumulativeAnswered: Int? = null,
    cumulativeExamCount: Int? = null,
    detailEnabled: Boolean = true,
    onBackHome: () -> Unit,
    onViewDetail: () -> Unit = {},
    onBack: () -> Unit = onBackHome,
) {
    val viewModel: ResultViewModel = hiltViewModel()
    LaunchedEffect(quizId) { viewModel.load(quizId) }

    val historyList by viewModel.history.collectAsState()
    val totalQuestions by viewModel.totalQuestions.collectAsState()
    val stats = remember(
        quizId,
        score,
        total,
        unanswered,
        cumulativeCorrect,
        cumulativeAnswered,
        cumulativeExamCount,
        historyList,
        totalQuestions,
    ) {
        buildResultDisplayStats(
            quizId,
            score,
            total,
            unanswered,
            cumulativeCorrect,
            cumulativeAnswered,
            cumulativeExamCount,
            historyList,
            totalQuestions,
        )
    }
    var showHistorySheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ResultDashboardColors.PageBackground,
        topBar = { ResultTopBar(onBack = onBack) },
        bottomBar = {
            ResultBottomActionBar(
                onBackHome = onBackHome,
                onViewDetail = onViewDetail,
                detailEnabled = detailEnabled && quizId.isNotBlank(),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "questionBank") {
                ResultQuestionBankHeader(stats.fileName)
            }
            item(key = "current") {
                ResultCurrentScoreCard(stats)
            }
            item(key = "overall") {
                ResultOverallScoreCard(stats)
            }
            item(key = "history") {
                ResultAccuracyChartSection(
                    accuracyList = stats.accuracyList,
                    historyCount = stats.sameFileHistory.size,
                    historyRecords = stats.sameFileHistory,
                    onShowHistory = { showHistorySheet = true },
                )
            }
        }
    }

    ResultHistorySheet(
        visible = showHistorySheet,
        historyList = stats.sameFileHistory,
        onDismiss = { showHistorySheet = false },
    )
}
