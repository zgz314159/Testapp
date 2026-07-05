package com.example.testapp.presentation.screen.wrongbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.ExportData
import com.example.testapp.domain.model.LibraryCatalog
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.usecase.ExportWrongBookUseCase
import com.example.testapp.domain.usecase.GetWrongBookLibraryCatalogUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.ImportQuestionsUseCase
import com.example.testapp.domain.usecase.RemoveWrongQuestionsByFileNameUseCase
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
class WrongBookViewModel @Inject constructor(
    getWrongBookLibraryCatalogUseCase: GetWrongBookLibraryCatalogUseCase,
    private val getWrongBookUseCase: GetWrongBookUseCase,
    private val wrongBookRepository: WrongBookRepository,
    private val removeWrongQuestionsByFileNameUseCase: RemoveWrongQuestionsByFileNameUseCase,
    private val exportWrongBookUseCase: ExportWrongBookUseCase,
    private val importQuestionsUseCase: ImportQuestionsUseCase
) : ViewModel() {

    companion object {
        private const val EXPORT_PREFIX = "导出成功："
        private const val EXPORT_FAILED_PREFIX = "导出失败："
        private const val IMPORT_SUCCESS = "导入成功"
        private const val IMPORT_FAILED_PREFIX = "导入失败："
    }

    private val _libraryCatalog = MutableStateFlow(LibraryCatalog(emptyList(), emptyMap()))
    val libraryCatalog: StateFlow<LibraryCatalog> = _libraryCatalog.asStateFlow()

    val fileNames: StateFlow<List<String>> = _libraryCatalog
        .map { it.fileNames }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _wrongQuestions = MutableStateFlow<List<WrongQuestion>>(emptyList())
    val wrongQuestions: StateFlow<List<WrongQuestion>> = _wrongQuestions.asStateFlow()

    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()

    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult.asStateFlow()

    private var fullListJob: Job? = null

    init {
        viewModelScope.launch {
            getWrongBookLibraryCatalogUseCase().collect { catalog ->
                _libraryCatalog.value = catalog
            }
        }
    }

    fun ensureFullListLoaded() {
        if (fullListJob != null) return
        fullListJob = viewModelScope.launch {
            getWrongBookUseCase().collect { list ->
                _wrongQuestions.value = list
            }
        }
    }

    fun addWrongQuestion(wrong: WrongQuestion) {
        viewModelScope.launch { wrongBookRepository.add(wrong) }
    }

    fun removeByFileName(fileName: String) {
        viewModelScope.launch { removeWrongQuestionsByFileNameUseCase(fileName) }
    }

    fun exportWrongBook(fileName: String? = null) {
        viewModelScope.launch {
            try {
                val exportData = exportWrongBookUseCase(fileName)
                val jsonString = Json.encodeToString(exportData)
                val exportFileName = if (fileName != null) {
                    "错题本_${fileName}_${System.currentTimeMillis()}.json"
                } else {
                    "错题本_全部_${System.currentTimeMillis()}.json"
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
