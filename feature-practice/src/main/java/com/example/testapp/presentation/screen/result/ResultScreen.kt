package com.example.testapp.presentation.screen.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.result.components.ResultAccuracyChartSection
import com.example.testapp.presentation.screen.result.components.ResultCurrentScoreCard
import com.example.testapp.presentation.screen.result.components.ResultOverallScoreCard
import com.example.testapp.presentation.screen.result.components.ResultStatColors
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.AppTopBar
import com.example.testapp.uicommon.screen.result.resultStatPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    unanswered: Int,
    quizId: String,
    cumulativeCorrect: Int? = null,
    cumulativeAnswered: Int? = null,
    cumulativeExamCount: Int? = null,
    onBackHome: () -> Unit,
    onViewDetail: () -> Unit = {},
    onBack: () -> Unit = onBackHome
) {
    val viewModel: ResultViewModel = hiltViewModel()
    LaunchedEffect(quizId) { viewModel.load(quizId) }

    val historyList by viewModel.history.collectAsState()
    val totalQuestions by viewModel.totalQuestions.collectAsState()
    val stats = remember(
        quizId, score, total, unanswered,
        cumulativeCorrect, cumulativeAnswered, cumulativeExamCount,
        historyList, totalQuestions
    ) {
        buildResultDisplayStats(
            quizId, score, total, unanswered,
            cumulativeCorrect, cumulativeAnswered, cumulativeExamCount,
            historyList, totalQuestions
        )
    }

    val scrollState = rememberScrollState()
    var showHistorySheet by remember { mutableStateOf(false) }
    val palette = resultStatPalette()
    val statColors = ResultStatColors(
        correct = palette.correct,
        wrong = palette.wrong,
        unanswered = palette.unanswered,
        chartAxis = palette.chartAxis,
    )

    Scaffold(
        topBar = {
            AppTopBar(title = "练习结果", onBack = onBack)
        },
        bottomBar = {
            Surface(shadowElevation = AppSpacing.sm) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("返回首页")
                    }
                    Button(
                        onClick = onViewDetail,
                        enabled = quizId.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("答题详情")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (stats.fileName.isNotBlank()) {
                Text(
                    text = stats.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = AppSpacing.xs)
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.lg))

            ResultCurrentScoreCard(stats = stats, statColors = statColors)
            ResultOverallScoreCard(stats = stats, statColors = statColors)

            ResultAccuracyChartSection(
                accuracyList = stats.accuracyList,
                historyCount = historyList.size,
                statColors = statColors,
                onShowHistory = { showHistorySheet = true },
            )
        }
    }

    ResultHistorySheet(
        visible = showHistorySheet,
        historyList = historyList,
        onDismiss = { showHistorySheet = false }
    )
}
