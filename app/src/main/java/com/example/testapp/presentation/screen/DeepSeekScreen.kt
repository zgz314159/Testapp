package com.example.testapp.presentation.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import kotlinx.coroutines.launch

@Composable
fun DeepSeekScreen(
    text: String,
    questionId: Int,
    index: Int,
    navController: NavController? = null,
    onSave: (String) -> Unit = {},
    aiViewModel: DeepSeekViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // —— 字号设置相关 ——
    val globalFontSize by settingsViewModel.fontSize.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val storedSize by FontSettingsDataStore
        .getDeepSeekFontSize(context, Float.NaN)
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
            FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
        }
    }

    // —— 菜单 & 编辑文本 & 保存弹窗 ——
    var menuExpanded by remember { mutableStateOf(false) }
    var editableText by remember { mutableStateOf(text) }
    var showSaveDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (editableText != text) {
            showSaveDialog = true
        } else {
            navController?.popBackStack()
        }
    }

    // —— 流式 AI 解析 ——
    val analysisPair by aiViewModel.analysis.collectAsState()
    val analysisText: String? = run {
        val pair = analysisPair
        when {
            pair == null || pair.first != index -> null
            pair.second.isEmpty()              -> "解析中..."
            else                               -> pair.second
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // —— 展示流式解析内容 ——
            analysisText?.let {
                Text(
                    text = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    style = TextStyle(
                        fontSize = screenFontSize.sp,
                        fontFamily = LocalFontFamily.current
                    )
                )
            }

            // —— 用户可编辑结果区 ——
            BasicTextField(
                value = editableText,
                onValueChange = { editableText = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = screenFontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
        }

        // —— 字号设置按钮 ——
        IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Filled.MoreVert, contentDescription = "设置")
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("放大字体") },
                onClick = {
                    screenFontSize = (screenFontSize + 2).coerceAtMost(32f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("缩小字体") },
                onClick = {
                    screenFontSize = (screenFontSize - 2).coerceAtLeast(14f)
                    coroutineScope.launch {
                        FontSettingsDataStore.setDeepSeekFontSize(context, screenFontSize)
                    }
                    menuExpanded = false
                }
            )
        }

        // —— 保存确认弹窗 ——
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        onSave(editableText)
                        aiViewModel.save(questionId, editableText)
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) {
                        Text("保存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showSaveDialog = false
                        navController?.popBackStack()
                    }) {
                        Text("取消")
                    }
                },
                text = { Text("是否保存修改？") }
            )
        }
    }
}
