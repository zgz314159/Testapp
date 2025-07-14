package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import androidx.hilt.navigation.compose.hiltViewModel

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
    val wrongCount = total - score
    val accuracyRate = if (total > 0) score.toFloat() / total else 0f
    val accuracyText = String.format("%.2f", accuracyRate * 100)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "答题结束！",
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
                        text = "$score / $total",
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
                                text = "$score",
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
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                ) {
                    val step = if (accuracyList.size > 1) size.width / (accuracyList.size - 1) else 0f
                    val points = accuracyList.mapIndexed { idx, v ->
                        Offset(idx * step, size.height - v.coerceIn(0f,1f) * size.height)
                    }
                    for (i in 0 until points.size - 1) {
                        drawLine(Color.Blue, points[i], points[i + 1], strokeWidth = 4f)
                    }
                    points.forEach { drawCircle(Color.Red, 6f, it) }
                }

                Spacer(modifier = Modifier.height(16.dp))
                historyList.value.forEachIndexed { idx, h ->
                    val wrong = h.total - h.score
                    val rate = if (h.total > 0) h.score * 100f / h.total else 0f
                    Text(
                        text = "${idx + 1}. 正确:${h.score} 错误:${wrong} 正确率:${"%.2f".format(rate)}%",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "返回首页",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
                val detailEnabled = quizId.isNotBlank()
                Button(
                    onClick = onViewDetail,
                    modifier = Modifier.weight(1f),
                    enabled = detailEnabled
                ) {
                    Text(
                        "答题详情",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }
    }
}
