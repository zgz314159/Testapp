package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize

@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    onBackHome: () -> Unit,
    onViewWrongBook: () -> Unit = {},
    onViewHistory: () -> Unit = {}
) {
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

            Button(
                onClick = onBackHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "返回首页",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewWrongBook,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "查看错题本",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
                Button(
                    onClick = onViewHistory,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "查看历史记录",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }
    }
}
