package com.example.testapp.presentation.screen.wrongbook

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
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.presentation.screen.file.DragDropViewModel
import com.example.testapp.presentation.screen.file.FileFolderViewModel
import com.example.testapp.presentation.screen.library.ScopedQuestionLibraryScreen
import com.example.testapp.uicommon.component.LocalFontFamily
import com.example.testapp.uicommon.component.LocalFontSize
import com.example.testapp.uicommon.util.buildFileStatisticsForQuestions

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
    val wrongQuestions by viewModel.wrongQuestions.collectAsState()
    val fileNames by viewModel.fileNames.collectAsState()

    if (!fileName.isNullOrEmpty()) {
        WrongBookDetailContent(
            fileName = fileName,
            wrongQuestions = wrongQuestions.filter { it.question.fileName == fileName },
            navController = navController
        )
        return
    }

    val groupedWrongs = remember(wrongQuestions) {
        wrongQuestions.groupBy { it.question.fileName.orEmpty() }.filterKeys { it.isNotBlank() }
    }
    val fileStatistics = remember(groupedWrongs) {
        groupedWrongs.mapValues { (_, list) ->
            buildFileStatisticsForQuestions(
                questions = list.map { it.question },
                wrongCount = list.size,
                favoriteCount = 0
            )
        }
    }

    ScopedQuestionLibraryScreen(
        scope = WRONGBOOK_GROUP_SCOPE,
        homeDropTargetKey = WRONGBOOK_HOME_DROP_TARGET,
        rootTitleRes = R.string.wrongbook_title,
        emptyMessageRes = R.string.no_wrong,
        fileNames = fileNames,
        fileStatistics = fileStatistics,
        folderViewModel = folderViewModel,
        dragViewModel = dragViewModel,
        onDeleteFile = viewModel::removeByFileName,
        onOpenFile = { name ->
            val encoded = java.net.URLEncoder.encode(name, "UTF-8")
            navController?.navigate("practice_wrongbook/$encoded")
        }
    )
}

@Composable
private fun WrongBookDetailContent(
    fileName: String,
    wrongQuestions: List<WrongQuestion>,
    navController: NavController?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (wrongQuestions.isEmpty()) {
            Text(
                text = stringResource(R.string.no_wrong),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            return@Column
        }

        wrongQuestions.forEachIndexed { index, wrong ->
            val selectedOptions = wrong.selected.joinToString("，") { optionIndex ->
                wrong.question.options.getOrNull(optionIndex).orEmpty()
            }
            Text(
                text = "${index + 1}. ${wrong.question.content} (${stringResource(R.string.your_answer_prefix)}$selectedOptions)",
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val encoded = java.net.URLEncoder.encode(fileName, "UTF-8")
                navController?.navigate("practice_wrongbook/$encoded")
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(R.string.redo_wrong),
                fontSize = LocalFontSize.current,
                fontFamily = LocalFontFamily.current
            )
        }
    }
}
