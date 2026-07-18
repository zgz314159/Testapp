package com.example.testapp.presentation.screen.favorite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.testapp.feature.practice.R
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import com.example.testapp.presentation.screen.library.LibraryQuestionDetailScreen
import com.example.testapp.presentation.screen.library.LibraryStartQuizSheet
import com.example.testapp.presentation.screen.library.ScopedQuestionLibraryScreen

private const val FAVORITE_GROUP_SCOPE = "__favorite__"
private const val FAVORITE_HOME_DROP_TARGET = "__FAVORITE_HOME__"

@Composable
fun FavoriteScreen(
    fileName: String? = null,
    viewModel: FavoriteViewModel = hiltViewModel(),
    folderViewModel: FileFolderViewModel = hiltViewModel(),
    dragViewModel: DragDropViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onOpenFile: (String) -> Unit = {},
    onStartFavoriteQuiz: (String) -> Unit = {},
    onStartFavoriteExam: (String) -> Unit = {},
) {
    val libraryCatalog by viewModel.libraryCatalog.collectAsState()
    val fileNames by viewModel.fileNames.collectAsState()
    val favoriteQuestions by viewModel.favoriteQuestions.collectAsState()
    val scopedProgress by viewModel.scopedPracticeProgress.collectAsState()

    if (!fileName.isNullOrEmpty()) {
        LaunchedEffect(fileName) { viewModel.ensureFullListLoaded() }
        val items = favoriteQuestions
            .filter { it.question.fileName == fileName }
            .mapIndexed { index, favorite ->
                "${index + 1}. ${favorite.question.content}"
            }
        LibraryQuestionDetailScreen(
            title = "${stringResource(R.string.favorites)} - $fileName",
            emptyMessage = stringResource(R.string.no_favorites),
            items = items,
            actionLabel = stringResource(R.string.practice_file_favorites),
            onBack = onBack,
            onAction = { onStartFavoriteQuiz(fileName) },
        )
        return
    }

    var showSheet by remember { mutableStateOf(false) }
    var pendingFileName by remember { mutableStateOf("") }
    val openSheet: (String) -> Unit = { name ->
        pendingFileName = name
        showSheet = true
    }

    ScopedQuestionLibraryScreen(
        scope = FAVORITE_GROUP_SCOPE,
        homeDropTargetKey = FAVORITE_HOME_DROP_TARGET,
        rootTitleRes = R.string.favorites,
        emptyMessageRes = R.string.no_favorites,
        fileNames = fileNames,
        fileStatistics = libraryCatalog.fileStatistics,
        folderViewModel = folderViewModel,
        dragViewModel = dragViewModel,
        onDeleteFile = viewModel::removeByFileName,
        onOpenFile = openSheet,
        onFileCtaClick = openSheet,
        practiceProgress = scopedProgress,
    )

    LibraryStartQuizSheet(
        visible = showSheet,
        pendingFileName = pendingFileName,
        hasProgress = (scopedProgress[pendingFileName] ?: 0) > 0,
        onDismiss = { showSheet = false },
        onStartPractice = onStartFavoriteQuiz,
        onStartExam = onStartFavoriteExam,
        onRestart = { name ->
            viewModel.clearScopedProgress(name)
        },
    )
}
