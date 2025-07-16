package com.example.testapp.presentation.screen

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.repository.HistoryRepository
import com.example.testapp.domain.repository.FavoriteQuestionRepository
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
    private val historyRepository: HistoryRepository,
    private val favoriteRepository: FavoriteQuestionRepository
) : ViewModel() {
    private val _fontSize = MutableStateFlow(18f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()
    private val _fontStyle = MutableStateFlow("Normal")
    val fontStyle: StateFlow<String> = _fontStyle.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    private val _examQuestionCount = MutableStateFlow(10)
    val examQuestionCount: StateFlow<Int> = _examQuestionCount.asStateFlow()
    private val _practiceQuestionCount = MutableStateFlow(0)
    val practiceQuestionCount: StateFlow<Int> = _practiceQuestionCount.asStateFlow()
    private val _randomPractice = MutableStateFlow(false)
    val randomPractice: StateFlow<Boolean> = _randomPractice.asStateFlow()
    private val _randomExam = MutableStateFlow(true)
    val randomExam: StateFlow<Boolean> = _randomExam.asStateFlow()
    private val _correctDelay = MutableStateFlow(1)
    val correctDelay: StateFlow<Int> = _correctDelay.asStateFlow()
    private val _wrongDelay = MutableStateFlow(2)
    val wrongDelay: StateFlow<Int> = _wrongDelay.asStateFlow()
    private val _examDelay = MutableStateFlow(1)
    val examDelay: StateFlow<Int> = _examDelay.asStateFlow()
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()
    private var currentJob: Job? = null

    fun setFontSize(context: Context, size: Float) {
        _fontSize.value = size
        android.util.Log.d("SettingsVM", "setFontSize size=$size")
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
    fun setExamQuestionCount(context: Context, count: Int) {
        _examQuestionCount.value = count
        viewModelScope.launch {
            FontSettingsDataStore.setExamQuestionCount(context, count)
        }
    }
    fun setPracticeQuestionCount(context: Context, count: Int) {
        _practiceQuestionCount.value = count
        viewModelScope.launch {
            FontSettingsDataStore.setPracticeQuestionCount(context, count)
        }
    }
    fun setRandomPractice(context: Context, enabled: Boolean) {
        _randomPractice.value = enabled
        viewModelScope.launch {
            FontSettingsDataStore.setRandomPractice(context, enabled)
        }
    }

    fun setRandomExam(context: Context, enabled: Boolean) {
        _randomExam.value = enabled
        viewModelScope.launch {
            FontSettingsDataStore.setRandomExam(context, enabled)
        }
    }

    fun setCorrectDelay(context: Context, delay: Int) {
        _correctDelay.value = delay
        viewModelScope.launch {
            FontSettingsDataStore.setCorrectDelay(context, delay)
        }
    }

    fun setWrongDelay(context: Context, delay: Int) {
        _wrongDelay.value = delay
        viewModelScope.launch {
            FontSettingsDataStore.setWrongDelay(context, delay)
        }
    }

    fun setExamDelay(context: Context, delay: Int) {
        _examDelay.value = delay
        viewModelScope.launch {
            FontSettingsDataStore.setExamDelay(context, delay)
        }
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        _soundEnabled.value = enabled
        viewModelScope.launch {
            FontSettingsDataStore.setSoundEnabled(context, enabled)
        }
    }

    fun loadFontSettings(context: Context) {
        viewModelScope.launch {
            val size = FontSettingsDataStore.getFontSize(context).first()
            val style = FontSettingsDataStore.getFontStyle(context).first()
            val examCount = FontSettingsDataStore.getExamQuestionCount(context).first()
            val practiceCount = FontSettingsDataStore.getPracticeQuestionCount(context).first()
            val random = FontSettingsDataStore.getRandomPractice(context).first()
            val randomExam = FontSettingsDataStore.getRandomExam(context).first()
            val correct = FontSettingsDataStore.getCorrectDelay(context).first()
            val wrong = FontSettingsDataStore.getWrongDelay(context).first()
            val examDelay = FontSettingsDataStore.getExamDelay(context).first()
            val sound = FontSettingsDataStore.getSoundEnabled(context).first()
            android.util.Log.d(
                "SettingsVM",
                "loadFontSettings size=$size style=$style examCount=$examCount practiceCount=$practiceCount random=$random randomExam=$randomExam sound=$sound"
            )
            _fontSize.value = size
            _fontStyle.value = style
            _examQuestionCount.value = examCount
            _practiceQuestionCount.value = practiceCount
            _randomPractice.value = random
            _randomExam.value = randomExam
            _correctDelay.value = correct
            _wrongDelay.value = wrong
            _examDelay.value = examDelay
            _soundEnabled.value = sound
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
    fun importFavoritesFromUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val file = uriToFile(context, uri)
            val count = if (file != null) favoriteRepository.importFromFile(file) else 0
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

    private fun saveTempToUri(tempFile: java.io.File, context: Context, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            tempFile.inputStream().use { input ->
                input.copyTo(out)
            }
        } ?: throw Exception("无法写入文件")
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
                val tmp = java.io.File.createTempFile("export_", ".xlsx", context.cacheDir)
                val result = (questionRepository as? com.example.testapp.data.repository.QuestionRepositoryImpl)?.exportQuestionsToExcel(questions, tmp) ?: false
                if (result) {
                    saveTempToUri(tmp, context, uri)
                }
                tmp.delete()
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
                val wrongs = wrongBookRepository.getAll().firstOrNull() ?: emptyList()
                val out = context.contentResolver.openOutputStream(uri)
                    ?: throw Exception("无法写入文件")
                out.use { output ->
                    val json = Json.encodeToString(wrongs)
                    output.write(json.toByteArray())
                }
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun exportFavoritesToUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val favorites = favoriteRepository.getAll().firstOrNull() ?: emptyList()
                val out = context.contentResolver.openOutputStream(uri)
                    ?: throw Exception("无法写入文件")
                out.use { output ->
                    val json = Json.encodeToString(favorites)
                    output.write(json.toByteArray())
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

                val wrongs = wrongBookRepository.getAll().firstOrNull() ?: emptyList()
                val tmp = java.io.File.createTempFile("export_", ".xlsx", context.cacheDir)
                val result = (wrongBookRepository as? com.example.testapp.data.repository.WrongBookRepositoryImpl)?.exportWrongBookAsQuestionExcel(wrongs, tmp) ?: false
                if (result) {
                    saveTempToUri(tmp, context, uri)
                }
                tmp.delete()
                onResult(result)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
    fun exportFavoritesToExcelFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val favorites = favoriteRepository.getAll().firstOrNull() ?: emptyList()
                val tmp = java.io.File.createTempFile("export_", ".xlsx", context.cacheDir)
                val result = (favoriteRepository as? com.example.testapp.data.repository.FavoriteQuestionRepositoryImpl)?.exportFavoritesToExcel(favorites, tmp) ?: false
                if (result) {
                    saveTempToUri(tmp, context, uri)
                }
                tmp.delete()
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
                val tmp = java.io.File.createTempFile("export_", ".xlsx", context.cacheDir)
                val result = (historyRepository as? com.example.testapp.data.repository.HistoryRepositoryImpl)?.exportHistoryToExcel(history, tmp) ?: false
                if (result) {
                    saveTempToUri(tmp, context, uri)
                }
                tmp.delete()
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
