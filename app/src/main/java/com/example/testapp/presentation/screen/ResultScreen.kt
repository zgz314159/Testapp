package com.example.testapp.presentation.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    androidx.compose.runtime.LaunchedEffect(quizId) {
        viewModel.load(quizId)
    }
    val historyList = viewModel.history.collectAsState()
    val latest = historyList.value.lastOrNull()
    val displayScore = if (score == 0 && total == 0) latest?.score ?: 0 else score
    val displayTotal = if (score == 0 && total == 0) latest?.total ?: 0 else total
    val wrongCount = displayTotal - displayScore
    val accuracyRate = if (displayTotal > 0) displayScore.toFloat() / displayTotal else 0f
    val accuracyText = String.format("%.2f", accuracyRate * 100)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val (modeText, fileName) = when {
        quizId.startsWith("exam_") -> "考试" to quizId.removePrefix("exam_")
        quizId.startsWith("practice_") -> "练习" to quizId.removePrefix("practice_")
        else -> "练习" to quizId
    }


    Box(modifier = Modifier.fillMaxSize()) {
        // 主内容区
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            if (fileName.isNotBlank()) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = "${modeText}结束！",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$displayScore / $displayTotal",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = LocalFontSize.current * 1.2f,
                            fontFamily = LocalFontFamily.current
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = accuracyRate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "答对",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                            Text(
                                text = "$displayScore",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "答错",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                            Text(
                                text = "$wrongCount",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "正确率",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                            Text(
                                text = "$accuracyText%",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            val accuracyList = historyList.value.map { it.score.toFloat() / it.total }

            if (accuracyList.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val step = if (accuracyList.size > 1) size.width / (accuracyList.size - 1) else 0f
                    val points = accuracyList.mapIndexed { idx, v ->
                        Offset(idx * step, size.height - v.coerceIn(0f, 1f) * size.height)
                    }
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)
                    drawLine(Color.DarkGray, Offset(0f, size.height), Offset(0f, 0f), strokeWidth = 2f)
                    for (i in 0 until points.size - 1) {
                        drawLine(Color.Blue, points[i], points[i + 1], strokeWidth = 4f)
                    }
                    points.forEach { drawCircle(Color.Red, 6f, it) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // LazyColumn无高度限制，自适应内容多少
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(historyList.value) { h ->
                        val idx = historyList.value.indexOf(h)
                        val wrong = h.total - h.score
                        val rate = if (h.total > 0) h.score * 100f / h.total else 0f
                        Text(
                            text = "${idx + 1}. 正确:${h.score} 错误:${wrong} 正确率:${"%.2f".format(rate)}% 时间:${h.time.format(formatter)}",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 底部按钮区（只占实际高度，永远贴底）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Text(
                    "返回首页",
                    fontSize = LocalFontSize.current * 0.95f,
                    fontFamily = LocalFontFamily.current
                )
            }
            val detailEnabled = quizId.isNotBlank()
            Button(
                onClick = onViewDetail,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                enabled = detailEnabled
            ) {
                Text(
                    "答题详情",
                    fontSize = LocalFontSize.current * 0.95f,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
    }
}
