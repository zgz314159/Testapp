package com.example.testapp.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.ActionModeTextToolbar
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import kotlinx.coroutines.launch

@Composable
fun SparkAskScreen(
    text: String,
    questionId: Int,
    index: Int,
    navController: NavController? = null,
    onSave: (String) -> Unit = {},
    viewModel: SparkAskViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()
    val globalFontSize by settingsViewModel.fontSize.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storedSize by FontSettingsDataStore
        .getSparkFontSize(context, Float.NaN)
        .collectAsState(initial = Float.NaN)
    var screenFontSize by remember { mutableStateOf(globalFontSize) }
    var fontLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(storedSize) {
        if (!storedSize.isNaN()) {
            screenFontSize = storedSize
            fontLoaded = true
        }
    }
    LaunchedEffect(screenFontSize, fontLoaded) {
        if (fontLoaded) {
            FontSettingsDataStore.setSparkFontSize(context, screenFontSize)
        }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    val questionState = remember { mutableStateOf(TextFieldValue(text)) }
    var question by questionState
    val answerState = remember { mutableStateOf(TextFieldValue("")) }
    var answer by answerState
    var originalAnswer by remember { mutableStateOf("") }
    val view = LocalView.current
    val toolbar = remember(view, navController) {
        ActionModeTextToolbar(
            view = view,
            onAIQuestion = {
                val sel = answerState.value.selection
                val selected = if (sel.min < sel.max) answerState.value.text.substring(sel.min, sel.max) else ""
                if (selected.isNotBlank()) {
                    val encoded = com.example.testapp.util.safeEncode(selected)
                    navController?.navigate("spark_ask/$questionId/$index/$encoded")
                }
            },
            aiServiceName = "Spark"
        )
    }

    LaunchedEffect(Unit) {
        viewModel.reset()
        answer = TextFieldValue("")
        originalAnswer = ""
        val saved = viewModel.getSavedNote(questionId)
        if (!saved.isNullOrBlank()) {
            answer = TextFieldValue(saved)
            originalAnswer = saved
        }
    }

    LaunchedEffect(result) {
        if (result.isNotBlank() && result != "解析中..." && !result.contains("解析失败")) {
            answer = TextFieldValue(result)
            originalAnswer = result
            // 移除自动保存，由用户手动控制保存
            android.util.Log.d("SparkAskScreen", "Received successful result: ${result.take(50)}...")
        } else if (result.isNotBlank() && result != "解析中...") {
            // 仅更新UI，不保存失败结果
            answer = TextFieldValue(result)
            originalAnswer = result
        }
    }

    var showSaveDialog by remember { mutableStateOf(false) }

    BackHandler {
        // 如果内容不为空，弹出保存确认弹窗
        if (answer.text.isNotBlank() && 
            !answer.text.contains("解析中") && 
            !answer.text.contains("解析失败")) {
            showSaveDialog = true
        } else {
            navController?.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = (LocalFontSize.current.value + 2).sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = LocalFontFamily.current
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))
            CompositionLocalProvider(LocalTextToolbar provides toolbar) {
                BasicTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        fontSize = screenFontSize.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = LocalFontFamily.current
                    ),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    answer = TextFieldValue("解析中...")
                    viewModel.ask(question.text)
                },
                enabled = answer.text != "解析中..."
            ) {
                Text(if (answer.text.isBlank() || answer.text == "解析中...") "提问" else "再次提问")
            }
        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "设置")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text("放大字体") }, onClick = {
                    screenFontSize = (screenFontSize + 2).coerceAtMost(32f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setSparkFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text("缩小字体") }, onClick = {
                    screenFontSize = (screenFontSize - 2).coerceAtLeast(14f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setSparkFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
            }
        }
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        android.util.Log.d("SparkAskScreen", "User confirmed save: ${answer.text.take(50)}...")
                        onSave(answer.text)
                        android.util.Log.d("SparkAskScreen", "Save completed")
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text("保存") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        android.util.Log.d("SparkAskScreen", "User cancelled save")
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text("不保存") }
                },
                text = { Text("是否保存当前内容？") }
            )
        }
    }
}
