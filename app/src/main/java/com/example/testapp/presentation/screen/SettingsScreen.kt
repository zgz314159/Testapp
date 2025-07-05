package com.example.testapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val fontSize by viewModel.fontSize.collectAsState()
    val fontStyle by viewModel.fontStyle.collectAsState()
    val examCount by viewModel.examQuestionCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val context = LocalContext.current

    // 首次进入时加载字体设置
    LaunchedEffect(Unit) {
        viewModel.loadFontSettings(context)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris != null && uris.isNotEmpty()) {
            viewModel.importQuestionsFromUris(context, uris) { success, duplicateFiles ->
                snackbarMessage = when {
                    success && (duplicateFiles == null || duplicateFiles.isEmpty()) -> "题库导入成功"
                    success && duplicateFiles != null -> "部分题库导入成功，以下文件已存在：${duplicateFiles.joinToString()}"
                    !success && duplicateFiles != null -> "导入失败，以下文件已存在：${duplicateFiles.joinToString()}"
                    else -> "题库导入失败"
                }
            }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        if (uri != null) {
            viewModel.exportQuestionsToExcelFile(context, uri) { success ->
                snackbarMessage = if (success) "题库导出成功" else "题库导出失败"
            }
        }
    }
    val importWrongBookLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importWrongBookFromUri(context, uri) { success ->
                snackbarMessage = if (success) "错题本导入成功" else "错题本导入失败"
            }
        }
    }
    val exportWrongBookLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        if (uri != null) {
            viewModel.exportWrongBookToExcelFile(context, uri) { success ->
                snackbarMessage = if (success) "错题本导出成功" else "错题本导出失败"
            }
        }
    }
    val importHistoryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importHistoryFromUri(context, uri) { success ->
                snackbarMessage = if (success) "历史记录导入成功" else "历史记录导入失败"
            }
        }
    }
    val exportHistoryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) { uri ->
        if (uri != null) {
            viewModel.exportHistoryToExcelFile(context, uri) { success ->
                snackbarMessage = if (success) "历史记录导出成功" else "历史记录导出失败"
            }
        }
    }

    // Snackbar 弹窗逻辑
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "设置",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = fontSize.sp,
                fontFamily = when (fontStyle) {
                    "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                    "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                    else -> androidx.compose.ui.text.font.FontFamily.Default
                }
            )
        )
        android.util.Log.d("SettingsScreen", "fontSize=$fontSize, fontStyle=$fontStyle, recomposed!")
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "字体大小：${fontSize}sp",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                fontFamily = when (fontStyle) {
                    "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                    "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                    else -> androidx.compose.ui.text.font.FontFamily.Default
                }
            )
        )
        Slider(
            value = fontSize,
            onValueChange = { viewModel.setFontSize(context, it) },
            valueRange = 14f..32f,
            steps = 3
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "字体样式：",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                fontFamily = when (fontStyle) {
                    "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                    "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                    else -> androidx.compose.ui.text.font.FontFamily.Default
                }
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = fontStyle == "Normal", onClick = { viewModel.setFontStyle(context, "Normal") })
            Text("常规", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Default))
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = fontStyle == "Serif", onClick = { viewModel.setFontStyle(context, "Serif") })
            Text("衬线", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif))
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = fontStyle == "Monospace", onClick = { viewModel.setFontStyle(context, "Monospace") })
            Text("等宽", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            if (examCount == 0) "考试题数：全部" else "考试题数：$examCount",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                fontFamily = when (fontStyle) {
                    "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                    "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                    else -> androidx.compose.ui.text.font.FontFamily.Default
                }
            )
        )
        Slider(
            value = examCount.toFloat(),
            onValueChange = {
                val value = it.roundToInt()
                viewModel.setExamQuestionCount(context, value)
            },
            valueRange = 0f..100f,
            steps = 0
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            importLauncher.launch(arrayOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/plain"))
        }) {
            Text("导入题库文件（支持xls/xlsx/txt）", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = when (fontStyle) { "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif; "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace; else -> androidx.compose.ui.text.font.FontFamily.Default }))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            exportLauncher.launch("quiz_export.xlsx")
        }) {
            Text("导出题库数据（Excel）", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = when (fontStyle) { "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif; "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace; else -> androidx.compose.ui.text.font.FontFamily.Default }))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { importWrongBookLauncher.launch(arrayOf("application/json", "text/plain")) }) {
            Text("导入错题本", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = when (fontStyle) { "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif; "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace; else -> androidx.compose.ui.text.font.FontFamily.Default }))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { exportWrongBookLauncher.launch("wrongbook_export.json") }) {
            Text("导出错题本", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = when (fontStyle) { "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif; "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace; else -> androidx.compose.ui.text.font.FontFamily.Default }))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { importHistoryLauncher.launch(arrayOf("application/json", "text/plain")) }) {
            Text("导入历史记录", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = when (fontStyle) { "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif; "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace; else -> androidx.compose.ui.text.font.FontFamily.Default }))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { exportHistoryLauncher.launch("history_export.json") }) {
            Text("导出历史记录", style = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp, fontFamily = when (fontStyle) { "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif; "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace; else -> androidx.compose.ui.text.font.FontFamily.Default }))
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 使用转圈式进度指示器，符合现代交互习惯
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "正在处理，请稍候…",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        fontFamily = when (fontStyle) {
                            "Serif" -> androidx.compose.ui.text.font.FontFamily.Serif
                            "Monospace" -> androidx.compose.ui.text.font.FontFamily.Monospace
                            else -> androidx.compose.ui.text.font.FontFamily.Default
                        }
                    )
                )
                }
            }
        }
    }

