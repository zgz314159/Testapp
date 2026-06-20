package com.example.testapp.presentation.screen.result

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import java.time.format.DateTimeFormatter

// 辅助统计块
@Composable
private fun ResultStatBlock(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = LocalFontFamily.current
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = LocalFontFamily.current
            ),
            color = valueColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    score: Int,                        // 本次 session 答对数
    total: Int,                        // 本次 session 已答数
    unanswered: Int,                   // 剩余未答数
    quizId: String,
    cumulativeCorrect: Int? = null,    // 可选：真实累计答对数
    cumulativeAnswered: Int? = null,   // 可选：真实累计已答数
    cumulativeExamCount: Int? = null,  // 可选：真实累计考试次数
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

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val scrollState = rememberScrollState()

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("返回首页", fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current)
                    }
                    Button(
                        onClick = onViewDetail,
                        enabled = quizId.isNotBlank(),
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("答题详情", fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current)
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
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (stats.fileName.isNotBlank()) {
                Text(
                    text = stats.fileName,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = LocalFontSize.current * 1.05f, fontFamily = LocalFontFamily.current),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            // 当前练习/考试卡片
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stats.currentLabel, style = MaterialTheme.typography.titleSmall.copy(fontSize = LocalFontSize.current * 0.9f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Text("${stats.currentScore} / ${stats.currentActualAnswered}", style = MaterialTheme.typography.displaySmall.copy(fontSize = LocalFontSize.current * 1.2f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { stats.currentRate.toFloat() }, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ResultStatBlock("答对", "${stats.currentScore}", Color.Green)
                        ResultStatBlock("答错", "${stats.currentWrong}", Color.Red)
                        ResultStatBlock("未答", "${stats.currentUnanswered}", Color.Yellow)
                        ResultStatBlock("正确率", "${stats.currentRateText}%")
                    }
                }
            }

            // 累计统计卡片
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stats.overallLabel, style = MaterialTheme.typography.titleSmall.copy(fontSize = LocalFontSize.current * 0.9f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Text("${stats.overallScore} / ${stats.displayOverallTotal}", style = MaterialTheme.typography.displaySmall.copy(fontSize = LocalFontSize.current * 1.2f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (stats.displayOverallTotal > 0) {
                                stats.overallScore.toFloat() / stats.displayOverallTotal.toFloat()
                            } else {
                                0f
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        if (stats.isExamMode) {
                            ResultStatBlock("累计答对", "${stats.overallScore}", Color.Green)
                            ResultStatBlock("累计答错", "${stats.overallWrong}", Color.Red)
                            ResultStatBlock("累计考试次数", "${stats.actualExamCount}", MaterialTheme.colorScheme.primary)
                            ResultStatBlock(
                                "平均正确率",
                                "${String.format("%.1f", if (stats.overallAnswered > 0) stats.overallScore.toFloat() / stats.overallAnswered * 100 else 0f)}%"
                            )
                        } else {
                            ResultStatBlock("累计答对", "${stats.overallScore}", Color.Green)
                            ResultStatBlock("累计答错", "${stats.overallWrong}", Color.Red)
                            ResultStatBlock("累计次数", "${stats.sameFileHistory.size}", MaterialTheme.colorScheme.primary)
                            ResultStatBlock("累计正确率", "${stats.overallRateText}%")
                        }
                    }
                }
            }

            // 历史成绩折线列表
            if (stats.accuracyList.isNotEmpty()) {
                Text("历史成绩走势", style = MaterialTheme.typography.titleMedium.copy(fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    val step = if (stats.accuracyList.size > 1) size.width / (stats.accuracyList.size - 1).toFloat() else 0f
                    val points = stats.accuracyList.mapIndexed { idx, v -> Offset(idx * step, size.height - v.coerceIn(0f, 1f) * size.height) }
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 2f)
                    for (i in 0 until points.size - 1) drawLine(primaryColor, points[i], points[i + 1], strokeWidth = 4f)
                    points.forEach { drawCircle(secondaryColor, 6f, it) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    itemsIndexed(historyList) { idx, h ->
                        val wrong = h.total - h.score - h.unanswered
                        val rate = if (h.total > 0) h.score.toFloat() / h.total.toFloat() else 0f
                        Column(Modifier.padding(vertical = 3.dp)) {
                            Text("${idx + 1}. 正确:${h.score} 错误:${wrong} 正确率:${"%.2f".format(rate)}% 时间:${h.time.format(formatter)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.onSurface)
                            HorizontalDivider(Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

