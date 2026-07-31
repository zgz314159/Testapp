package com.example.testapp.presentation.screen.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.components.HomeBottomBar
import com.example.testapp.presentation.screen.questionbank.QuestionBankDrawerViewModel
import com.example.testapp.uicommon.screen.questionbank.resolveQuestionBankDrawerWidth
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
    homeRootCoordsRef: HomeRootCoordsRef,
    homeRootDragModifier: Modifier,
    /** 根节点空白区长按；入参为相对 Scaffold 内容区的坐标。 */
    onBlankAreaLongPress: (Offset) -> Unit,
    librarySection: @Composable () -> Unit,
    overlays: @Composable () -> Unit,
) {
    // 根 pointerInput 必须始终挂着：用 draggingFile 条件拆除会在拖起瞬间改父树，
    // 连带取消卡片上的 detectDragGesturesAfterLongPress（长按闪一下就消失的根因）。
    // Interaction-ready 翻转限制在 Scaffold 子树，避免整棵 Home 重组。
    val homeInteractionReady = rememberHomeInteractionReady()
    val blankLongPressEnabled = homeInteractionReady && draggingFile == null && !drawerOpen
    val currentBlankLongPressEnabled = rememberUpdatedState(blankLongPressEnabled)
    val currentOnBlankAreaLongPress = rememberUpdatedState(onBlankAreaLongPress)
    val rootPointerReady = homeInteractionReady && !drawerOpen

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
                .then(if (rootPointerReady) homeRootDragModifier else Modifier)
                .then(
                    if (rootPointerReady) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { offset ->
                                    if (!currentBlankLongPressEnabled.value) return@detectTapGestures
                                    currentOnBlankAreaLongPress.value(offset)
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
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
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(fileNames) {
        val restore = HomeDrawerRestoreHolder.pending
        HomeDrawerDebugLog.d(
            "restore LaunchedEffect fileNames=${fileNames.size} pending=$restore",
        )
        if (restore == null) return@LaunchedEffect
        HomeDrawerRestoreHolder.pending = null
        if (restore.openDrawer) {
            HomeDrawerDebugLog.open("HomeDrawerRestoreHolder", drawerState)
            drawerState.open()
        }
        if (restore.searchQuery.isNotBlank()) {
            drawerViewModel.onSearchQueryChange(restore.searchQuery, fileNames)
        }
    }

    HomeNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // 关闭后若卸载抽屉内容，ModalNavigationDrawer measure 会 updateAnchors/trySnapTo
            // 把状态又 snap 回 Open（log：CONFIRM Open from NavigationDrawer.kt:424）。
            // 策略：首次打开前放同宽占位稳住 anchors；打开过后保持内容挂载，关闭不再卸载。
            val drawerWidth = resolveQuestionBankDrawerWidth(LocalConfiguration.current.screenWidthDp)
            val drawerActive by remember {
                derivedStateOf {
                    drawerState.currentValue != DrawerValue.Closed ||
                        drawerState.targetValue != DrawerValue.Closed
                }
            }
            var contentMounted by remember { mutableStateOf(false) }
            LaunchedEffect(drawerActive) {
                if (drawerActive) {
                    contentMounted = true
                    HomeDrawerDebugLog.d("drawerContent mounted (active)")
                }
            }
            if (contentMounted) {
                HomeDrawerContent(
                    fileNames = fileNames,
                    folders = folders,
                    folderNames = folderNames,
                    fileStatistics = fileStatistics,
                    drawerViewModel = drawerViewModel,
                    onQuestionSelected = { fileName, questionId, searchQuery ->
                        HomeDrawerDebugLog.d("onQuestionSelected file=$fileName id=$questionId")
                        HomeDrawerBrowseNavigationPipeline.captureRestoreBeforeBrowse(searchQuery)
                        HomeDrawerDebugLog.close("onQuestionSelected", drawerState)
                        scope.launch { drawerState.close() }
                        onBrowseQuestion(fileName, questionId)
                    },
                    onQuestionEdit = { fileName, questionId, searchQuery ->
                        HomeDrawerDebugLog.d("onQuestionEdit file=$fileName id=$questionId")
                        HomeDrawerBrowseNavigationPipeline.captureRestoreBeforeBrowse(searchQuery)
                        HomeDrawerDebugLog.close("onQuestionEdit", drawerState)
                        scope.launch { drawerState.close() }
                        onEditQuestion(fileName, questionId)
                    },
                    onClose = {
                        HomeDrawerDebugLog.close("QuestionBankDrawer.onClose", drawerState)
                        scope.launch { drawerState.close() }
                    },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(drawerWidth),
                )
            }
        },
        gesturesEnabled = gesturesEnabled,
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
