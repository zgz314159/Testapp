package com.example.testapp.presentation.screen.result

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.uicommon.design.AppCard
import com.example.testapp.uicommon.design.AppSpacing
import com.example.testapp.uicommon.design.AppTopBar

@Composable
private fun ResultStatBlock(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor
        )
    }
}

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
    val statColors = resultStatPalette()

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

            AppCard(modifier = Modifier.padding(bottom = AppSpacing.sm)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stats.currentLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${stats.currentScore} / ${stats.currentActualAnswered}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    LinearProgressIndicator(
                        progress = { stats.currentRate.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(AppSpacing.sm)
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ResultStatBlock("答对", "${stats.currentScore}", statColors.correct)
                        ResultStatBlock("答错", "${stats.currentWrong}", statColors.wrong)
                        ResultStatBlock("未答", "${stats.currentUnanswered}", statColors.unanswered)
                        ResultStatBlock("正确率", "${stats.currentRateText}%")
                    }
                }
            }

            AppCard(modifier = Modifier.padding(bottom = AppSpacing.sm)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(AppSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stats.overallLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${stats.overallScore} / ${stats.displayOverallTotal}",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    LinearProgressIndicator(
                        progress = {
                            if (stats.displayOverallTotal > 0) {
                                stats.overallScore.toFloat() / stats.displayOverallTotal.toFloat()
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(AppSpacing.sm)
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        if (stats.isExamMode) {
                            ResultStatBlock("累计答对", "${stats.overallScore}", statColors.correct)
                            ResultStatBlock("累计答错", "${stats.overallWrong}", statColors.wrong)
                            ResultStatBlock("累计考试次数", "${stats.actualExamCount}", MaterialTheme.colorScheme.primary)
                            ResultStatBlock(
                                "平均正确率",
                                "${String.format("%.1f", if (stats.overallAnswered > 0) stats.overallScore.toFloat() / stats.overallAnswered * 100 else 0f)}%"
                            )
                        } else {
                            ResultStatBlock("累计答对", "${stats.overallScore}", statColors.correct)
                            ResultStatBlock("累计答错", "${stats.overallWrong}", statColors.wrong)
                            ResultStatBlock("累计次数", "${stats.sameFileHistory.size}", MaterialTheme.colorScheme.primary)
                            ResultStatBlock("累计正确率", "${stats.overallRateText}%")
                        }
                    }
                }
            }

            if (stats.accuracyList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                Text(
                    "历史成绩走势",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = AppSpacing.xs)
                )
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    val step = if (stats.accuracyList.size > 1) size.width / (stats.accuracyList.size - 1).toFloat() else 0f
                    val points = stats.accuracyList.mapIndexed { idx, v ->
                        Offset(idx * step, size.height - v.coerceIn(0f, 1f) * size.height)
                    }
                    drawLine(statColors.chartAxis, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
                    drawLine(statColors.chartAxis, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 2f)
                    for (i in 0 until points.size - 1) drawLine(primaryColor, points[i], points[i + 1], strokeWidth = 4f)
                    points.forEach { drawCircle(secondaryColor, 6f, it) }
                }
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                TextButton(
                    onClick = { showHistorySheet = true },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("查看历史记录 (${historyList.size})")
                }
            }
        }
    }

    ResultHistorySheet(
        visible = showHistorySheet,
        historyList = historyList,
        onDismiss = { showHistorySheet = false }
    )
}
