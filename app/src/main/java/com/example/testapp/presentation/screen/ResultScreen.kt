package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily

@Composable
fun ResultScreen(score: Int, total: Int, onBackHome: () -> Unit, onViewWrongBook: () -> Unit = {}, onViewHistory: () -> Unit = {}) {
    val wrongCount = total - score
    val accuracy = if (total > 0) String.format("%.2f", score * 100f / total) else "0"
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "答题结束！", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "你的得分：$score / $total", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "答对题数：$score", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "答错题数：$wrongCount", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "答对率：$accuracy%", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onBackHome) {
                Text(
                    "返回首页",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onViewWrongBook) {
                Text(
                    "查看错题本",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onViewHistory) {
                Text(
                    "查看历史记录",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
    }
}
