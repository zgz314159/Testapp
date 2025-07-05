package com.example.testapp.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.screen.FavoriteViewModel

@Composable
fun ExamScreen(
    quizId: String,
    viewModel: ExamViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onExamEnd: (score: Int, total: Int) -> Unit = { _, _ -> }
) {
    val examCount by settingsViewModel.examQuestionCount.collectAsState()
    LaunchedEffect(quizId, examCount) { viewModel.loadQuestions(quizId, examCount) }
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedOptions by viewModel.selectedOptions.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val question = questions.getOrNull(currentIndex)
    val coroutineScope = rememberCoroutineScope()
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    val favoriteQuestions by favoriteViewModel.favoriteQuestions.collectAsState()
    val isFavorite = remember(question, favoriteQuestions) {
        question != null && favoriteQuestions.any { it.question.id == question.id }
    }
    if (question == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "暂无题目...",
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = {
                if (isFavorite) {
                    favoriteViewModel.removeFavorite(question.id)
                } else {
                    favoriteViewModel.addFavorite(question)
                }
            }) {
                Text(
                    if (isFavorite) "取消收藏" else "收藏",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        question.options.forEachIndexed { idx, option ->
            val isSelected = selectedOptions.getOrElse(currentIndex) { -1 } == idx
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { viewModel.selectOption(idx) }
                )
                Text(option, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row {
            if (currentIndex > 0) {
                Button(onClick = { viewModel.prevQuestion() }) {
                    Text(
                        "上一题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            if (currentIndex < questions.size - 1) {
                Button(onClick = { viewModel.nextQuestion() }) {
                    Text(
                        "下一题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (currentIndex >= 1) {
            Button(onClick = {
                coroutineScope.launch {
                    val score = viewModel.gradeExam()
                    onExamEnd(score, questions.size)
                }
            }) {
                Text(
                    "交卷",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            }
        }
    }
}