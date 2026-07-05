package com.example.testapp.presentation.screen.home

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import com.example.testapp.data.datastore.FontSettingsDataStore
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import com.example.testapp.presentation.screen.home.components.HomeScreenLibrarySection
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
    onBrowseQuestion: (fileName: String, questionId: Int) -> Unit = { fileName, _ -> onStartQuiz(fileName) },
    onEditQuestion: (fileName: String, questionId: Int) -> Unit = { _, _ -> },
    onStartExam: (quizId: String) -> Unit = {},
    onSettings: () -> Unit = {},
    onViewQuestionDetail: (quizId: String) -> Unit = {},
    onWrongBook: (fileName: String) -> Unit = {},
    onFavoriteBook: (fileName: String) -> Unit = {},
    onViewResult: (fileName: String) -> Unit = {},
    onStartWrongBookQuiz: (fileName: String) -> Unit = {},
    onStartWrongBookExam: (fileName: String) -> Unit = {},
    onStartFavoriteQuiz: (fileName: String) -> Unit = {},
    onStartFavoriteExam: (fileName: String) -> Unit = {},
    onHistory: () -> Unit = {},
) {
    val fileNames by viewModel.fileNames.collectAsState()
    val folders by folderViewModel.folders.collectAsState()
    val folderNames by folderViewModel.folderNames.collectAsState()
    val fileStatistics by viewModel.fileStatistics.collectAsState()
    val homeContentReady by viewModel.homeContentReady.collectAsState()
    val isLoading by settingsViewModel.isLoading.collectAsState()
    val importProgress by settingsViewModel.progress.collectAsState()

    var currentFolder by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectedFileName = remember { mutableStateOf("") }

    val storedFileName by FontSettingsDataStore
        .getLastSelectedFile(context)
        .collectAsState(initial = "")
    val recentFileNames by FontSettingsDataStore
        .getRecentSelectedFiles(context)
        .collectAsState(initial = emptyList())

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

    val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior()
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
            scrollBehavior = scrollBehavior,
            drawerState = drawerState,
            onSettings = onSettings,
            bottomNavIndex = navPrefs.bottomNavIndex,
            onNavChange = navPrefs.setBottomNavIndex,
            onWrongBook = { onWrongBook("") },
            onFavoriteBook = { onFavoriteBook("") },
            onHistory = onHistory,
            draggingFile = draggingFile,
            drawerOpen = drawerOpen,
            homeInteractionReady = homeInteractionReady,
            homeRootCoordsRef = homeRootCoordsRef,
            homeRootDragModifier = homeRootDragModifier,
            onLongPressAddFolder = { showAddFolderDialog = true },
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
                    bottomNavIndex = navPrefs.bottomNavIndex,
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
                    onViewResult = onViewResult,
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
                )
            },
            overlays = {
                HomeActionOverlays(
                    context = context,
                    viewModel = viewModel,
                    isLoading = isLoading,
                    importProgress = importProgress,
                    draggingFile = draggingFile,
                    folders = folders,
                    dragPosition = dragPosition,
                    dragOffset = dragOffset,
                    dragItemSize = dragItemSize,
                    showSheet = showSheet,
                    pendingFileName = pendingFileName,
                    bottomNavIndex = navPrefs.bottomNavIndex,
                    onDismissSheet = { showSheet = false },
                    onStartQuiz = onStartQuiz,
                    onStartExam = onStartExam,
                    onStartWrongBookQuiz = onStartWrongBookQuiz,
                    onStartWrongBookExam = onStartWrongBookExam,
                    onStartFavoriteQuiz = onStartFavoriteQuiz,
                    onStartFavoriteExam = onStartFavoriteExam,
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
                        showDeleteFolderDialog = false
                        folderToDelete?.let { folderViewModel.deleteFolder(it) }
                    },
                )
            },
        )
    }
}
