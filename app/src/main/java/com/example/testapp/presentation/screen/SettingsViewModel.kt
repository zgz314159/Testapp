package com.example.testapp.presentation.screen

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.repository.HistoryRepository
import com.example.testapp.data.datastore.FontSettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val wrongBookRepository: WrongBookRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    private val _fontSize = MutableStateFlow(18f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()
    private val _fontStyle = MutableStateFlow("Normal")
    val fontStyle: StateFlow<String> = _fontStyle.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    private var currentJob: Job? = null

    fun setFontSize(context: Context, size: Float) {
        _fontSize.value = size
        viewModelScope.launch {
            FontSettingsDataStore.setFontSize(context, size)
        }
    }
    fun setFontStyle(context: Context, style: String) {
        _fontStyle.value = style
        viewModelScope.launch {
            FontSettingsDataStore.setFontStyle(context, style)
        }
    }
    fun loadFontSettings(context: Context) {
        viewModelScope.launch {
            val size = FontSettingsDataStore.getFontSize(context).first()
            val style = FontSettingsDataStore.getFontStyle(context).first()
            _fontSize.value = size
            _fontStyle.value = style
        }
    }

    fun importQuestionsFromUris(context: Context, uris: List<Uri>, onResult: (Boolean, List<String>?) -> Unit) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _isLoading.value = true
            _progress.value = 0f
            var total = 0
            val count = uris.size
            var duplicateFiles: List<String>? = null
            for ((idx, uri) in uris.withIndex()) {
                if (!isActive) break
                val file = uriToFile(context, uri)
                val originalFileName = getFileNameFromUri(context, uri)
                if (file != null && originalFileName != null) {
                    try {
                        total += questionRepository.importFromFilesWithOrigin(listOf(file to originalFileName))
                    } catch (e: com.example.testapp.data.repository.QuestionRepositoryImpl.DuplicateFileImportException) {
                        duplicateFiles = e.duplicates
                    }
                }
                _progress.value = (idx + 1f) / count
            }
            _isLoading.value = false
            _progress.value = 0f
            onResult(total > 0, duplicateFiles)
        }
    }
    fun importWrongBookFromUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val file = uriToFile(context, uri)
            val count = if (file != null) wrongBookRepository.importFromFile(file) else 0
            onResult(count > 0)
        }
    }
    fun importHistoryFromUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val file = uriToFile(context, uri)
            val count = if (file != null) historyRepository.importFromFile(file) else 0
            onResult(count > 0)
        }
    }

    // 工具方法：将 SAF Uri 转为临时 File，适用于导入流程
    private fun uriToFile(context: Context, uri: Uri): java.io.File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = java.io.File.createTempFile("import_", null, context.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
    fun exportQuestionsToFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val questions = questionRepository.exportQuestions()
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    // 示例：导出为 JSON
                    val json = Json.encodeToString(questions)
                    output.write(json.toByteArray())
                }
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun exportQuestionsToExcelFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val questions = questionRepository.exportQuestions()
                val file = uriToFile(context, uri) ?: throw Exception("无法写入文件")
                val result = (questionRepository as? com.example.testapp.data.repository.QuestionRepositoryImpl)?.exportQuestionsToExcel(questions, file) ?: false
                _isLoading.value = false
                onResult(result)
            } catch (e: Exception) {
                _isLoading.value = false
                onResult(false)
            }
        }
    }
    fun exportWrongBookToUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // TODO: 获取错题本数据并序列化导出
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    // 示例：导出为 JSON
                    // val wrongs = wrongBookRepository.exportToList()
                    // val json = kotlinx.serialization.json.Json.encodeToString(wrongs)
                    // output.write(json.toByteArray())
                }
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun exportHistoryToUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // TODO: 获取历史记录数据并序列化导出
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    // 示例：导出为 JSON
                    // val history = historyRepository.exportToList()
                    // val json = kotlinx.serialization.json.Json.encodeToString(history)
                    // output.write(json.toByteArray())
                }
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun exportWrongBookToExcelFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 获取错题本数据
                val wrongs = wrongBookRepository.getAll().firstOrNull() ?: emptyList()
                val file = uriToFile(context, uri) ?: throw Exception("无法写入文件")
                val result = (wrongBookRepository as? com.example.testapp.data.repository.WrongBookRepositoryImpl)?.exportWrongBookToExcel(wrongs, file) ?: false
                onResult(result)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun exportHistoryToExcelFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val history = historyRepository.getAll().firstOrNull() ?: emptyList()
                val file = uriToFile(context, uri) ?: throw Exception("无法写入文件")
                val result = (historyRepository as? com.example.testapp.data.repository.HistoryRepositoryImpl)?.exportHistoryToExcel(history, file) ?: false
                onResult(result)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun cancelImportExport() {
        currentJob?.cancel()
        _isLoading.value = false
        _progress.value = 0f
    }

    // 新增：通过uri获取原始文件名
    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        val resolver = context.contentResolver
        val cursor = resolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return it.getString(idx)
            }
        }
        return uri.lastPathSegment
    }
}
