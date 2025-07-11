package com.example.testapp.presentation.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.presentation.component.LocalFontFamily
import com.example.testapp.presentation.component.LocalFontSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val fontSize by viewModel.fontSize.collectAsState()
    val fontStyle by viewModel.fontStyle.collectAsState()
    val examCount by viewModel.examQuestionCount.collectAsState()
    val randomPractice by viewModel.randomPractice.collectAsState()
    val correctDelay by viewModel.correctDelay.collectAsState()
    val wrongDelay by viewModel.wrongDelay.collectAsState()
    val examDelay by viewModel.examDelay.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val context = LocalContext.current

    // 折叠状态
    var examExpanded by remember { mutableStateOf(false) }
    var practiceExpanded by remember { mutableStateOf(false) }

    // 首次进入时加载字体和其它设置
    LaunchedEffect(Unit) {
        viewModel.loadFontSettings(context)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // 导入题库启动器
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            viewModel.importQuestionsFromUris(context, uris) { success, duplicateFiles ->
                snackbarMessage = when {
                    success && duplicateFiles.isNullOrEmpty() ->
                        "题库导入成功"
                    success && !duplicateFiles.isNullOrEmpty() ->
                        "部分题库导入成功，以下文件已存在：${duplicateFiles.joinToString()}"
                    !success && !duplicateFiles.isNullOrEmpty() ->
                        "导入失败，以下文件已存在：${duplicateFiles.joinToString()}"
                    else ->
                        "题库导入失败"
                }
            }
        }
    }
    // 导出题库启动器
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            viewModel.exportQuestionsToExcelFile(context, it) { success ->
                snackbarMessage = if (success) "题库导出成功" else "题库导出失败"
            }
        }
    }
    // 导入错题本启动器
    val importWrongBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importWrongBookFromUri(context, it) { success ->
                snackbarMessage = if (success) "错题本导入成功" else "错题本导入失败"
            }
        }
    }
    // 导出错题本启动器
    val exportWrongBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            viewModel.exportWrongBookToExcelFile(context, it) { success ->
                snackbarMessage = if (success) "错题本导出成功" else "错题本导出失败"
            }
        }
    }
    // 导入收藏记录启动器
    val importFavoriteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importFavoritesFromUri(context, it) { success ->
                snackbarMessage = if (success) "收藏记录导入成功" else "收藏记录导入失败"
            }
        }
    }
    // 导出收藏记录启动器
    val exportFavoriteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            viewModel.exportFavoritesToExcelFile(context, it) { success ->
                snackbarMessage = if (success) "收藏记录导出成功" else "收藏记录导出失败"
            }
        }
    }

    // Snackbar 弹窗
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 标题
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
        Spacer(modifier = Modifier.height(24.dp))

        // 字体大小
        Text(
            "字体大小：${fontSize}sp",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                fontFamily = LocalFontFamily.current
            )
        )
        Slider(
            value = fontSize,
            onValueChange = { viewModel.setFontSize(context, it) },
            valueRange = 14f..32f,
            steps = 3
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 字体样式
        Text(
            "字体样式：",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize.sp,
                fontFamily = LocalFontFamily.current
            )
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = fontStyle == "Normal",
                onClick = { viewModel.setFontStyle(context, "Normal") }
            )
            Text(
                "常规",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = fontStyle == "Serif",
                onClick = { viewModel.setFontStyle(context, "Serif") }
            )
            Text(
                "衬线",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                )
            )
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = fontStyle == "Monospace",
                onClick = { viewModel.setFontStyle(context, "Monospace") }
            )
            Text(
                "等宽",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // 考试 可折叠标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { examExpanded = !examExpanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "考试：",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (examExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (examExpanded) "收起考试设置" else "展开考试设置"
            )
        }
        if (examExpanded) {
            // 考试题数
            Text(
                if (examCount == 0) "考试题数：全部" else "考试题数：$examCount",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
            var sliderPosition by remember(examCount) {
                mutableStateOf(if (examCount == 0) 150f else examCount.toFloat())
            }
            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    val value = if (it <= 100f) it.roundToInt() else 0
                    viewModel.setExamQuestionCount(context, value)
                },
                valueRange = 0f..150f,
                steps = 0
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 答题停留时间
            Text(
                "答题停留时间：${examDelay}秒",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
            Slider(
                value = examDelay.toFloat(),
                onValueChange = { viewModel.setExamDelay(context, it.roundToInt()) },
                valueRange = 0f..10f,
                steps = 5
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 练习 可折叠标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { practiceExpanded = !practiceExpanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "练习：",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (practiceExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (practiceExpanded) "收起练习设置" else "展开练习设置"
            )
        }
        if (practiceExpanded) {
            // 随机练习开关
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "随机练习：",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        fontFamily = LocalFontFamily.current
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = randomPractice,
                    onCheckedChange = { viewModel.setRandomPractice(context, it) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // 答对停留时间
            Text(
                "答对停留时间：${correctDelay}秒",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
            Slider(
                value = correctDelay.toFloat(),
                onValueChange = { viewModel.setCorrectDelay(context, it.roundToInt()) },
                valueRange = 0f..10f,
                steps = 5
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 答错停留时间
            Text(
                "答错停留时间：${wrongDelay}秒",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
            Slider(
                value = wrongDelay.toFloat(),
                onValueChange = { viewModel.setWrongDelay(context, it.roundToInt()) },
                valueRange = 0f..10f,
                steps = 5
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 导入/导出按钮
        Button(onClick = { importLauncher.launch(
            arrayOf(
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain"
            )
        )
        }) {
            Text(
                "导入题库文件（支持xls/xlsx/txt）",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { exportLauncher.launch("quiz_export.xlsx") }) {
            Text(
                "导出题库数据（Excel）",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            importWrongBookLauncher.launch(
                arrayOf(
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
        }) {
            Text(
                "导入错题本",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { exportWrongBookLauncher.launch("wrongbook_export.xlsx") }) {
            Text(
                "导出错题本",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            importFavoriteLauncher.launch(
                arrayOf(
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
            )
        }) {
            Text(
                "导入收藏记录",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { exportFavoriteLauncher.launch("favorite_export.xlsx") }) {
            Text(
                "导出收藏记录",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    fontFamily = LocalFontFamily.current
                )
            )
        }
    }

    // Snackbar 和加载中提示
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "正在处理，请稍候…",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = fontSize.sp,
                            fontFamily = LocalFontFamily.current
                        )
                    )
                }
            }
        }
    }
}
