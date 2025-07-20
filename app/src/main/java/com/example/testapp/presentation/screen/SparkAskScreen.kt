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
    var showSaveDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val toolbar = remember(view, navController) {
        ActionModeTextToolbar(view) {
            val sel = answerState.value.selection
            val selected = if (sel.min < sel.max) answerState.value.text.substring(sel.min, sel.max) else ""
            if (selected.isNotBlank()) {
                val encoded = java.net.URLEncoder.encode(selected, "UTF-8")
                navController?.navigate("spark_ask/$questionId/$index/$encoded")
            }
        }
    }

    LaunchedEffect(Unit) {
        val saved = viewModel.getSavedNote(questionId)
        if (!saved.isNullOrBlank()) {
            answer = TextFieldValue(saved)
            originalAnswer = saved
        }
    }

    LaunchedEffect(result) {
        if (result.isNotBlank() && result != "解析中...") {
            answer = TextFieldValue(result)
            originalAnswer = result
        }
    }

    BackHandler {
        if (answer.text != originalAnswer && answer.text.isNotBlank()) {
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
                .padding(16.dp)
        ) {
            BasicTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = LocalFontSize.current, fontFamily = LocalFontFamily.current),
            )

            Spacer(modifier = Modifier.height(16.dp))
            CompositionLocalProvider(LocalTextToolbar provides toolbar) {
                BasicTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = screenFontSize.sp, fontFamily = LocalFontFamily.current),
                )
            }
        }
        if (answer.text.isBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Button(onClick = {
                    answer = TextFieldValue("解析中...")
                    viewModel.ask(question.text)
                }) { Text("提问") }
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
                        onSave(answer.text)
                        viewModel.save(questionId, answer.text)
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text("保存") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text("取消") }
                },
                text = { Text("是否保存修改？") }
            )
        }
    }
}