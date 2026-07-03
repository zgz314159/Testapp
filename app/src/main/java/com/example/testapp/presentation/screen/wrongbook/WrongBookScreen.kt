package com.example.testapp.presentation.screen.wrongbook

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

private const val WRONGBOOK_GROUP_SCOPE = "__wrongbook__"
private const val WRONGBOOK_HOME_DROP_TARGET = "__WRONGBOOK_HOME__"

@Composable
fun WrongBookScreen(
    fileName: String? = null,
    viewModel: WrongBookViewModel = hiltViewModel(),
    navController: NavController? = null,
    folderViewModel: FileFolderViewModel = hiltViewModel(),
    dragViewModel: DragDropViewModel = hiltViewModel()
) {
    val libraryCatalog by viewModel.libraryCatalog.collectAsState()
    val fileNames by viewModel.fileNames.collectAsState()
    val wrongQuestions by viewModel.wrongQuestions.collectAsState()

    if (!fileName.isNullOrEmpty()) {
        LaunchedEffect(fileName) { viewModel.ensureFullListLoaded() }
        val yourAnswerPrefix = stringResource(R.string.your_answer_prefix)
        val items = wrongQuestions
            .filter { it.question.fileName == fileName }
            .mapIndexed { index, wrong ->
                val selectedOptions = wrong.selected.joinToString("，") { optionIndex ->
                    wrong.question.options.getOrNull(optionIndex).orEmpty()
                }
                "${index + 1}. ${wrong.question.content} ($yourAnswerPrefix$selectedOptions)"
            }
        LibraryQuestionDetailScreen(
            title = "${stringResource(R.string.wrongbook_title)} - $fileName",
            emptyMessage = stringResource(R.string.no_wrong),
            items = items,
            actionLabel = stringResource(R.string.redo_wrong),
            onBack = { navController?.popBackStack() },
            onAction = {
                val encoded = URLEncoder.encode(fileName, "UTF-8")
                navController?.navigate("practice_wrongbook/$encoded")
            }
        )
        return
    }

    ScopedQuestionLibraryScreen(
        scope = WRONGBOOK_GROUP_SCOPE,
        homeDropTargetKey = WRONGBOOK_HOME_DROP_TARGET,
        rootTitleRes = R.string.wrongbook_title,
        emptyMessageRes = R.string.no_wrong,
        fileNames = fileNames,
        fileStatistics = libraryCatalog.fileStatistics,
        folderViewModel = folderViewModel,
        dragViewModel = dragViewModel,
        onDeleteFile = viewModel::removeByFileName,
        onOpenFile = { name ->
            val encoded = URLEncoder.encode(name, "UTF-8")
            navController?.navigate("wrongbook/$encoded")
        }
    )
}
