package com.example.testapp.presentation.screen.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import com.example.testapp.presentation.screen.home.components.HomeContinueStudyCard
import com.example.testapp.presentation.screen.home.components.HomeGreetingHeader
import com.example.testapp.presentation.screen.home.components.HomeNotificationAction
import com.example.testapp.presentation.screen.home.components.HomeScreenLibrarySection
import com.example.testapp.presentation.screen.home.components.HomeSearchAction
import com.example.testapp.presentation.screen.home.components.HomeSectionHeader
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import com.example.testapp.presentation.screen.questionbank.QuestionBankDrawerViewModel
import com.example.testapp.presentation.screen.settings.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    folderViewModel: FileFolderViewModel,
    dragViewModel: DragDropViewModel,
    drawerViewModel: QuestionBankDrawerViewModel,
    settingsViewModel: SettingsViewModel,
    onStartQuiz: (quizId: String) -> Unit = {},
    onStartAdaptive: (quizId: String) -> Unit = {},
    onBrowseQuestion: (fileName: String, questionId: Int) -> Unit = { fileName, _ -> onStartQuiz(fileName) },
    onEditQuestion: (fileName: String, questionId: Int) -> Unit = { _, _ -> },
    onStartExam: (quizId: String) -> Unit = {},
    onSettings: () -> Unit = {},
    onViewQuestionDetail: (quizId: String) -> Unit = {},
    onWrongBook: (fileName: String) -> Unit = {},
    onFavoriteBook: (fileName: String) -> Unit = {},
    onHistory: () -> Unit = {},
) {
    remember { HomePerformanceLog.resetSession() }
    DisposableEffect(Unit) {
        onDispose { HomePerformanceLog.event("home_exit") }
    }
    val fileNames by viewModel.fileNames.collectAsState()
    val folders by folderViewModel.folders.collectAsState()
    val folderNames by folderViewModel.folderNames.collectAsState()
    val fileStatistics by viewModel.fileStatistics.collectAsState()
    val homeContentReady by viewModel.homeContentReady.collectAsState()
    val isLoading by settingsViewModel.isLoading.collectAsState()
    val importProgress by settingsViewModel.progress.collectAsState()

    var currentFolder by remember { mutableStateOf<String?>(null) }
    // 分组内系统返回（手势/按键）先退回主页根列表，而不是退出 App
    BackHandler(enabled = currentFolder != null) { currentFolder = null }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedFileName = remember { mutableStateOf("") }

    val storedFileName by FontSettingsDataStore
        .getLastSelectedFile(context)
        .collectAsState(initial = "")
    val recentFileNames by FontSettingsDataStore
        .getRecentSelectedFiles(context)
        .collectAsState(initial = emptyList())

    val practiceProgress by viewModel.practiceProgress.collectAsState()

    val dashboardState = remember(
        fileNames,
        fileStatistics,
        practiceProgress,
        storedFileName,
        selectedFileName.value,
        recentFileNames,
    ) {
        HomePerformanceLog.measure("dashboard_build files=${fileNames.size}") {
            HomeDashboardPipeline.buildDashboard(
                fileNames = fileNames,
                fileStatistics = fileStatistics,
                practiceProgressCompleted = practiceProgress,
                storedFileName = storedFileName,
                selectedFileName = selectedFileName.value,
                recentFileNames = recentFileNames,
            )
        }
    }

    LaunchedEffect(homeContentReady, fileNames.size, fileStatistics.size, practiceProgress.size) {
        HomePerformanceLog.event(
            "content_state ready=$homeContentReady files=${fileNames.size} " +
                "statistics=${fileStatistics.size} progress=${practiceProgress.size}",
        )
    }

    val libraryState = rememberHomeLibraryDisplayState(
        fileNames = fileNames,
        folders = folders,
        folderNames = folderNames,
        currentFolder = currentFolder,
        storedFileName = storedFileName,
        recentFileNames = recentFileNames,
    )
    val navPrefs = rememberHomeNavPrefsState()

    LaunchedEffect(storedFileName, fileNames) {
        if (selectedFileName.value.isNotBlank() && selectedFileName.value in fileNames) return@LaunchedEffect
        selectedFileName.value = when {
            storedFileName.isNotBlank() && storedFileName in fileNames -> storedFileName
            fileNames.isNotEmpty() -> fileNames.first()
            else -> ""
        }
    }

    var showSheet by remember { mutableStateOf(false) }
    var pendingFileName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf("") }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var renameFolderTarget by remember { mutableStateOf<String?>(null) }
    var renameFolderName by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteFolderDialog by remember { mutableStateOf(false) }

    val folderBounds = remember { mutableStateMapOf<String, Rect>() }
    val fileCardBounds = remember { mutableStateMapOf<String, Rect>() }
    val homeDropTargetKey = "__HOME_ROOT__"

    val dragPosition by dragViewModel.dragPosition.collectAsState()
    val draggingFile by dragViewModel.draggingFile.collectAsState()
    val dragItemSize by dragViewModel.dragItemSize.collectAsState()
    val dragOffset by dragViewModel.offsetWithinItem.collectAsState()
    val hoverFolder by dragViewModel.hoverFolder.collectAsState()
    val hoverFile by dragViewModel.hoverFile.collectAsState()

    val persistFileUsage: (String) -> Unit = remember(context, scope) {
        { fileName ->
            scope.launch {
                FontSettingsDataStore.markFileAsRecent(context, fileName)
                FontSettingsDataStore.setLastSelectedFile(context, fileName)
            }
        }
    }

    val drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerOpen by remember {
        derivedStateOf {
            drawerState.currentValue == DrawerValue.Open ||
                drawerState.targetValue == DrawerValue.Open
        }
    }
    val homeInteractionReady = rememberHomeInteractionReady()
    val homeRootCoordsRef = remember { HomeRootCoordsRef() }
    val dragFinishRef = remember { HomeDragFinishRef() }

    val homeRootDragModifier = rememberHomeRootDragModifier(
        dragViewModel = dragViewModel,
        folderViewModel = folderViewModel,
        folderBounds = folderBounds,
        fileCardBounds = fileCardBounds,
        coordsRef = homeRootCoordsRef,
        finishRef = dragFinishRef,
        coroutineScope = scope,
        currentFolder = currentFolder,
        homeDropTargetKey = homeDropTargetKey,
        currentFolderFileNames = libraryState.currentFolderFileNames,
    )

    LaunchedEffect(
        libraryState.rootDisplayFileNames,
        libraryState.visibleHomeFolders,
        libraryState.currentFolderDisplayFileNames,
        currentFolder,
    ) {
        pruneHomeDragBounds(
            rootDisplayFileNames = libraryState.rootDisplayFileNames,
            currentFolderDisplayFileNames = libraryState.currentFolderDisplayFileNames,
            visibleHomeFolders = libraryState.visibleHomeFolders,
            currentFolder = currentFolder,
            homeDropTargetKey = homeDropTargetKey,
            fileCardBounds = fileCardBounds,
            folderBounds = folderBounds,
        )
    }

    // 统一滚动 Header + Hero + SectionTitle（嵌入 LazyColumn 首个 item）
    @Composable
    fun DashboardHeaderContent() {
        HomeGreetingHeader(
            greeting = dashboardState.greeting,
            subtitle = dashboardState.subtitle,
            searchAction = {
                HomeSearchAction(onClick = { scope.launch { drawerState.open() } })
            },
            notificationAction = { HomeNotificationAction() },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(HomeDesignTokens.spacingMd))
        if (dashboardState.showContinueCard) {
            HomeContinueStudyCard(
                fileName = dashboardState.continueFileName,
                displayName = dashboardState.continueFileDisplayName,
                progressPercent = dashboardState.continueProgressPercent,
                totalQuestions = dashboardState.totalQuestions,
                wrongCount = dashboardState.wrongCount,
                favoriteCount = dashboardState.favoriteCount,
                completedCount = dashboardState.completedCount,
                onClick = {
                    pendingFileName = dashboardState.continueFileName
                    viewModel.preloadQuestionFile(dashboardState.continueFileName)
                    persistFileUsage(dashboardState.continueFileName)
                    showSheet = true
                },
                onWrongBookClick = {
                    if (dashboardState.continueFileName.isNotBlank()) {
                        onWrongBook(dashboardState.continueFileName)
                    } else {
                        onWrongBook("")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = HomeDesignTokens.pageHorizontalPadding),
            )
        }
        Spacer(modifier = Modifier.height(HomeDesignTokens.spacingXl))
        HomeSectionHeader(
            title = stringResource(R.string.home_book_title),
            onShowAll = { scope.launch { drawerState.open() } },
            modifier = Modifier.padding(horizontal = HomeDesignTokens.pageHorizontalPadding),
        )
    }

    HomeScreenDrawerHost(
        fileNames = fileNames,
        folders = folders,
        folderNames = folderNames,
        fileStatistics = fileStatistics,
        drawerState = drawerState,
        drawerViewModel = drawerViewModel,
        onBrowseQuestion = onBrowseQuestion,
        onEditQuestion = onEditQuestion,
    ) {
        HomeScreenScaffoldContent(
            bottomNavIndex = navPrefs.bottomNavIndex,
            onNavChange = navPrefs.setBottomNavIndex,
            onWrongBook = { onWrongBook("") },
            onFavoriteBook = { onFavoriteBook("") },
            onHistory = onHistory,
            onSettings = onSettings,
            draggingFile = draggingFile,
            drawerOpen = drawerOpen,
            homeInteractionReady = homeInteractionReady,
            homeRootCoordsRef = homeRootCoordsRef,
            homeRootDragModifier = homeRootDragModifier,
            onBlankAreaLongPress = { localOffset ->
                // 题库/文件夹卡片上的长按用于拖拽或重命名，不得误开「新建分组」。
                val rootPos = homeRootCoordsRef.value?.localToRoot(localOffset) ?: localOffset
                val hitCard = fileCardBounds.values.any { it.contains(rootPos) }
                val hitFolder = folderBounds.values.any { it.contains(rootPos) }
                if (!hitCard && !hitFolder) {
                    showAddFolderDialog = true
                }
            },
            librarySection = {
                HomeScreenLibrarySection(
                    currentFolder = currentFolder,
                    displayFolders = libraryState.displayFolders,
                    displayFileNames = libraryState.displayFileNames,
                    folderFileCounts = libraryState.folderFileCounts,
                    folders = folders,
                    homeContentReady = homeContentReady,
                    homeLibraryEmptyReason = libraryState.homeLibraryEmptyReason,
                    viewModel = viewModel,
                    selectedFileName = selectedFileName.value,
                    draggingFile = draggingFile,
                    dragPosition = dragPosition,
                    hoverFolder = hoverFolder,
                    hoverFile = hoverFile,
                    folderBounds = folderBounds,
                    fileCardBounds = fileCardBounds,
                    dragViewModel = dragViewModel,
                    homeDropTargetKey = homeDropTargetKey,
                    currentFolderFileNames = libraryState.currentFolderFileNames,
                    onCurrentFolderChange = { currentFolder = it },
                    onRenameFolderTarget = { renameFolderTarget = it },
                    onRenameFolderName = { renameFolderName = it },
                    onFolderToDelete = { folderToDelete = it },
                    onShowDeleteFolderDialog = { showDeleteFolderDialog = true },
                    onSelectedFileNameChange = { selectedFileName.value = it },
                    onPendingFileName = { pendingFileName = it },
                    onShowSheet = { showSheet = true },
                    onFileToDelete = { fileToDelete = it },
                    onShowDeleteDialog = { showDeleteDialog = true },
                    onViewQuestionDetail = onViewQuestionDetail,
                    persistFileUsage = persistFileUsage,
                    onUpdateDragHover = { position, folder, dropKey, folderFiles ->
                        updateHomeDragHover(
                            dragViewModel, folderBounds, fileCardBounds, position,
                            folder, dropKey, folderFiles,
                        )
                    },
                    onFinishDrag = { fileName, folder, dropKey, folderFiles ->
                        dragFinishRef.handledByCard = true
                        finishActiveDrag(
                            fileName, dragViewModel, folderViewModel, folderBounds, fileCardBounds,
                            scope, "card-onDragEnd", folder, dropKey, folderFiles,
                        )
                    },
                    onBeforeDragStart = { dragFinishRef.handledByCard = false },
                    onFileCtaClick = { fileName ->
                        selectedFileName.value = fileName
                        pendingFileName = fileName
                        viewModel.preloadQuestionFile(fileName)
                        persistFileUsage(fileName)
                        // 有进度时打开 sheet（可接着/重答）；无进度直接开始练习
                        if (
                            (practiceProgress[fileName] ?: 0) > 0 ||
                            HomeAdaptiveModeEligibilityPipeline.isEligible(fileName)
                        ) {
                            showSheet = true
                        } else {
                            onStartQuiz(fileName)
                        }
                    },
                    headerContent = { DashboardHeaderContent() },
                )
            },
            overlays = {
                HomeActionOverlays(
                    context = context,
                    fileStatistics = fileStatistics,
                    practiceProgress = practiceProgress,
                    isLoading = isLoading,
                    importProgress = importProgress,
                    draggingFile = draggingFile,
                    dragPosition = dragPosition,
                    dragOffset = dragOffset,
                    dragItemSize = dragItemSize,
                    showSheet = showSheet,
                    pendingFileName = pendingFileName,
                    hasProgress = (practiceProgress[pendingFileName] ?: 0) > 0,
                    bottomNavIndex = navPrefs.bottomNavIndex,
                    onDismissSheet = { showSheet = false },
                    onStartQuiz = onStartQuiz,
                    onStartAdaptive = onStartAdaptive,
                    onStartExam = onStartExam,
                    onRestartQuiz = { fileName ->
                        viewModel.clearProgressForFile(fileName)
                    },
                    showDeleteDialog = showDeleteDialog,
                    fileToDelete = fileToDelete,
                    onDismissDeleteFile = { showDeleteDialog = false },
                    onConfirmDeleteFile = {
                        showDeleteDialog = false
                        viewModel.deleteFileAndData(fileToDelete)
                    },
                    showAddFolderDialog = showAddFolderDialog,
                    newFolderName = newFolderName,
                    onNewFolderNameChange = { newFolderName = it },
                    onDismissAddFolder = { showAddFolderDialog = false },
                    onConfirmAddFolder = {
                        folderViewModel.addFolder(newFolderName)
                        newFolderName = ""
                        showAddFolderDialog = false
                    },
                    renameFolderTarget = renameFolderTarget,
                    renameFolderName = renameFolderName,
                    onRenameFolderNameChange = { renameFolderName = it },
                    onDismissRenameFolder = { renameFolderTarget = null },
                    onConfirmRenameFolder = {
                        renameFolderTarget?.let { folderViewModel.renameFolder(it, renameFolderName) }
                        renameFolderTarget = null
                    },
                    showDeleteFolderDialog = showDeleteFolderDialog,
                    folderToDelete = folderToDelete,
                    onDismissDeleteFolder = { showDeleteFolderDialog = false },
                    onConfirmDeleteFolder = {
                        showDeleteDialog = false
                        folderToDelete?.let { folderViewModel.deleteFolder(it) }
                    },
                )
            },
        )
    }
}
