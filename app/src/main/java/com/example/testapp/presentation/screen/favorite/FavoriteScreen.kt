package com.example.testapp.presentation.screen.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.testapp.R
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import com.example.testapp.presentation.screen.library.ScopedQuestionLibraryScreen
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.util.buildFileStatisticsForQuestions

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
    val favoriteQuestions by viewModel.favoriteQuestions.collectAsState()
    val fileNames by viewModel.fileNames.collectAsState()

    if (!fileName.isNullOrEmpty()) {
        FavoriteDetailContent(
            fileName = fileName,
            favoriteQuestions = favoriteQuestions.filter { it.question.fileName == fileName },
            navController = navController
        )
        return
    }

    val groupedFavorites = remember(favoriteQuestions) {
        favoriteQuestions.groupBy { it.question.fileName.orEmpty() }.filterKeys { it.isNotBlank() }
    }
    val fileStatistics = remember(groupedFavorites) {
        groupedFavorites.mapValues { (_, list) ->
            buildFileStatisticsForQuestions(
                questions = list.map { it.question },
                wrongCount = 0,
                favoriteCount = list.size
            )
        }
    }

    ScopedQuestionLibraryScreen(
        scope = FAVORITE_GROUP_SCOPE,
        homeDropTargetKey = FAVORITE_HOME_DROP_TARGET,
        rootTitleRes = R.string.favorites,
        emptyMessageRes = R.string.no_favorites,
        fileNames = fileNames,
        fileStatistics = fileStatistics,
        folderViewModel = folderViewModel,
        dragViewModel = dragViewModel,
        onDeleteFile = viewModel::removeByFileName,
        onOpenFile = { name ->
            val encoded = java.net.URLEncoder.encode(name, "UTF-8")
            navController?.navigate("practice_favorite/$encoded")
        }
    )
}

@Composable
private fun FavoriteDetailContent(
    fileName: String,
    favoriteQuestions: List<FavoriteQuestion>,
    navController: NavController?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (favoriteQuestions.isEmpty()) {
            Text(
                text = stringResource(R.string.no_favorites),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            return@Column
        }

        favoriteQuestions.forEachIndexed { index, favorite ->
            Text(
                text = "${index + 1}. ${favorite.question.content}",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                navController?.navigate("practice_favorite/$encoded")
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(R.string.practice_file_favorites),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
    }
}
