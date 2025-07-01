package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize

@Composable
fun WrongBookPracticeScreen(viewModel: WrongBookViewModel = hiltViewModel()) {
    val wrongList = viewModel.wrongQuestions.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }
    val question = wrongList.value.getOrNull(currentIndex)?.question
    if (question == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "暂无错题可重练",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
        return
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "错题重练 第${currentIndex + 1}题 / 共${wrongList.value.size}题",
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = question.content, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
        Spacer(modifier = Modifier.height(16.dp))
        question.options.forEachIndexed { idx, option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedOption == idx,
                    onClick = { selectedOption = idx }
                )
                Text(option, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { showResult = true },
            enabled = selectedOption != -1 && !showResult
        ) {
            Text(
                "提交答案",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
        if (showResult) {
            val correct = selectedOption == question.answer.toInt()
            Text(
                if (correct) "回答正确！" else "回答错误，正确答案：${question.options[question.answer.toInt()]}",
                color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                showResult = false
                selectedOption = -1
                if (currentIndex < wrongList.value.size - 1) currentIndex++
            }, enabled = currentIndex < wrongList.value.size - 1) {
                Text(
                    "下一题",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
    }
}
