package com.example.testapp.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import java.time.format.DateTimeFormatter

// 子组件：统计块
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
    score: Int,
    total: Int,
    unanswered: Int,
    quizId: String,
    onBackHome: () -> Unit,
    onViewDetail: () -> Unit = {},
    onBack: () -> Unit = onBackHome
) {
    val viewModel: ResultViewModel = hiltViewModel()
    LaunchedEffect(quizId) { viewModel.load(quizId) }

    // 获取历史记录
    val historyList by viewModel.history.collectAsState(initial = emptyList())
    // 最新一次记录
    val latest = historyList.maxByOrNull { it.time }

    // 当前考试/练习统计：优先使用传入参数，否则回退到最新记录
    val currentScore = if (total > 0) score else latest?.score ?: 0
    val currentTotal = if (total > 0) total else latest?.total ?: 0
    val currentUnanswered = if (total > 0) unanswered else latest?.unanswered ?: 0
    val currentWrong = currentTotal - currentScore - currentUnanswered
    val currentRate = if (currentTotal > 0) currentScore.toFloat() / currentTotal.toFloat() else 0f
    val currentRateText = String.format("%.2f", currentRate * 100)

    // 整张考试/练习统计：基于题库总题数（latest.total）
    val overallTotal = latest?.total ?: 0
    val overallScore = latest?.score ?: 0
    val overallUnanswered = latest?.unanswered ?: 0
    val overallWrong = overallTotal - overallScore - overallUnanswered
    val overallRate = if (overallTotal > 0) overallScore.toFloat() / overallTotal.toFloat() else 0f
    val overallRateText = String.format("%.2f", overallRate * 100)

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val scrollState = rememberScrollState()

    // 模式与文件名
    val (modeText, fileName) = when {
        quizId.startsWith("exam_") -> "考试" to quizId.removePrefix("exam_")
        quizId.startsWith("practice_") -> "练习" to quizId.removePrefix("practice_")
        else -> "练习" to quizId
    }
    val isExam = modeText == "考试"
    val currentLabel = if (isExam) "当前考试：" else "当前练习："
    val overallLabel = if (isExam) "整张考试：" else "整张练习："

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
            if (fileName.isNotBlank()) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = LocalFontSize.current * 1.05f, fontFamily = LocalFontFamily.current),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            // 当前考试卡片
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(currentLabel, style = MaterialTheme.typography.titleSmall.copy(fontSize = LocalFontSize.current * 0.9f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Text("$currentScore / $currentTotal", style = MaterialTheme.typography.displaySmall.copy(fontSize = LocalFontSize.current * 1.2f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(progress = currentRate, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ResultStatBlock("答对", "$currentScore", Color.Green)
                        ResultStatBlock("答错", "$currentWrong", Color.Red)
                        ResultStatBlock("未答", "$currentUnanswered", Color.Yellow)
                        ResultStatBlock("正确率", "$currentRateText%")
                    }
                }
            }

            // 整张考试卡片
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(overallLabel, style = MaterialTheme.typography.titleSmall.copy(fontSize = LocalFontSize.current * 0.9f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Text("$overallScore / $overallTotal", style = MaterialTheme.typography.displaySmall.copy(fontSize = LocalFontSize.current * 1.2f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(progress = overallRate, modifier = Modifier.fillMaxWidth().height(8.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ResultStatBlock("答对", "$overallScore", Color.Green)
                        ResultStatBlock("答错", "$overallWrong", Color.Red)
                        ResultStatBlock("未答", "$overallUnanswered", Color.Yellow)
                        ResultStatBlock("正确率", "$overallRateText%")
                    }
                }
            }

            // 历史成绩走势及列表
            val accuracyList = historyList.map { it.score.toFloat() / it.total.toFloat() }
            if (accuracyList.isNotEmpty()) {
                Text("历史成绩走势", style = MaterialTheme.typography.titleMedium.copy(fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp))
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    val step = if (accuracyList.size > 1) size.width / (accuracyList.size - 1).toFloat() else 0f
                    val points = accuracyList.mapIndexed { idx, v -> Offset(idx * step, size.height - v.coerceIn(0f, 1f) * size.height) }
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 2f)
                    for (i in 0 until points.size - 1) drawLine(primaryColor, points[i], points[i + 1], strokeWidth = 4f)
                    points.forEach { drawCircle(secondaryColor, 6f, it) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                    items(historyList) { h ->
                        val idx = historyList.indexOf(h)
                        val wrong = h.total - h.score - h.unanswered
                        val rate = if (h.total > 0) h.score.toFloat() / h.total.toFloat() else 0f
                        Column(Modifier.padding(vertical = 3.dp)) {
                            Text("${idx + 1}. 正确:${h.score} 错误:${wrong} 正确率:${"%.2f".format(rate)}% 时间:${h.time.format(formatter)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = LocalFontSize.current * 0.98f, fontFamily = LocalFontFamily.current), color = MaterialTheme.colorScheme.onSurface)
                            Divider(Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}
