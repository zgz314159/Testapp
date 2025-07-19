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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import java.time.format.DateTimeFormatter

// 1. 先写Composabe子组件
@Composable
private fun ResultStatBlock(label: String, value: String) {
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
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// 2. 主界面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    quizId: String,
    onBackHome: () -> Unit,
    onViewDetail: () -> Unit = {},
    onBack: () -> Unit = onBackHome
) {
    val viewModel: ResultViewModel = hiltViewModel()
    LaunchedEffect(quizId) {
        viewModel.load(quizId)
    }
    val historyList by viewModel.history.collectAsState()
    val latest = historyList.lastOrNull()
    val displayScore = if (score == 0 && total == 0) latest?.score ?: 0 else score
    val displayTotal = if (score == 0 && total == 0) latest?.total ?: 0 else total
    val wrongCount = displayTotal - displayScore
    val accuracyRate = if (displayTotal > 0) displayScore.toFloat() / displayTotal else 0f
    val accuracyText = String.format("%.2f", accuracyRate * 100)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val scrollState = rememberScrollState()
    val (modeText, fileName) = when {
        quizId.startsWith("exam_") -> "考试" to quizId.removePrefix("exam_")
        quizId.startsWith("practice_") -> "练习" to quizId.removePrefix("practice_")
        else -> "练习" to quizId
    }

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
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "返回首页",
                            fontSize = LocalFontSize.current * 0.98f,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                    Button(
                        onClick = onViewDetail,
                        enabled = quizId.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "答题详情",
                            fontSize = LocalFontSize.current * 0.98f,
                            fontFamily = LocalFontFamily.current
                        )
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
            // 文件名与模式
            if (fileName.isNotBlank()) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = LocalFontSize.current * 1.05f,
                        fontFamily = LocalFontFamily.current
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = "${modeText}已完成",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = LocalFontSize.current * 1.05f,
                    fontFamily = LocalFontFamily.current
                ),
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // 统计卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$displayScore / $displayTotal",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = LocalFontSize.current * 1.2f,
                            fontFamily = LocalFontFamily.current
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = accuracyRate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultStatBlock(label = "答对", value = "$displayScore")
                        ResultStatBlock(label = "答错", value = "$wrongCount")
                        ResultStatBlock(label = "正确率", value = "$accuracyText%")
                    }
                }
            }


            // ===== 整个题库的答题情况 =====
            val allHistoryList by viewModel.allHistory.collectAsState()
            val allLatest = allHistoryList.lastOrNull()
            if (allLatest != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "全部题库",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = LocalFontSize.current * 1.05f,
                        fontFamily = LocalFontFamily.current
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "整体答题情况",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = LocalFontSize.current * 1.05f,
                        fontFamily = LocalFontFamily.current
                    ),
                    modifier = Modifier.padding(bottom = 18.dp)
                )

                val gScore = allLatest.score
                val gTotal = allLatest.total
                val gWrong = gTotal - gScore
                val gRate = if (gTotal > 0) gScore.toFloat() / gTotal else 0f
                val gRateText = String.format("%.2f", gRate * 100)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$gScore / $gTotal",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = LocalFontSize.current * 1.2f,
                                fontFamily = LocalFontFamily.current
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = gRate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ResultStatBlock(label = "答对", value = "$gScore")
                            ResultStatBlock(label = "答错", value = "$gWrong")
                            ResultStatBlock(label = "正确率", value = "$gRateText%")
                        }
                    }
                }

                val allAccList = allHistoryList.map { it.score.toFloat() / it.total }
                if (allAccList.isNotEmpty()) {
                    Text(
                        "题库整体成绩走势",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = LocalFontSize.current * 0.98f,
                            fontFamily = LocalFontFamily.current
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 6.dp)
                    )
                    val primary = MaterialTheme.colorScheme.primary
                    val secondary = MaterialTheme.colorScheme.secondary

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        val step = if (allAccList.size > 1) size.width / (allAccList.size - 1) else 0f
                        val points = allAccList.mapIndexed { idx, v ->
                            Offset(idx * step, size.height - v.coerceIn(0f, 1f) * size.height)
                        }
                        drawLine(Color.DarkGray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
                        drawLine(Color.DarkGray, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 2f)
                        for (i in 0 until points.size - 1) {
                            drawLine(primary, points[i], points[i + 1], strokeWidth = 4f)
                        }
                        points.forEach { drawCircle(secondary, 6f, it) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(allHistoryList) { h ->
                            val idx = allHistoryList.indexOf(h)
                            val wrong = h.total - h.score
                            val rate = if (h.total > 0) h.score * 100f / h.total else 0f
                            Column(Modifier.padding(vertical = 3.dp)) {
                                Text(
                                    text = "${idx + 1}. 正确:${h.score} 错误:${wrong} 正确率:${"%.2f".format(rate)}% 时间:${h.time.format(formatter)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = LocalFontSize.current * 0.98f,
                                        fontFamily = LocalFontFamily.current
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Divider(Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
            // 历史趋势线和记录
            val accuracyList = historyList.map { it.score.toFloat() / it.total }
            if (accuracyList.isNotEmpty()) {
                // 小标题
                Text(
                    "历史成绩走势",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = LocalFontSize.current * 0.98f,
                        fontFamily = LocalFontFamily.current
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 6.dp)
                )
                // 折线趋势
                // 在 Canvas 外部获取主题色
                val primary = MaterialTheme.colorScheme.primary
                val secondary = MaterialTheme.colorScheme.secondary

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    val step = if (accuracyList.size > 1) size.width / (accuracyList.size - 1) else 0f
                    val points = accuracyList.mapIndexed { idx, v ->
                        Offset(idx * step, size.height - v.coerceIn(0f, 1f) * size.height)
                    }
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 2f)
                    for (i in 0 until points.size - 1) {
                        drawLine(primary, points[i], points[i + 1], strokeWidth = 4f)
                    }
                    points.forEach { drawCircle(secondary, 6f, it) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 历史记录列表（适配内容高，带分割线）
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(historyList) { h ->
                        val idx = historyList.indexOf(h)
                        val wrong = h.total - h.score
                        val rate = if (h.total > 0) h.score * 100f / h.total else 0f
                        Column(Modifier.padding(vertical = 3.dp)) {
                            Text(
                                text = "${idx + 1}. 正确:${h.score} 错误:${wrong} 正确率:${"%.2f".format(rate)}% 时间:${h.time.format(formatter)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = LocalFontSize.current * 0.98f,
                                    fontFamily = LocalFontFamily.current
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Divider(Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}
