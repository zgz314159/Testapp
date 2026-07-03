package com.example.testapp.presentation.screen.home



import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.foundation.layout.*

import androidx.compose.material3.*

import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.geometry.Rect

import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.layout.onGloballyPositioned

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

import com.example.testapp.data.datastore.FontSettingsDataStore

import com.example.testapp.presentation.screen.file.DragDropViewModel

import com.example.testapp.presentation.screen.file.FileFolderViewModel

import com.example.testapp.presentation.screen.home.components.HomeBottomBar

import com.example.testapp.presentation.screen.home.components.HomeFileListContainer

import com.example.testapp.presentation.screen.home.components.HomeFolderRow

import com.example.testapp.presentation.screen.home.components.HomeTopBar

import com.example.testapp.presentation.screen.home.components.buildFolderFileCounts

import com.example.testapp.presentation.screen.home.components.buildRootDisplayFileNames

import com.example.testapp.presentation.screen.home.components.filterVisibleHomeFolders

import com.example.testapp.presentation.screen.home.components.reorderByRecentUsage

import com.example.testapp.presentation.screen.settings.SettingsViewModel

import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun HomeScreen(

    onStartQuiz: (quizId: String) -> Unit = {},

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

    settingsViewModel: SettingsViewModel

) {

    val viewModel: HomeViewModel = hiltViewModel()

    val folderViewModel: FileFolderViewModel = hiltViewModel()

    val dragViewModel: DragDropViewModel = hiltViewModel()

    val fileNames by viewModel.fileNames.collectAsState()

    val folders by folderViewModel.folders.collectAsState()

    val folderNames by folderViewModel.folderNames.collectAsState()

    var currentFolder by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val storedFileName by FontSettingsDataStore

        .getLastSelectedFile(context)

        .collectAsState(initial = "")

    val recentFileNames by FontSettingsDataStore

        .getRecentSelectedFiles(context)

        .collectAsState(initial = emptyList())

    val selectedFileName = remember { mutableStateOf("") }



    val isLoading by settingsViewModel.isLoading.collectAsState()

    val importProgress by settingsViewModel.progress.collectAsState()

    val fileStatistics by viewModel.fileStatistics.collectAsState()

    val practiceProgress by viewModel.practiceProgress.collectAsState()

    val homeContentReady by viewModel.homeContentReady.collectAsState()

    val homeDropTargetKey = "__HOME_ROOT__"

    val rootDisplayFileNames = remember(fileNames, folders, storedFileName, recentFileNames) {

        buildRootDisplayFileNames(

            allFileNames = fileNames,

            rootVisibleFileNames = fileNames.filter { folders[it] == null },

            primaryFileName = storedFileName,

            recentFileNames = recentFileNames

        )

    }

    val currentFolderDisplayFileNames = remember(fileNames, folders, currentFolder, storedFileName, recentFileNames) {

        currentFolder?.let { folderName ->

            reorderByRecentUsage(

                visibleFileNames = fileNames.filter { folders[it] == folderName },

                primaryFileName = storedFileName,

                recentFileNames = recentFileNames

            )

        } ?: emptyList()

    }

    val folderFileCounts = remember(fileNames, folders, folderNames) {

        buildFolderFileCounts(fileNames, folders, folderNames)

    }

    val visibleHomeFolders = remember(folderNames, folderFileCounts) {

        filterVisibleHomeFolders(folderNames.distinct(), folderFileCounts)

    }

    val currentFolderFileNames = remember(currentFolderDisplayFileNames) {

        currentFolderDisplayFileNames.toSet()

    }

    val displayFileNames = if (currentFolder == null) rootDisplayFileNames else currentFolderDisplayFileNames

    val displayFolders = if (currentFolder == null) visibleHomeFolders else emptyList()

    val homeLibraryEmptyReason = remember(currentFolder, displayFileNames, displayFolders, fileNames) {
        resolveHomeLibraryEmpty(
            currentFolder = currentFolder,
            displayFileNames = displayFileNames,
            visibleFolders = displayFolders,
            totalFileCount = fileNames.size
        )
    }

    LaunchedEffect(storedFileName, fileNames) {

        if (selectedFileName.value.isNotBlank() && selectedFileName.value in fileNames) {

            return@LaunchedEffect

        }

        if (storedFileName.isNotBlank() && storedFileName in fileNames) {

            selectedFileName.value = storedFileName

        } else if (fileNames.isNotEmpty()) {

            selectedFileName.value = fileNames.first()

        }

    }



    val storedNavIndex by FontSettingsDataStore

        .getLastSelectedNav(context)

        .collectAsState(initial = 3)

    var bottomNavIndex by remember { mutableStateOf(storedNavIndex) }



    LaunchedEffect(storedNavIndex) {

        bottomNavIndex = storedNavIndex

    }



    LaunchedEffect(bottomNavIndex) {

        FontSettingsDataStore.setLastSelectedNav(context, bottomNavIndex)

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



    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val folderBounds = remember { mutableStateMapOf<String, Rect>() }

    val fileCardBounds = remember { mutableStateMapOf<String, Rect>() }

    val dragPosition by dragViewModel.dragPosition.collectAsState()

    val draggingFile by dragViewModel.draggingFile.collectAsState()

    val dragItemSize by dragViewModel.dragItemSize.collectAsState()

    val dragOffset by dragViewModel.offsetWithinItem.collectAsState()

    val hoverFolder by dragViewModel.hoverFolder.collectAsState()

    val hoverFile by dragViewModel.hoverFile.collectAsState()

    val scope = rememberCoroutineScope()

    val persistFileUsage: (String) -> Unit = remember(context, scope) {

        { fileName ->

            scope.launch {

                FontSettingsDataStore.markFileAsRecent(context, fileName)

                FontSettingsDataStore.setLastSelectedFile(context, fileName)

            }

        }

    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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

        currentFolderFileNames = currentFolderFileNames

    )

    LaunchedEffect(rootDisplayFileNames, visibleHomeFolders, currentFolderDisplayFileNames, currentFolder) {

        val validFileKeys = if (currentFolder == null) {

            rootDisplayFileNames.toSet()

        } else {

            currentFolderDisplayFileNames.toSet()

        }

        val validFolderKeys = if (currentFolder == null) visibleHomeFolders.toSet() else emptySet()

        fileCardBounds.keys.retainAll(validFileKeys)

        val allowedFolderKeys = buildSet {

            addAll(validFolderKeys)

            if (currentFolder != null) add(homeDropTargetKey)

        }

        folderBounds.keys.retainAll(allowedFolderKeys)

    }



    HomeNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            HomeDrawerContent(

                fileNames = fileNames,

                folders = folders,

                folderNames = folderNames,

                fileStatistics = fileStatistics,

                onQuestionSelected = { fileName, _ ->

                    onStartQuiz(fileName)

                },

                onClose = { scope.launch { drawerState.close() } }

            )

        }

    ) {

        Scaffold(

            modifier = if (draggingFile == null) {
                Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            } else {
                Modifier
            },

            topBar = {

                HomeTopBar(

                    scrollBehavior = scrollBehavior,

                    drawerState = drawerState,

                    onSettings = onSettings

                )

            },

            bottomBar = {

                HomeBottomBar(

                    bottomNavIndex = bottomNavIndex,

                    onNavChange = { bottomNavIndex = it },

                    onWrongBook = { onWrongBook("") },

                    onFavoriteBook = { onFavoriteBook("") },

                    onHistory = onHistory

                )

            }

        ) { innerPadding ->

            Box(

                Modifier

                    .fillMaxSize()

                    .padding(innerPadding)

                    .onGloballyPositioned { homeRootCoordsRef.value = it }

                    .then(
                        if (homeInteractionReady && draggingFile == null && !drawerOpen) {
                            homeRootDragModifier
                        } else {
                            Modifier
                        }
                    )

                    .then(
                        if (homeInteractionReady && !drawerOpen) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(onLongPress = { showAddFolderDialog = true })
                            }
                        } else {
                            Modifier
                        }
                    )

            ) {

                Column(

                    modifier = Modifier.fillMaxSize(),

                    horizontalAlignment = Alignment.CenterHorizontally

                ) {

                    HomeFolderRow(

                        currentFolder = currentFolder,

                        folderNames = emptyList(),

                        hoverFolder = hoverFolder,

                        showFolderList = false,

                        showBackAction = true,

                        onBackFolder = { currentFolder = null },

                        onFolderClick = { currentFolder = it },

                        onFolderLongPress = {

                            renameFolderTarget = it

                            renameFolderName = it

                        }

                    )



                    if (homeContentReady && homeLibraryEmptyReason != null) {
                        HomeEmptyLibraryPanel(
                            reason = homeLibraryEmptyReason,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(bottom = 8.dp)
                        )
                    } else {
                        HomeFileListContainer(

                        viewModel = viewModel,

                        visibleFolders = displayFolders,

                        folderFileCounts = folderFileCounts,

                        displayFileNames = displayFileNames,

                        folders = folders,

                        enableItemGestures = homeContentReady,

                        selectedFileName = selectedFileName.value,

                        draggingFile = draggingFile,

                        dragPosition = dragPosition,

                        hoverFolder = hoverFolder,

                        hoverFile = hoverFile,

                        showFilesFirst = true,

                        onFolderClick = { currentFolder = it },

                        onFolderLongPress = {

                            renameFolderTarget = it

                            renameFolderName = it

                        },

                        onDeleteFolderClick = {

                            folderToDelete = it

                            showDeleteFolderDialog = true

                        },

                        onCardClick = { fileName ->

                            when (bottomNavIndex) {

                                2 -> {

                                    selectedFileName.value = fileName

                                    persistFileUsage(fileName)

                                    onViewResult(fileName)

                                }

                                else -> {

                                    if (selectedFileName.value == fileName) {

                                        pendingFileName = fileName

                                        viewModel.preloadQuestionFile(fileName)

                                        showSheet = true

                                    } else {

                                        selectedFileName.value = fileName

                                        viewModel.preloadQuestionFile(fileName)

                                    }

                                }

                            }

                        },

                        onDeleteClick = { fileName ->

                            fileToDelete = fileName

                            showDeleteDialog = true

                        },

                        onDoubleClick = { fileName ->

                            selectedFileName.value = fileName

                            persistFileUsage(fileName)

                            onViewQuestionDetail(fileName)

                        },

                        onDragStart = { fileName, position, size, offset ->

                            dragFinishRef.handledByCard = false

                            dragViewModel.startDragging(fileName, position, size, offset)

                        },

                        onDragUpdate = { position ->

                            updateHomeDragHover(

                                dragViewModel,

                                folderBounds,

                                fileCardBounds,

                                position,

                                currentFolder,

                                homeDropTargetKey,

                                currentFolderFileNames

                            )

                        },

                        onDragEnd = { fileName ->

                            dragFinishRef.handledByCard = true

                            finishActiveDrag(

                                fileName,

                                dragViewModel,

                                folderViewModel,

                                folderBounds,

                                fileCardBounds,

                                scope,

                                "card-onDragEnd",

                                currentFolder,

                                homeDropTargetKey,

                                currentFolderFileNames

                            )

                        },

                        onDragCancel = { _ ->

                        },

                        onReportFolderBounds = { name, rect -> folderBounds[name] = rect },

                        onReportCardBounds = { name, rect -> fileCardBounds[name] = rect },

                        modifier = Modifier

                            .fillMaxWidth()

                            .weight(1f)

                            .padding(bottom = 8.dp)

                        )
                    }

                }



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

                    bottomNavIndex = bottomNavIndex,

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

                    }

                )

            }

        }

    }

}


