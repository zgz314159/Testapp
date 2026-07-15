package com.example.testapp.presentation.screen.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.components.HomeBottomBar
import com.example.testapp.presentation.screen.questionbank.QuestionBankDrawerViewModel
import kotlinx.coroutines.launch

/**
 * 首页 Scaffold 容器。
 * librarySection 内部使用统一 LazyColumn 渲染 Header + 题库列表。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenScaffoldContent(
    onSettings: () -> Unit,
    bottomNavIndex: Int,
    onNavChange: (Int) -> Unit,
    onWrongBook: () -> Unit,
    onFavoriteBook: () -> Unit,
    onHistory: () -> Unit,
    draggingFile: String?,
    drawerOpen: Boolean,
    homeInteractionReady: Boolean,
    homeRootCoordsRef: HomeRootCoordsRef,
    homeRootDragModifier: Modifier,
    onLongPressAddFolder: () -> Unit,
    librarySection: @Composable () -> Unit,
    overlays: @Composable () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HomeBottomBar(
                bottomNavIndex = bottomNavIndex,
                onNavChange = onNavChange,
                onWrongBook = onWrongBook,
                onFavoriteBook = onFavoriteBook,
                onHistory = onHistory,
                onSettings = onSettings,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
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
                            detectTapGestures(onLongPress = { onLongPressAddFolder() })
                        }
                    } else {
                        Modifier
                    }
                ),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    librarySection()
                }
            }
            overlays()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenDrawerHost(
    fileNames: List<String>,
    folders: Map<String, String?>,
    folderNames: List<String>,
    fileStatistics: Map<String, FileStatistics>,
    drawerState: DrawerState,
    drawerViewModel: QuestionBankDrawerViewModel,
    onBrowseQuestion: (fileName: String, questionId: Int) -> Unit,
    onEditQuestion: (fileName: String, questionId: Int) -> Unit = { _, _ -> },
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(fileNames) {
        val restore = HomeDrawerRestoreHolder.pending ?: return@LaunchedEffect
        HomeDrawerRestoreHolder.pending = null
        if (restore.openDrawer) {
            drawerState.open()
        }
        if (restore.searchQuery.isNotBlank()) {
            drawerViewModel.onSearchQueryChange(restore.searchQuery, fileNames)
        }
    }

    HomeNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                fileNames = fileNames,
                folders = folders,
                folderNames = folderNames,
                fileStatistics = fileStatistics,
                drawerViewModel = drawerViewModel,
                onQuestionSelected = { fileName, questionId, searchQuery ->
                    HomeDrawerBrowseNavigationPipeline.captureRestoreBeforeBrowse(searchQuery)
                    scope.launch { drawerState.close() }
                    onBrowseQuestion(fileName, questionId)
                },
                onQuestionEdit = { fileName, questionId, searchQuery ->
                    HomeDrawerBrowseNavigationPipeline.captureRestoreBeforeBrowse(searchQuery)
                    scope.launch { drawerState.close() }
                    onEditQuestion(fileName, questionId)
                },
                onClose = { scope.launch { drawerState.close() } },
            )
        },
        content = content,
    )
}

fun pruneHomeDragBounds(
    rootDisplayFileNames: List<String>,
    currentFolderDisplayFileNames: List<String>,
    visibleHomeFolders: List<String>,
    currentFolder: String?,
    homeDropTargetKey: String,
    fileCardBounds: SnapshotStateMap<String, Rect>,
    folderBounds: SnapshotStateMap<String, Rect>,
) {
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
