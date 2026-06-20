package com.example.testapp.presentation.screen.ai

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
import androidx.compose.ui.res.stringResource
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
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import com.example.testapp.presentation.component.ActionModeTextToolbar
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import kotlinx.coroutines.launch

@Composable
fun SparkAskScreen(
    text: String,
    questionId: Int,
    index: Int,
    navController: NavController? = null,
    onSave: suspend (String) -> Unit = {},
    viewModel: SparkAskViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()
    val parsingText = stringResource(com.example.testapp.R.string.parsing)
    val askText = stringResource(com.example.testapp.R.string.ask)
    val askAgainText = stringResource(com.example.testapp.R.string.ask_again)
    val settingsText = stringResource(com.example.testapp.R.string.settings)
    val increaseFontText = stringResource(com.example.testapp.R.string.increase_font)
    val decreaseFontText = stringResource(com.example.testapp.R.string.decrease_font)
    val saveText = stringResource(com.example.testapp.R.string.save)
    val dontSaveText = stringResource(com.example.testapp.R.string.cancel)
    val confirmSaveText = stringResource(com.example.testapp.R.string.confirm_save_changes)
    val parseFailedKeyword = stringResource(com.example.testapp.R.string.parse_failed)
    val parsingKeyword = parsingText.removeSuffix("...")
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
        if (result.isNotBlank() && result != parsingText && !result.contains(parseFailedKeyword)) {
            answer = TextFieldValue(result)
            originalAnswer = result
            // 移除自动保存，由用户手动控制保存
            
        } else if (result.isNotBlank() && result != parsingText) {
            // 在 UI 上显示解析失败结果
            answer = TextFieldValue(result)
            originalAnswer = result
        }
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    val saveScope = rememberCoroutineScope()

    BackHandler {
        // 如果内容不为空，弹出保存确认对话框
        if (answer.text.isNotBlank() && 
            !answer.text.contains(parsingKeyword) && 
            !answer.text.contains(parseFailedKeyword)) {
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
                    answer = TextFieldValue(parsingText)
                    viewModel.ask(question.text)
                },
                enabled = answer.text != parsingText
            ) {
                Text(if (answer.text.isBlank() || answer.text == parsingText) askText else askAgainText)
            }
        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = settingsText)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text(increaseFontText) }, onClick = {
                    screenFontSize = (screenFontSize + 2).coerceAtMost(32f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setSparkFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                })
                DropdownMenuItem(text = { Text(decreaseFontText) }, onClick = {
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
                        saveScope.launch {
                            onSave(answer.text)
                            showSaveDialog = false
                            navController?.popBackStack()
                        }
                    }) { Text(saveText) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) { Text(dontSaveText) }
                },
                text = { Text(confirmSaveText) }
            )
        }
    }
}

