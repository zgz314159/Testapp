package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontSize
import com.example.testapp.presentation.component.LocalFontFamily
import androidx.navigation.NavController

@Composable
fun WrongBookScreen(
    fileName: String? = null,
    viewModel: WrongBookViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val wrongList = viewModel.wrongQuestions.collectAsState()
    val fileNames = viewModel.fileNames.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "错题本",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = LocalFontSize.current,
            fontFamily = LocalFontFamily.current
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (fileName.isNullOrEmpty()) {
            // 显示错题按文件分类
            if (fileNames.value.isEmpty()) {
                Text(
                    "暂无错题",
                    fontSize = LocalFontSize.current,
                    fontFamily = LocalFontFamily.current
                )
            } else {
                fileNames.value.forEach { name ->
                    val count = wrongList.value.count { it.question.fileName == name }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            "$name ($count)",
                            modifier = Modifier.weight(1f),
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                        Button(onClick = {
                            navController?.navigate("practice_wrongbook/$name")
                        }) {
                            Text(
                                "练习",
                                fontSize = LocalFontSize.current,
                                fontFamily = LocalFontFamily.current
                            )
                        }
                    }
                }
            }} else {
                val filteredList = wrongList.value.filter { it.question.fileName == fileName }
                if (filteredList.isEmpty()) {
                    Text(
                        "暂无错题",
                        fontSize = LocalFontSize.current,
                        fontFamily = LocalFontFamily.current
                    )
                } else {
                    filteredList.forEachIndexed { idx, wrong ->
                        Text(
                            "${idx + 1}. ${wrong.question.content} (你的答案：${wrong.question.options[wrong.selected]})",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            navController?.navigate("practice_wrongbook/$fileName")
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            "重练错题",
                            fontSize = LocalFontSize.current,
                            fontFamily = LocalFontFamily.current
                        )
                    }
                }
            }
        }
    }

