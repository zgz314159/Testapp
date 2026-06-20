package com.example.testapp.presentation.screen.questionbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QuestionBankQuestionMatch(
    val question: Question,
    val snippet: String
)

data class QuestionBankSearchState(
    val isSearching: Boolean = false,
    val resultsByFile: Map<String, List<QuestionBankQuestionMatch>> = emptyMap(),
    val matchCountByFile: Map<String, Int> = emptyMap(),
    val searchedFileCount: Int = 0
)

@HiltViewModel
class QuestionBankDrawerViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _expandedFolders = MutableStateFlow<Set<String>>(emptySet())
    val expandedFolders: StateFlow<Set<String>> = _expandedFolders.asStateFlow()

    private val _expandedFiles = MutableStateFlow<Set<String>>(emptySet())
    val expandedFiles: StateFlow<Set<String>> = _expandedFiles.asStateFlow()

    private val _questionsByFile = MutableStateFlow<Map<String, List<Question>>>(emptyMap())
    val questionsByFile: StateFlow<Map<String, List<Question>>> = _questionsByFile.asStateFlow()

    private val _loadingFiles = MutableStateFlow<Set<String>>(emptySet())
    val loadingFiles: StateFlow<Set<String>> = _loadingFiles.asStateFlow()

    private val _searchState = MutableStateFlow(QuestionBankSearchState())
    val searchState: StateFlow<QuestionBankSearchState> = _searchState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String, fileNames: List<String>) {
        _searchQuery.value = query
        searchJob?.cancel()

        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            _searchState.value = QuestionBankSearchState()
            return
        }

        searchJob = viewModelScope.launch {
            delay(320)
            if (normalizedQuery.length < 2) {
                _searchState.value = QuestionBankSearchState()
                return@launch
            }

            _searchState.value = QuestionBankSearchState(isSearching = true)
            val previewResultsByFile = linkedMapOf<String, List<QuestionBankQuestionMatch>>()
            val matchCountByFile = linkedMapOf<String, Int>()

            fileNames.forEachIndexed { index, fileName ->
                ensureActive()
                val questions = withContext(Dispatchers.IO) {
                    getQuestionsUseCase(fileName).firstOrNull().orEmpty()
                }
                val result = withContext(Dispatchers.Default) {
                    buildSearchResultForFile(questions, normalizedQuery)
                }
                if (result.matchCount > 0) {
                    matchCountByFile[fileName] = result.matchCount
                    previewResultsByFile[fileName] = result.previewMatches
                }

                if ((index + 1) % SEARCH_PUBLISH_BATCH_SIZE == 0 || index == fileNames.lastIndex) {
                    _searchState.value = QuestionBankSearchState(
                        isSearching = index != fileNames.lastIndex,
                        resultsByFile = previewResultsByFile.toMap(),
                        matchCountByFile = matchCountByFile.toMap(),
                        searchedFileCount = index + 1
                    )
                    delay(1)
                }
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _searchState.value = QuestionBankSearchState()
    }

    fun toggleFolder(folderName: String) {
        _expandedFolders.update { current ->
            if (folderName in current) current - folderName else current + folderName
        }
    }

    fun toggleFile(fileName: String) {
        val willExpand = fileName !in _expandedFiles.value
        _expandedFiles.update { current ->
            if (fileName in current) current - fileName else current + fileName
        }
        if (willExpand) {
            loadQuestionsForFile(fileName)
        }
    }

    fun loadQuestionsForFile(fileName: String) {
        if (_questionsByFile.value.containsKey(fileName) || fileName in _loadingFiles.value) return

        viewModelScope.launch {
            _loadingFiles.update { it + fileName }
            val questions = withContext(Dispatchers.IO) {
                getQuestionsUseCase(fileName).firstOrNull().orEmpty()
            }
            _questionsByFile.update { it + (fileName to questions) }
            _loadingFiles.update { it - fileName }
        }
    }

    private data class FileSearchResult(
        val matchCount: Int,
        val previewMatches: List<QuestionBankQuestionMatch>
    )

    private fun buildSearchResultForFile(
        questions: List<Question>,
        query: String
    ): FileSearchResult {
        val loweredQuery = query.lowercase()
        var matchCount = 0
        val previewMatches = mutableListOf<QuestionBankQuestionMatch>()
        questions.forEach { question ->
            val searchableFields = buildList {
                add(question.content)
                addAll(question.options)
                add(question.answer)
                add(question.explanation)
            }
            val matchedText = searchableFields.firstOrNull { field ->
                field.contains(loweredQuery, ignoreCase = true)
            } ?: return@forEach
            matchCount += 1
            previewMatches += QuestionBankQuestionMatch(
                question = question,
                snippet = matchedText.toSearchSnippet(query)
            )
        }
        return FileSearchResult(
            matchCount = matchCount,
            previewMatches = previewMatches
        )
    }

    private fun String.toSearchSnippet(query: String): String {
        val matchIndex = indexOf(query, ignoreCase = true)
        if (matchIndex < 0) return take(MAX_SNIPPET_LENGTH)

        val start = (matchIndex - SNIPPET_CONTEXT).coerceAtLeast(0)
        val end = (matchIndex + query.length + SNIPPET_CONTEXT).coerceAtMost(length)
        val prefix = if (start > 0) "..." else ""
        val suffix = if (end < length) "..." else ""
        return prefix + substring(start, end) + suffix
    }

    private companion object {
        const val SEARCH_PUBLISH_BATCH_SIZE = 4
        const val MAX_SNIPPET_LENGTH = 96
        const val SNIPPET_CONTEXT = 42
    }
}
