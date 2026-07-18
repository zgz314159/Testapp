package com.example.testapp.presentation.screen.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.feature.settings.R
import com.example.testapp.presentation.screen.settings.file.QuizFileBrowserDialog
import com.example.testapp.presentation.screen.settings.file.hasStorageAccess
import com.example.testapp.presentation.screen.settings.ui.SettingsAnswerSettingsCard
import com.example.testapp.presentation.screen.settings.ui.SettingsAppearanceCard
import com.example.testapp.presentation.screen.settings.ui.SettingsCardGroup
import com.example.testapp.presentation.screen.settings.ui.SettingsDataManagementSection
import com.example.testapp.presentation.screen.settings.ui.SettingsExportBottomSheet
import com.example.testapp.presentation.screen.settings.ui.SettingsImportProgressOverlay
import com.example.testapp.presentation.screen.settings.ui.SettingsImportQuizBottomSheet
import com.example.testapp.presentation.screen.settings.ui.SettingsNavListItem
import com.example.testapp.presentation.screen.settings.ui.SettingsSectionHeader
import com.example.testapp.presentation.screen.settings.ui.SettingsTopBar
import com.example.testapp.presentation.screen.settings.ui.importSnackbarMessages
import com.example.testapp.uicommon.design.AppElevatedActionSheetTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit = {},
    onNavigateFillSettings: () -> Unit = {},
    onNavigateAiService: () -> Unit = {},
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
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val darkTheme by viewModel.darkTheme.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val importProgress by viewModel.progress.collectAsState()
    val context = LocalContext.current
    val importSnackbarMessages = remember(context) { importSnackbarMessages(context) }

    var examExpanded by remember { mutableStateOf(false) }
    var practiceExpanded by remember { mutableStateOf(false) }
    var showFileBrowser by remember { mutableStateOf(false) }
    var showStoragePermissionDialog by remember { mutableStateOf(false) }
    var showExportFilePicker by remember { mutableStateOf(false) }
    var showImportQuizSheet by remember { mutableStateOf(false) }
    var pendingExportType by remember { mutableStateOf("quiz") }
    val quizFileNames by viewModel.quizFileNames.collectAsState()
    val wrongBookFileNames by viewModel.wrongBookFileNames.collectAsState()
    val favoriteFileNames by viewModel.favoriteFileNames.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFontSettings()
        viewModel.ensureSettingsCollectionsStarted()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (!uris.isNullOrEmpty()) {
            viewModel.importQuestionsFromUris(context, uris) { success, errorFiles ->
                val result = resolveImportSnackbarResult(
                    uris.size, success, errorFiles, importSnackbarMessages
                )
                if (result.shouldNavigateHome) onNavigateHome()
                snackbarMessage = result.message
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
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            viewModel.exportQuestionsToExcelFile(context, it, null) { success ->
                snackbarMessage = if (success) {
                    context.getString(R.string.export_quiz_success)
                } else {
                    context.getString(R.string.export_quiz_failed)
                }
            }
        }
    }
    val importWrongBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importWrongBookFromUri(context, it) { success ->
                snackbarMessage = if (success) {
                    context.getString(R.string.import_wrong_success)
                } else {
                    context.getString(R.string.import_wrong_failed)
                }
            }
        }
    }
    val exportWrongBookLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            viewModel.exportWrongBookToExcelFile(context, it, null) { success ->
                snackbarMessage = if (success) {
                    context.getString(R.string.export_wrong_success)
                } else {
                    context.getString(R.string.export_wrong_failed)
                }
            }
        }
    }
    val importFavoriteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importFavoritesFromUri(context, it) { success ->
                snackbarMessage = if (success) {
                    context.getString(R.string.import_favorites_success)
                } else {
                    context.getString(R.string.import_favorites_failed)
                }
            }
        }
    }
    val exportFavoriteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        uri?.let {
            viewModel.exportFavoritesToExcelFile(context, it, null) { success ->
                snackbarMessage = if (success) {
                    context.getString(R.string.export_favorites_success)
                } else {
                    context.getString(R.string.export_favorites_failed)
                }
            }
        }
    }

    val exportTimestampPattern = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val launchExportForFile: (String) -> Unit = { fileName ->
        val timestamp = java.time.LocalDateTime.now().format(exportTimestampPattern)
        val output = buildExportOutputName(fileName, timestamp)
        when (pendingExportType) {
            "wrongbook" -> exportWrongBookLauncher.launch(output)
            "favorite" -> exportFavoriteLauncher.launch(output)
            else -> exportLauncher.launch(output)
        }
    }
    val requestExport: (String, List<String>) -> Unit = { type, fileNames ->
        pendingExportType = type
        resolveDirectExportFileName(fileNames)?.let(launchExportForFile)
            ?: run { showExportFilePicker = true }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppElevatedActionSheetTokens.sheetBg,
        topBar = { SettingsTopBar(onBack = onNavigateHome) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                SettingsSectionHeader(stringResource(R.string.settings_section_appearance))
                SettingsAppearanceCard(
                    fontSize = fontSize,
                    fontStyle = fontStyle,
                    soundEnabled = soundEnabled,
                    darkTheme = darkTheme,
                    onFontSizeChange = { viewModel.setFontSize(context, it) },
                    onFontStyleChange = { viewModel.setFontStyle(context, it) },
                    onSoundEnabledChange = { viewModel.setSoundEnabled(context, it) },
                    onDarkThemeChange = { viewModel.setDarkTheme(context, it) }
                )

                SettingsSectionHeader(stringResource(R.string.settings_section_answer))
                SettingsAnswerSettingsCard(
                    fontSize = fontSize,
                    practiceExpanded = practiceExpanded,
                    examExpanded = examExpanded,
                    randomPractice = randomPractice,
                    randomExam = randomExam,
                    practiceCount = practiceCount,
                    examCount = examCount,
                    correctDelay = correctDelay,
                    wrongDelay = wrongDelay,
                    examDelay = examDelay,
                    onPracticeExpandedChange = { practiceExpanded = it },
                    onExamExpandedChange = { examExpanded = it },
                    onRandomPracticeChange = { viewModel.setRandomPractice(context, it) },
                    onRandomExamChange = { viewModel.setRandomExam(context, it) },
                    onPracticeCountChange = { viewModel.setPracticeQuestionCount(context, it) },
                    onExamCountChange = { viewModel.setExamQuestionCount(context, it) },
                    onCorrectDelayChange = { viewModel.setCorrectDelay(context, it) },
                    onWrongDelayChange = { viewModel.setWrongDelay(context, it) },
                    onExamDelayChange = { viewModel.setExamDelay(context, it) },
                )
                SettingsCardGroup {
                    SettingsNavListItem(
                        label = stringResource(R.string.settings_fill_settings),
                        fontSize = fontSize,
                        onClick = onNavigateFillSettings,
                        leadingIcon = Icons.Filled.EditNote
                    )
                }

                SettingsSectionHeader(stringResource(R.string.settings_section_ai))
                SettingsCardGroup {
                    SettingsNavListItem(
                        label = stringResource(R.string.settings_ai_service),
                        fontSize = fontSize,
                        onClick = onNavigateAiService,
                        leadingIcon = Icons.Filled.AutoAwesome,
                    )
                }

                SettingsSectionHeader(stringResource(R.string.settings_section_data))
                SettingsDataManagementSection(
                    fontSize = fontSize,
                    onImportQuiz = { showImportQuizSheet = true },
                    onExportQuiz = { requestExport("quiz", quizFileNames) },
                    onImportWrong = {
                        importWrongBookLauncher.launch(
                            arrayOf(
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                        )
                    },
                    onExportWrong = { requestExport("wrongbook", wrongBookFileNames) },
                    onImportFavorites = {
                        importFavoriteLauncher.launch(
                            arrayOf(
                                "application/vnd.ms-excel",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                        )
                    },
                    onExportFavorites = { requestExport("favorite", favoriteFileNames) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            SettingsImportProgressOverlay(
                isLoading = isLoading,
                importProgress = importProgress,
                fontSize = fontSize,
                onCancel = viewModel::cancelImportExport
            )
        }
    }

    if (showStoragePermissionDialog) {
        SettingsStoragePermissionDialog(
            onDismiss = { showStoragePermissionDialog = false },
            onGrant = {
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
            }
        )
    }

    if (showImportQuizSheet) {
        SettingsImportQuizBottomSheet(
            onImportFile = {
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
            },
            onImportLocal = launchLocalImport,
            onDismiss = { showImportQuizSheet = false }
        )
    }

    if (showExportFilePicker) {
        val (fileList, title, emptyMessage) = when (pendingExportType) {
            "wrongbook" -> Triple(
                wrongBookFileNames,
                stringResource(R.string.export_wrong_select_title),
                stringResource(R.string.export_wrong_no_data)
            )
            "favorite" -> Triple(
                favoriteFileNames,
                stringResource(R.string.export_favorites_select_title),
                stringResource(R.string.export_favorites_no_data)
            )
            else -> Triple(
                quizFileNames,
                stringResource(R.string.export_quiz_select_title),
                stringResource(R.string.export_quiz_no_data)
            )
        }
        SettingsExportBottomSheet(
            title = title,
            fileNames = fileList,
            emptyMessage = emptyMessage,
            fontSize = fontSize,
            onSelectFile = { fileName ->
                showExportFilePicker = false
                launchExportForFile(fileName)
            },
            onDismiss = { showExportFilePicker = false }
        )
    }

    if (showFileBrowser) {
        QuizFileBrowserDialog(
            onDismiss = { showFileBrowser = false },
            onConfirm = { files ->
                showFileBrowser = false
                viewModel.importQuestionsFromFiles(context, files) { success, errorFiles ->
                    val result = resolveImportSnackbarResult(
                        files.size, success, errorFiles, importSnackbarMessages
                    )
                    if (result.shouldNavigateHome) onNavigateHome()
                    snackbarMessage = result.message
                }
            }
        )
    }
}
