package com.example.testapp.presentation.screen.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.core.common.practiceProgressBaseId
import com.example.testapp.domain.model.ExportData
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.LibraryCatalog
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.AddFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.ClearExamProgressByFileNameUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressByFileNameUseCase
import com.example.testapp.domain.usecase.ExportFavoriteUseCase
import com.example.testapp.domain.usecase.GetAllPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetFavoriteLibraryCatalogUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.ImportQuestionsUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionsByFileNameUseCase
import com.example.testapp.presentation.screen.practice.practiceProgressAnsweredCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    getFavoriteLibraryCatalogUseCase: GetFavoriteLibraryCatalogUseCase,
    getAllPracticeProgressFlowUseCase: GetAllPracticeProgressFlowUseCase,
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    private val addFavoriteQuestionUseCase: AddFavoriteQuestionUseCase,
    private val removeFavoriteQuestionUseCase: RemoveFavoriteQuestionUseCase,
    private val removeFavoriteQuestionsByFileNameUseCase: RemoveFavoriteQuestionsByFileNameUseCase,
    private val exportFavoriteUseCase: ExportFavoriteUseCase,
    private val importQuestionsUseCase: ImportQuestionsUseCase,
    private val clearPracticeProgressByFileNameUseCase: ClearPracticeProgressByFileNameUseCase,
    private val clearExamProgressByFileNameUseCase: ClearExamProgressByFileNameUseCase,
) : ViewModel() {

    companion object {
        private const val EXPORT_PREFIX = "导出成功："
        private const val EXPORT_FAILED_PREFIX = "导出失败："
        private const val IMPORT_SUCCESS = "导入成功"
        private const val IMPORT_FAILED_PREFIX = "导入失败："
        private const val EXPORT_FILENAME_PREFIX = "收藏导出"
        private const val PRACTICE_PREFIX = "practice_favorite_"
    }

    private val _libraryCatalog = MutableStateFlow(LibraryCatalog(emptyList(), emptyMap()))
    val libraryCatalog: StateFlow<LibraryCatalog> = _libraryCatalog.asStateFlow()

    val fileNames: StateFlow<List<String>> = _libraryCatalog
        .map { it.fileNames }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _favoriteQuestions = MutableStateFlow<List<FavoriteQuestion>>(emptyList())
    val favoriteQuestions: StateFlow<List<FavoriteQuestion>> = _favoriteQuestions.asStateFlow()

    private val _scopedPracticeProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val scopedPracticeProgress: StateFlow<Map<String, Int>> = _scopedPracticeProgress.asStateFlow()

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult.asStateFlow()

    private var fullListJob: Job? = null

    init {
        viewModelScope.launch {
            getFavoriteLibraryCatalogUseCase().collect { catalog ->
                _libraryCatalog.value = catalog
            }
        }
        viewModelScope.launch {
            getAllPracticeProgressFlowUseCase().collect { list ->
                _scopedPracticeProgress.value = list.mapNotNull { progress ->
                    val base = practiceProgressBaseId(progress.id)
                    if (!base.startsWith(PRACTICE_PREFIX)) return@mapNotNull null
                    val name = base.removePrefix(PRACTICE_PREFIX)
                    val count = practiceProgressAnsweredCount(progress)
                    name.takeIf { count > 0 }?.let { it to count }
                }.toMap()
            }
        }
    }

    fun ensureFullListLoaded() {
        if (fullListJob != null) return
        fullListJob = viewModelScope.launch {
            getFavoriteQuestionsUseCase().collect { list ->
                _favoriteQuestions.value = list
            }
        }
    }

    fun addFavorite(question: Question) {
        viewModelScope.launch { addFavoriteQuestionUseCase(FavoriteQuestion(question)) }
    }

    fun removeFavorite(id: Int) {
        viewModelScope.launch { removeFavoriteQuestionUseCase(id) }
    }

    fun removeByFileName(fileName: String) {
        viewModelScope.launch { removeFavoriteQuestionsByFileNameUseCase(fileName) }
    }

    /** 重答：清除该文件在收藏库练习/考试下的进度。 */
    fun clearScopedProgress(fileName: String) {
        viewModelScope.launch {
            clearPracticeProgressByFileNameUseCase("favorite_$fileName")
            clearExamProgressByFileNameUseCase("favorite_$fileName")
            clearExamProgressByFileNameUseCase(fileName)
        }
    }

    fun exportFavorite(fileName: String? = null) {
        viewModelScope.launch {
            try {
                val exportData = exportFavoriteUseCase(fileName)
                val jsonString = Json.encodeToString(exportData)
                val exportFileName = if (fileName != null) {
                    "${EXPORT_FILENAME_PREFIX}_${fileName}_${System.currentTimeMillis()}.json"
                } else {
                    "${EXPORT_FILENAME_PREFIX}_全部_${System.currentTimeMillis()}.json"
                }
                saveToFile(exportFileName, jsonString)
                _exportResult.value = "$EXPORT_PREFIX$exportFileName"
            } catch (e: Exception) {
                _exportResult.value = "$EXPORT_FAILED_PREFIX${e.message}"
            }
        }
    }

    fun importQuestions(jsonContent: String) {
        viewModelScope.launch {
            try {
                val exportData = Json.decodeFromString<ExportData>(jsonContent)
                val result = importQuestionsUseCase(exportData)
                _importResult.value = if (result.isSuccess) {
                    result.getOrNull() ?: IMPORT_SUCCESS
                } else {
                    "$IMPORT_FAILED_PREFIX${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _importResult.value = "$IMPORT_FAILED_PREFIX${e.message}"
            }
        }
    }

    fun clearExportResult() { _exportResult.value = null }
    fun clearImportResult() { _importResult.value = null }

    private suspend fun saveToFile(fileName: String, content: String) = Unit
}
