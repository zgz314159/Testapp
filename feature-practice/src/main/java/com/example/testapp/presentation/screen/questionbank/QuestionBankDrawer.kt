package com.example.testapp.presentation.screen.questionbank

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.presentation.screen.home.design.HomeDesignTokens
import com.example.testapp.uicommon.design.AppLoadingIndicator
import com.example.testapp.uicommon.screen.questionbank.resolveQuestionBankDrawerWidth
import kotlin.math.abs

// ---- Tree model ----

private data class QuestionBankTree(
    val rootFiles: List<String>,
    val filesByFolder: Map<String, List<String>>
)

private fun buildQuestionBankTree(
    fileNames: List<String>,
    folders: Map<String, String?>,
    folderNames: List<String>
): QuestionBankTree {
    val rootFiles = mutableListOf<String>()
    val buckets = linkedMapOf<String, MutableList<String>>()
    folderNames.distinct().forEach { folderName ->
        buckets[folderName] = mutableListOf()
    }
    fileNames.forEach { fileName ->
        val folderName = folders[fileName]?.takeIf { it.isNotBlank() }
        if (folderName == null) {
            rootFiles += fileName
        } else {
            buckets.getOrPut(folderName) { mutableListOf() } += fileName
        }
    }
    return QuestionBankTree(
        rootFiles = rootFiles,
        filesByFolder = buckets.mapValues { it.value.toList() }
    )
}

// ---- Question items scope ----

private fun androidx.compose.foundation.lazy.LazyListScope.QuestionBankQuestionItems(
    fileName: String,
    query: String,
    isSearchMode: Boolean,
    isExpanded: Boolean,
    questions: List<com.example.testapp.domain.model.Question>,
    matches: List<QuestionBankQuestionMatch>,
    onQuestionSelected: (fileName: String, questionId: Int, searchQuery: String) -> Unit,
    onQuestionEdit: (fileName: String, questionId: Int, searchQuery: String) -> Unit,
    indent: Int = 16
) {
    if (!isExpanded) return

    if (isSearchMode) {
        matches.forEach { match ->
            item(key = "search_question_${fileName}_${match.question.id}") {
                QuestionBankSearchQuestionRow(
                    question = match.question,
                    query = query,
                    indent = indent,
                    onClick = { onQuestionSelected(fileName, match.question.id, query) },
                    onLongClick = { onQuestionEdit(fileName, match.question.id, query) },
                )
            }
        }
    } else {
        questions.forEachIndexed { index, question ->
            item(key = "question_${fileName}_${question.id}_$index") {
                QuestionBankQuestionRow(
                    question = question,
                    text = question.content.firstDisplayLine(index),
                    questionIndex = index,
                    query = query,
                    indent = indent,
                    onClick = { onQuestionSelected(fileName, question.id, query) },
                    onLongClick = { onQuestionEdit(fileName, question.id, query) },
                )
            }
        }
    }
}

// ---- Main composable ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBankDrawer(
    fileNames: List<String>,
    folders: Map<String, String?>,
    folderNames: List<String>,
    fileStatistics: Map<String, FileStatistics>,
    onQuestionSelected: (fileName: String, questionId: Int, searchQuery: String) -> Unit,
    onQuestionEdit: (fileName: String, questionId: Int, searchQuery: String) -> Unit = { _, _, _ -> },
    onClose: () -> Unit,
    viewModel: QuestionBankDrawerViewModel,
    modifier: Modifier = Modifier,
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val questionsByFile by viewModel.questionsByFile.collectAsState()
    val loadingFiles by viewModel.loadingFiles.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val expansion by viewModel.expansionSnapshot.collectAsState()

    val query = searchQuery.trim()
    val isSearchMode = expansion.isSearchMode
    val tree = remember(fileNames, folders, folderNames) { buildQuestionBankTree(fileNames, folders, folderNames) }
    val fileNameMatches = remember(fileNames, query) {
        if (query.isBlank()) emptySet() else fileNames.filter { it.contains(query, ignoreCase = true) }.toSet()
    }
    val folderNameMatches = remember(folderNames, query) {
        if (query.isBlank()) emptySet() else folderNames.filter { it.contains(query, ignoreCase = true) }.toSet()
    }
    val contentMatchFiles = remember(searchState.resultsByFile) { searchState.resultsByFile.keys }
    val searchVisibleFiles = remember(fileNameMatches, contentMatchFiles) { fileNameMatches + contentMatchFiles }
    val totalCountByFile = remember(fileStatistics, questionsByFile) {
        buildMap {
            fileStatistics.forEach { (fileName, statistics) ->
                put(fileName, statistics.questionCount)
            }
            questionsByFile.forEach { (fileName, questions) ->
                if (fileName !in this) put(fileName, questions.size)
            }
        }
    }
    val listState = rememberLazyListState()
    val drawerWidth = resolveQuestionBankDrawerWidth(LocalConfiguration.current.screenWidthDp)

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .pointerInput(searchQuery) {
                var totalHorizontal = 0f
                detectHorizontalDragGestures(
                    onDragStart = { totalHorizontal = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalHorizontal += dragAmount
                    },
                    onDragEnd = {
                        if (abs(totalHorizontal) > 120f) {
                            if (searchQuery.isNotBlank()) {
                                viewModel.clearSearch()
                            } else {
                                onClose()
                            }
                        }
                    }
                )
            },
        drawerContainerColor = HomeDesignTokens.backgroundLight,
        drawerContentColor = HomeDesignTokens.textPrimaryLight,
        drawerTonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    start = HomeDesignTokens.spacingMd,
                    end = HomeDesignTokens.spacingMd,
                    top = HomeDesignTokens.spacingSm,
                    bottom = HomeDesignTokens.spacingMd,
                )
        ) {
            QuestionBankDrawerHeader(
                title = "题库浏览",
                closeContentDescription = "关闭题库抽屉",
                onClose = onClose
            )
            Spacer(Modifier.height(HomeDesignTokens.spacingSm))
            QuestionBankSearchBar(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it, fileNames) },
                onClear = viewModel::clearSearch,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(HomeDesignTokens.spacingMd))
            HorizontalDivider(
                color = HomeDesignTokens.outlineLight.copy(alpha = 0.45f),
                thickness = 1.dp,
            )
            Spacer(Modifier.height(HomeDesignTokens.spacingSm))
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    val visibleRootFiles = if (isSearchMode) {
                        tree.rootFiles.filter { fileName -> fileName in searchVisibleFiles }
                    } else {
                        tree.rootFiles
                    }
                    visibleRootFiles.forEach { fileName ->
                        item(key = "root_file_$fileName") {
                            QuestionBankFileRow(
                                fileName = fileName,
                                query = query,
                                isExpanded = expansion.isFileExpanded(fileName),
                                isLoading = fileName in loadingFiles,
                                matchCount = searchState.matchCountByFile[fileName],
                                totalCount = totalCountByFile[fileName],
                                onClick = { viewModel.toggleFile(fileName) }
                            )
                        }
                        QuestionBankQuestionItems(
                            fileName = fileName,
                            query = query,
                            isSearchMode = isSearchMode,
                            isExpanded = expansion.isFileExpanded(fileName),
                            questions = questionsByFile[fileName].orEmpty(),
                            matches = searchState.resultsByFile[fileName].orEmpty(),
                            onQuestionSelected = onQuestionSelected,
                            onQuestionEdit = onQuestionEdit,
                        )
                    }

                    tree.filesByFolder.forEach { (folderName, folderFiles) ->
                        val visibleFolderFiles = if (isSearchMode) {
                            if (folderName in folderNameMatches) {
                                folderFiles
                            } else {
                                folderFiles.filter { fileName -> fileName in searchVisibleFiles }
                            }
                        } else {
                            folderFiles
                        }
                        if (visibleFolderFiles.isNotEmpty()) {
                            val isExpanded = expansion.isFolderExpanded(folderName)
                            item(key = "folder_$folderName") {
                                QuestionBankFolderRow(
                                    folderName = folderName,
                                    query = query,
                                    isExpanded = isExpanded,
                                    itemCount = visibleFolderFiles.size,
                                    onClick = { viewModel.toggleFolder(folderName) }
                                )
                            }
                            if (isExpanded) {
                                visibleFolderFiles.forEach { fileName ->
                                    item(key = "folder_file_${folderName}_$fileName") {
                                        QuestionBankFileRow(
                                            fileName = fileName,
                                            query = query,
                                            isExpanded = expansion.isFileExpanded(fileName),
                                            isLoading = fileName in loadingFiles,
                                            matchCount = searchState.matchCountByFile[fileName],
                                            totalCount = totalCountByFile[fileName],
                                            indent = 16,
                                            onClick = { viewModel.toggleFile(fileName) }
                                        )
                                    }
                                    QuestionBankQuestionItems(
                                        fileName = fileName,
                                        query = query,
                                        isSearchMode = isSearchMode,
                                        isExpanded = expansion.isFileExpanded(fileName),
                                        questions = questionsByFile[fileName].orEmpty(),
                                        matches = searchState.resultsByFile[fileName].orEmpty(),
                                        onQuestionSelected = onQuestionSelected,
                                        onQuestionEdit = onQuestionEdit,
                                        indent = 32
                                    )
                                }
                            }
                        }
                    }
                }

                if (searchState.isSearching) {
                    AppLoadingIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = HomeDesignTokens.spacingMd)
                            .size(28.dp)
                    )
                }
            }
        }
    }
}
