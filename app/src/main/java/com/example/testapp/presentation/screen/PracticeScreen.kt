package com.example.testapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.presentation.screen.PracticeViewModel
import com.example.testapp.presentation.screen.SettingsViewModel
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import kotlinx.coroutines.launch

@Composable
fun PracticeScreen(
    quizId: String = "default",
    isWrongBookMode: Boolean = false,
    wrongBookFileName: String? = null,
    viewModel: PracticeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onQuizEnd: (score: Int, total: Int) -> Unit = { _, _ -> },
    onSubmit: (Boolean) -> Unit = {}
) {
    LaunchedEffect(quizId, isWrongBookMode, wrongBookFileName) {
        if (isWrongBookMode && wrongBookFileName != null) {
            viewModel.loadWrongQuestions(wrongBookFileName)
        } else {
            viewModel.setProgressId(quizId)
        }
    }
    val questions by viewModel.questions.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val fontStyle by settingsViewModel.fontStyle.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val answeredList by viewModel.answeredList.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val progressLoaded by viewModel.progressLoaded.collectAsState()
    val showResultList by viewModel.showResultList.collectAsState()
    var score by remember { mutableStateOf(0) }
    val question = questions.getOrNull(currentIndex)
    val selectedOption = selectedOptions.getOrNull(currentIndex) ?: -1
    val showResult = showResultList.getOrNull(currentIndex) ?: false

    if (question == null || !progressLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "暂无题目或正在加载进度…",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "第${currentIndex + 1}题 / 共${questions.size}题",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { viewModel.clearProgress() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text(
                    "清除进度",
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
        LinearProgressIndicator(
            progress = if (questions.isNotEmpty()) (currentIndex + 1f) / questions.size else 0f,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        Text(
            text = "第${currentIndex + 1}题：${question.content}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        question.options.forEachIndexed { idx, option ->
            val correctIndex = answerLetterToIndex(question.answer)
            val isCorrect = showResult && correctIndex != null && idx == correctIndex
            val isSelected = selectedOption == idx
            val isWrong = showResult && isSelected && !isCorrect
            val backgroundColor = when {
                isCorrect -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                isWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(backgroundColor)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { viewModel.answerQuestion(idx) },
                    enabled = !showResult
                )
                Text(option, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row {
            if (currentIndex > 0) {
                Button(onClick = {
                    viewModel.updateShowResult(currentIndex, showResult)
                    viewModel.prevQuestion()
                }) {
                    Text(
                        "上一题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            if (currentIndex < questions.size - 1) {
                Button(onClick = {
                    viewModel.updateShowResult(currentIndex, showResult)
                    viewModel.nextQuestion()
                }) {
                    Text(
                        "下一题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            } else {
                Button(onClick = {
                    onQuizEnd(score, questions.size)
                }) {
                    Text(
                        "交卷",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        val wrongBookViewModel: WrongBookViewModel = hiltViewModel()
        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
                viewModel.updateShowResult(currentIndex, true)
                val correctIndex = answerLetterToIndex(question.answer)
                val correct = selectedOption == correctIndex
                if (!correct && selectedOption != -1) {
                    coroutineScope.launch {
                        try {
                            wrongBookViewModel.addWrongQuestion(
                                com.example.testapp.domain.model.WrongQuestion(question, selectedOption)
                            )
                            android.util.Log.d("PracticeScreen", "保存错题: ${question.content}, 选项: $selectedOption, fileName: ${question.fileName}")
                        } catch (e: Exception) {
                            android.util.Log.e("PracticeScreen", "保存��题失败: ${e.message}")
                        }
                    }
                }
                if (correct) score++
                onSubmit(correct)
            },
            enabled = selectedOption != -1 && !showResult,
        ) {
            Text(
                "提交答案",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
        if (showResult) {
            val correctIndex = answerLetterToIndex(question.answer)
            val correct = selectedOption == correctIndex
            Text(
                if (correct) "回答正确！" else "回答错误，正确答案：${if (correctIndex != null && correctIndex in question.options.indices) question.options[correctIndex] else question.answer}",
                color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            // 新增：显示 fileName 字段
            if (question.fileName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "题目来源文件：${question.fileName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// 工具函数：将字母答案转为索引
private fun answerLetterToIndex(answer: String): Int? {
    return answer.trim().uppercase().firstOrNull()?.let { it - 'A' }
}
