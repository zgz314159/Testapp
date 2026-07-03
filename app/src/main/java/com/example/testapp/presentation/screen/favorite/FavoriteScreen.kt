package com.example.testapp.presentation.screen.favorite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import com.example.testapp.presentation.screen.library.LibraryQuestionDetailScreen
import com.example.testapp.presentation.screen.library.ScopedQuestionLibraryScreen
import java.net.URLEncoder

private const val FAVORITE_GROUP_SCOPE = "__favorite__"
private const val FAVORITE_HOME_DROP_TARGET = "__FAVORITE_HOME__"

@Composable
fun FavoriteScreen(
    fileName: String? = null,
    navController: NavController? = null,
    viewModel: FavoriteViewModel = hiltViewModel(),
    folderViewModel: FileFolderViewModel = hiltViewModel(),
    dragViewModel: DragDropViewModel = hiltViewModel()
) {
    val libraryCatalog by viewModel.libraryCatalog.collectAsState()
    val fileNames by viewModel.fileNames.collectAsState()
    val favoriteQuestions by viewModel.favoriteQuestions.collectAsState()

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
            onBack = { navController?.popBackStack() },
            onAction = {
                val encoded = URLEncoder.encode(fileName, "UTF-8")
                navController?.navigate("practice_favorite/$encoded")
            }
        )
        return
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
        onOpenFile = { name ->
            val encoded = URLEncoder.encode(name, "UTF-8")
            navController?.navigate("favorite/$encoded")
        }
    )
}
