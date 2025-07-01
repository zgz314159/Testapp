package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.screen.PracticeViewModel

@Composable
fun QuestionScreen(
    quizId: String,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    // 初始化时设置 quizId，加载对应题库
    LaunchedEffect(quizId) {
        viewModel.setProgressId(quizId)
    }
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    // 判断是否为题库详情模式（question_detail 路由进入）
    // 这里假设 quizId 不是 "favorite" 或其它特殊模式时，均为详情模式
    // 你可根据实际业务调整判断条件
    val isDetailMode = true // 题库详情模式

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isDetailMode) {
            Text("题库详情（共${questions.size}题）", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(questions.size) { idx ->
                    val q = questions[idx]
                    val contentState = remember(q.content) { mutableStateOf(q.content) }
                    val optionsState = remember(q.options) { mutableStateOf(q.options?.toMutableList() ?: mutableListOf()) }
                    val answerState = remember(q.answer) { mutableStateOf(q.answer ?: "") }
                    val explanationState = remember(q.explanation) { mutableStateOf(q.explanation ?: "") }
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        OutlinedTextField(
                            value = contentState.value,
                            onValueChange = { contentState.value = it },
                            label = { Text("题目内容") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // 可编辑选项
                        optionsState.value.forEachIndexed { optIdx, opt ->
                            OutlinedTextField(
                                value = opt,
                                onValueChange = { newOpt ->
                                    val newList = optionsState.value.toMutableList()
                                    if (optIdx < newList.size) newList[optIdx] = newOpt
                                    optionsState.value = newList
                                },
                                label = { Text("选项${'A' + optIdx}") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                            )
                        }
                        // 添加/删除选项按钮
                        Row(modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp)) {
                            Button(onClick = {
                                val newList = optionsState.value.toMutableList()
                                newList.add("")
                                optionsState.value = newList
                            }) {
                                Text(
                                    "添加选项",
                                    fontSize = LocalFontSize.current,
                                    fontFamily = LocalFontFamily.current
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (optionsState.value.size > 1) {
                                Button(onClick = {
                                    optionsState.value = optionsState.value.dropLast(1).toMutableList()
                                }) {
                                    Text(
                                        "删除选项",
                                        fontSize = LocalFontSize.current,
                                        fontFamily = LocalFontFamily.current
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = answerState.value,
                            onValueChange = { answerState.value = it },
                            label = { Text("答案") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = explanationState.value,
                            onValueChange = { explanationState.value = it },
                            label = { Text("解析") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = {
                            val changed = contentState.value != q.content ||
                                    optionsState.value.toList() != (q.options ?: emptyList<String>()) ||
                                    answerState.value != (q.answer ?: "") ||
                                    explanationState.value != (q.explanation ?: "")
                            if (changed) {
                                viewModel.updateQuestionAllFields(
                                    idx,
                                    contentState.value,
                                    optionsState.value.toList(),
                                    answerState.value,
                                    explanationState.value
                                )
                            }
                        }) {
                            Text(
                                "保存修改",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        } else {
            Text("题目：${currentIndex + 1}", fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            if (questions.isNotEmpty()) {
                Text(questions[currentIndex].content, fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current)
            } else {
                Text("暂无题目", fontSize = LocalFontSize.current)
            }
            // ...原有的上一题/下一题/提交等按钮...
        }
    }
}
