package com.example.testapp.presentation.screen.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.testapp.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.presentation.screen.settings.ui.SettingsFillPanel
import com.example.testapp.presentation.screen.file.QuizFileBrowserDialog
import com.example.testapp.presentation.screen.file.hasStorageAccess

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit = {}
) {
    val fontSize by viewModel.fontSize.collectAsState()
    val fontStyle by viewModel.fontStyle.collectAsState()
    val examCount by viewModel.examQuestionCount.collectAsState()
    val practiceCount by viewModel.practiceQuestionCount.collectAsState()
    val randomPractice by viewModel.randomPractice.collectAsState()
    val randomExam by viewModel.randomExam.collectAsState()
    val correctDelay by viewModel.correctDelay.collectAsState()
    val wrongDelay by viewModel.wrongDelay.collectAsState()
    val examDelay by viewModel.examDelay.collectAsState()
    val fillBlankCount by viewModel.fillBlankCount.collectAsState()
    val fillQuestionGenerationMode by viewModel.fillQuestionGenerationMode.collectAsState()
    val fillFullAnswerRandomOrder by viewModel.fillFullAnswerRandomOrder.collectAsState()
    val fillFullAnswerRequireCorrect by viewModel.fillFullAnswerRequireCorrect.collectAsState()
    val fillAnswerScoreMin by viewModel.fillAnswerScoreMin.collectAsState()
    val fillAnswerScoreMax by viewModel.fillAnswerScoreMax.collectAsState()
    val fillAnswerTagFilter by viewModel.fillAnswerTagFilter.collectAsState()
    val availableFillAnswerTags by viewModel.availableFillAnswerTags.collectAsState()
    val fillQuestionFilterSummary by viewModel.fillQuestionFilterSummary.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val darkTheme by viewModel.darkTheme.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val importProgress by viewModel.progress.collectAsState()
    val context = LocalContext.current

    var examExpanded by remember { mutableStateOf(false) }
    var practiceExpanded by remember { mutableStateOf(false) }
    var fillExpanded by remember { mutableStateOf(false) }
    var showFileBrowser by remember { mutableStateOf(false) }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }
    var showExportFilePicker by remember { mutableStateOf(false) }
    var pendingExportType by remember { mutableStateOf("quiz") }
    val quizFileNames by viewModel.quizFileNames.collectAsState()
    val wrongBookFileNames by viewModel.wrongBookFileNames.collectAsState()
    val favoriteFileNames by viewModel.favoriteFileNames.collectAsState()

    // 启动时加载字体设置并启动集合监听
    LaunchedEffect(Unit) {
        viewModel.loadFontSettings()
        viewModel.ensureSettingsCollectionsStarted()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // 导入启动器
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            viewModel.importQuestionsFromUris(context, uris) { success, errorFiles ->
                snackbarMessage = when {
                    success && errorFiles.isNullOrEmpty() -> {
                        onNavigateHome()
                        context.getString(R.string.import_success)
                    }
                    success && !errorFiles.isNullOrEmpty() -> {
                        val errorCount = errorFiles.size
                        val successCount = uris.size - errorCount
                        if (successCount > 0) onNavigateHome()
                        val reasons = errorFiles.take(2).joinToString("；") + if (errorFiles.size > 2) "…" else ""
                        context.getString(R.string.import_partial, successCount, errorCount, reasons)
                    }
                    !success && !errorFiles.isNullOrEmpty() -> {
                        val reasons = errorFiles.take(3).joinToString("；") + if (errorFiles.size > 3) "…" else ""
                        context.getString(R.string.import_failed) + "：" + reasons
                    }
                    else ->
                        context.getString(R.string.import_failed)
                }
            }
        } else {
            snackbarMessage = context.getString(R.string.import_canceled)
        }
    }
    val manageStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasStorageAccess(context)) {
            showFileBrowser = true
        } else {
            snackbarMessage = context.getString(R.string.import_quiz_local_perm_denied)
        }
    }
    val readStorageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted || hasStorageAccess(context)) {
            showFileBrowser = true
        } else {
            snackbarMessage = context.getString(R.string.import_quiz_local_perm_denied)
        }
    }
    val launchLocalImport = {
        if (hasStorageAccess(context)) {
            showFileBrowser = true
        } else {
            showStoragePermissionDialog = true
        }
    }
    // 导入启动器
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
                uri?.let {
            viewModel.exportQuestionsToExcelFile(context, it, null) { success ->
                snackbarMessage = if (success) context.getString(R.string.export_quiz_success) else context.getString(R.string.export_quiz_failed)
            }
        }
    }
    // 导入启动器?
    val importWrongBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
                uri?.let {
            viewModel.importWrongBookFromUri(context, it) { success ->
                snackbarMessage = if (success) context.getString(R.string.import_wrong_success) else context.getString(R.string.import_wrong_failed)
            }
        }
    }
    // 导入启动器?
    val exportWrongBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
                uri?.let {
            viewModel.exportWrongBookToExcelFile(context, it, null) { success ->
                snackbarMessage = if (success) context.getString(R.string.export_wrong_success) else context.getString(R.string.export_wrong_failed)
            }
        }
    }
    // 导入启动器??
    val importFavoriteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
                uri?.let {
            viewModel.importFavoritesFromUri(context, it) { success ->
                snackbarMessage = if (success) context.getString(R.string.import_favorites_success) else context.getString(R.string.import_favorites_failed)
            }
        }
    }
    // 导入启动器??
    val exportFavoriteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
                uri?.let {
            viewModel.exportFavoritesToExcelFile(context, it, null) { success ->
                snackbarMessage = if (success) context.getString(R.string.export_favorites_success) else context.getString(R.string.export_favorites_failed)
            }
        }
    }

    // Snackbar 提示
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
            stringResource(R.string.settings_title),
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

        SettingsFontSection(
            fontSize = fontSize,
            fontStyle = fontStyle,
            soundEnabled = soundEnabled,
            darkTheme = darkTheme,
            examExpanded = examExpanded,
            practiceExpanded = practiceExpanded,
            randomExam = randomExam,
            randomPractice = randomPractice,
            examCount = examCount,
            practiceCount = practiceCount,
            examDelay = examDelay,
            correctDelay = correctDelay,
            wrongDelay = wrongDelay,
            onFontSizeChange = { viewModel.setFontSize(context, it) },
            onFontStyleChange = { viewModel.setFontStyle(context, it) },
            onSoundEnabledChange = { viewModel.setSoundEnabled(context, it) },
            onDarkThemeChange = { viewModel.setDarkTheme(context, it) },
            onExamExpandedChange = { examExpanded = it },
            onPracticeExpandedChange = { practiceExpanded = it },
            onRandomExamChange = { viewModel.setRandomExam(context, it) },
            onRandomPracticeChange = { viewModel.setRandomPractice(context, it) },
            onExamCountChange = { viewModel.setExamQuestionCount(context, it) },
            onPracticeCountChange = { viewModel.setPracticeQuestionCount(context, it) },
            onExamDelayChange = { viewModel.setExamDelay(context, it) },
            onCorrectDelayChange = { viewModel.setCorrectDelay(context, it) },
            onWrongDelayChange = { viewModel.setWrongDelay(context, it) }
        )

        SettingsFillPanel(
            expanded = fillExpanded,
            onToggle = { fillExpanded = !fillExpanded },
            fontSize = fontSize,
            fillQuestionGenerationMode = fillQuestionGenerationMode,
            fillBlankCount = fillBlankCount,
            fillFullAnswerRequireCorrect = fillFullAnswerRequireCorrect,
            fillFullAnswerRandomOrder = fillFullAnswerRandomOrder,
            fillAnswerScoreMin = fillAnswerScoreMin,
            fillAnswerScoreMax = fillAnswerScoreMax,
            fillAnswerTagFilter = fillAnswerTagFilter,
            availableFillAnswerTags = availableFillAnswerTags,
            fillQuestionFilterSummary = fillQuestionFilterSummary,
            onModeChange = { viewModel.setFillQuestionGenerationMode(context, it) },
            onBlankCountChange = { viewModel.setFillBlankCount(context, it) },
            onRequireCorrectChange = { viewModel.setFillFullAnswerRequireCorrect(context, it) },
            onRandomOrderChange = { viewModel.setFillFullAnswerRandomOrder(context, it) },
            onScoreRangeChange = { min, max -> viewModel.setFillAnswerScoreRange(context, min, max) },
            onTagFilterChange = { viewModel.setFillAnswerTagFilter(context, it) },
            onTagFilterClear = { viewModel.setFillAnswerTagFilter(context, "") }
        )

        SettingsImportButton(
            labelResId = R.string.import_quiz_button,
            fontSize = fontSize,
            onClick = {
                importLauncher.launch(
                    arrayOf(
                        "*/*",
                        "application/json",
                        "application/octet-stream",
                        "application/x-sqlite3",
                        "application/vnd.sqlite3",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "text/plain",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/msword"
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingsImportButton(
            labelResId = R.string.import_quiz_local_button,
            fontSize = fontSize,
            onClick = launchLocalImport
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingsExportButton(
            labelResId = R.string.export_quiz_button,
            fontSize = fontSize,
            onClick = { pendingExportType = "quiz"; showExportFilePicker = true }
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsImportButton(
            labelResId = R.string.import_wrong_button,
            fontSize = fontSize,
            onClick = {
                importWrongBookLauncher.launch(
                    arrayOf(
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingsExportButton(
            labelResId = R.string.export_wrong_button,
            fontSize = fontSize,
            onClick = { pendingExportType = "wrongbook"; showExportFilePicker = true }
        )
        Spacer(modifier = Modifier.height(24.dp))
        SettingsImportButton(
            labelResId = R.string.import_favorites_button,
            fontSize = fontSize,
            onClick = {
                importFavoriteLauncher.launch(
                    arrayOf(
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingsExportButton(
            labelResId = R.string.export_favorites_button,
            fontSize = fontSize,
            onClick = { pendingExportType = "favorite"; showExportFilePicker = true }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        SettingsImportProgressOverlay(
            isLoading = isLoading,
            importProgress = importProgress,
            fontSize = fontSize,
            onCancel = viewModel::cancelImportExport
        )
    }

    if (showStoragePermissionDialog) {
        AlertDialog(
            onDismissRequest = { showStoragePermissionDialog = false },
            title = { Text(stringResource(R.string.import_quiz_local_perm_title)) },
            text = { Text(stringResource(R.string.import_quiz_local_perm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showStoragePermissionDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        manageStorageLauncher.launch(
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    } else {
                        readStorageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }) {
                    Text(stringResource(R.string.import_quiz_local_perm_grant))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStoragePermissionDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    if (showExportFilePicker) {
        val fileList = when (pendingExportType) {
            "wrongbook" -> wrongBookFileNames
            "favorite" -> favoriteFileNames
            else -> quizFileNames
        }
        AlertDialog(
            onDismissRequest = { showExportFilePicker = false },
            title = { Text("选择要导出的题库") },
            text = {
                if (fileList.isEmpty()) {
                    Text("暂无题库文件，请先导入")
                } else {
                    Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                        fileList.forEach { fileName ->
                            TextButton(
                                onClick = {
                                    showExportFilePicker = false
                                    val now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                                    when (pendingExportType) {
                                        "wrongbook" -> exportWrongBookLauncher.launch("${now}_${fileName}.xlsx")
                                        "favorite" -> exportFavoriteLauncher.launch("${now}_${fileName}.xlsx")
                                        else -> exportLauncher.launch("${now}_${fileName}.xlsx")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(fileName, fontSize = fontSize.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
    if (showFileBrowser) {
        QuizFileBrowserDialog(
            onDismiss = { showFileBrowser = false },
            onConfirm = { files ->
                showFileBrowser = false
                viewModel.importQuestionsFromFiles(context, files) { success, errorFiles ->
                    snackbarMessage = when {
                        success && errorFiles.isNullOrEmpty() -> {
                            onNavigateHome()
                            context.getString(R.string.import_success)
                        }
                        success && !errorFiles.isNullOrEmpty() -> {
                            val errorCount = errorFiles.size
                            val successCount = files.size - errorCount
                            if (successCount > 0) onNavigateHome()
                            val reasons = errorFiles.take(2).joinToString("；") + if (errorFiles.size > 2) "…" else ""
                            context.getString(R.string.import_partial, successCount, errorCount, reasons)
                        }
                        !success && !errorFiles.isNullOrEmpty() -> {
                            val reasons = errorFiles.take(3).joinToString("；") + if (errorFiles.size > 3) "…" else ""
                            context.getString(R.string.import_failed) + ":" + reasons
                        }
                        else -> context.getString(R.string.import_failed)
                    }
                }
            }
        )
    }
}