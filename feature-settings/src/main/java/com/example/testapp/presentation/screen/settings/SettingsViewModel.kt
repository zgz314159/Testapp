package com.example.testapp.presentation.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.util.FillQuestionFilterSummary
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.usecase.SettingsRepositoryFacade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repositories: SettingsRepositoryFacade,
    val fontSettings: FontSettingsCoordinator,
    private val fontSettingsRepository: FontSettingsRepository,
    private val fillFilter: FillQuestionFilterCoordinator,
    private val importCoordinator: SettingsImportGateway,
    private val jsonExport: SettingsJsonExportGateway,
    private val excelExport: SettingsExcelExportGateway
) : ViewModel() {

    // --- VM-owned state (import/export UI) ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    private val _messageResult = MutableStateFlow<LocalizedResult?>(null)
    val messageResult: StateFlow<LocalizedResult?> = _messageResult.asStateFlow()

    private val actionPipeline = SettingsActionPipeline(
        scope = viewModelScope,
        isLoading = _isLoading,
        progress = _progress,
        messageResult = _messageResult
    )

    // --- Delegated font settings StateFlows ---
    val fontSize: StateFlow<Float> get() = fontSettings.fontSize
    val fontStyle: StateFlow<String> get() = fontSettings.fontStyle
    val examQuestionCount: StateFlow<Int> get() = fontSettings.examQuestionCount
    val practiceQuestionCount: StateFlow<Int> get() = fontSettings.practiceQuestionCount
    val randomPractice: StateFlow<Boolean> get() = fontSettings.randomPractice
    val randomExam: StateFlow<Boolean> get() = fontSettings.randomExam
    val correctDelay: StateFlow<Int> get() = fontSettings.correctDelay
    val wrongDelay: StateFlow<Int> get() = fontSettings.wrongDelay
    val examDelay: StateFlow<Int> get() = fontSettings.examDelay
    val fillBlankCount: StateFlow<Int> get() = fontSettings.fillBlankCount
    val randomFillBlanks: StateFlow<Boolean> get() = fontSettings.randomFillBlanks
    val fillQuestionGenerationMode: StateFlow<FillQuestionGenerationMode> get() = fontSettings.fillQuestionGenerationMode
    val fillFullAnswerRandomOrder: StateFlow<Boolean> get() = fontSettings.fillFullAnswerRandomOrder
    val fillFullAnswerRequireCorrect: StateFlow<Boolean> get() = fontSettings.fillFullAnswerRequireCorrect
    val fillAnswerScoreMin: StateFlow<Int> get() = fontSettings.fillAnswerScoreMin
    val fillAnswerScoreMax: StateFlow<Int> get() = fontSettings.fillAnswerScoreMax
    val fillAnswerTagFilter: StateFlow<String> get() = fontSettings.fillAnswerTagFilter
    val availableFillAnswerTags: StateFlow<List<String>> get() = fontSettings.availableFillAnswerTags
    val fillQuestionFilterSummary: StateFlow<FillQuestionFilterSummary> get() = fontSettings.fillQuestionFilterSummary
    val practiceMemoryMode: StateFlow<Boolean> get() = fontSettings.practiceMemoryMode
    val practiceMemoryBatchSize: StateFlow<Int> get() = fontSettings.practiceMemoryBatchSize
    val practiceMemoryWrongMode: StateFlow<Int> get() = fontSettings.practiceMemoryWrongMode
    val practiceMemoryPoolMode: StateFlow<Int> get() = fontSettings.practiceMemoryPoolMode
    val examMemoryMode: StateFlow<Boolean> get() = fontSettings.examMemoryMode
    val examMemoryBatchSize: StateFlow<Int> get() = fontSettings.examMemoryBatchSize
    val examMemoryWrongMode: StateFlow<Int> get() = fontSettings.examMemoryWrongMode
    val examMemoryPoolMode: StateFlow<Int> get() = fontSettings.examMemoryPoolMode
    val soundEnabled: StateFlow<Boolean> get() = fontSettings.soundEnabled
    val darkTheme: StateFlow<Boolean> get() = fontSettings.darkTheme
    val quizFileNames: StateFlow<List<String>> get() = fontSettings.quizFileNames
    val wrongBookFileNames: StateFlow<List<String>> get() = fontSettings.wrongBookFileNames
    val favoriteFileNames: StateFlow<List<String>> get() = fontSettings.favoriteFileNames
    val settingsReady: StateFlow<Boolean> get() = fontSettings.settingsReady

    // --- Lifecycle ---

    fun ensureSettingsCollectionsStarted() {
        fontSettings.ensureCollectionsStarted(
            repositories.questions, repositories.wrongBook, repositories.favorites, fillFilter, viewModelScope
        )
    }

    fun loadFontSettings() {
        viewModelScope.launch { fontSettings.loadFontSettings() }
    }

    // --- Font settings setters (emit state + persist via DataStore) ---

    fun setFontSize(context: Context, size: Float) {
        fontSettings.emitFontSize(size)
        viewModelScope.launch { fontSettingsRepository.setFontSize(size) }
    }

    fun setFontStyle(context: Context, style: String) {
        fontSettings.emitFontStyle(style)
        viewModelScope.launch { fontSettingsRepository.setFontStyle(style) }
    }

    fun setExamQuestionCount(context: Context, count: Int) {
        fontSettings.emitExamQuestionCount(count)
        viewModelScope.launch { fontSettingsRepository.setExamQuestionCount(count) }
    }

    fun setPracticeQuestionCount(context: Context, count: Int) {
        fontSettings.emitPracticeQuestionCount(count)
        viewModelScope.launch { fontSettingsRepository.setPracticeQuestionCount(count) }
    }

    fun setRandomPractice(context: Context, enabled: Boolean) {
        fontSettings.emitRandomPractice(enabled)
        viewModelScope.launch { fontSettingsRepository.setRandomPractice(enabled) }
    }

    fun setRandomExam(context: Context, enabled: Boolean) {
        fontSettings.emitRandomExam(enabled)
        viewModelScope.launch { fontSettingsRepository.setRandomExam(enabled) }
    }

    fun setCorrectDelay(context: Context, delay: Int) {
        fontSettings.emitCorrectDelay(delay)
        viewModelScope.launch { fontSettingsRepository.setCorrectDelay(delay) }
    }

    fun setWrongDelay(context: Context, delay: Int) {
        fontSettings.emitWrongDelay(delay)
        viewModelScope.launch { fontSettingsRepository.setWrongDelay(delay) }
    }

    fun setExamDelay(context: Context, delay: Int) {
        fontSettings.emitExamDelay(delay)
        viewModelScope.launch { fontSettingsRepository.setExamDelay(delay) }
    }

    fun setFillBlankCount(context: Context, count: Int) {
        fontSettings.emitFillBlankCount(count)
        viewModelScope.launch { fontSettingsRepository.setFillBlankCount(count) }
    }

    fun setRandomFillBlanks(context: Context, enabled: Boolean) {
        fontSettings.emitRandomFillBlanks(enabled)
        viewModelScope.launch { fontSettingsRepository.setRandomPractice(enabled) }
    }

    fun setFillQuestionGenerationMode(context: Context, mode: FillQuestionGenerationMode) {
        fontSettings.emitFillQuestionGenerationMode(mode)
        viewModelScope.launch {
            fontSettingsRepository.setFillQuestionGenerationMode(mode)
            if (mode == FillQuestionGenerationMode.FULL_ANSWER && fontSettings.fillBlankCount.value <= 0) {
                fontSettings.emitFillBlankCount(1)
                fontSettingsRepository.setFillBlankCount(1)
            }
        }
    }

    fun setFillFullAnswerRandomOrder(context: Context, enabled: Boolean) {
        fontSettings.emitFillFullAnswerRandomOrder(enabled)
        viewModelScope.launch { fontSettingsRepository.setFillFullAnswerRandomOrder(enabled) }
    }

    fun setFillFullAnswerRequireCorrect(context: Context, enabled: Boolean) {
        fontSettings.emitFillFullAnswerRequireCorrect(enabled)
        viewModelScope.launch { fontSettingsRepository.setFillFullAnswerRequireCorrect(enabled) }
    }

    fun setFillAnswerScoreRange(context: Context, minScore: Int, maxScore: Int) {
        fontSettings.emitFillAnswerScoreRange(minScore, maxScore)
        viewModelScope.launch {
            fontSettingsRepository.setFillAnswerScoreMin(minScore.coerceIn(1, 10))
            fontSettingsRepository.setFillAnswerScoreMax(maxScore.coerceIn(minScore, 10))
        }
    }

    fun setFillAnswerTagFilter(context: Context, value: String) {
        fontSettings.emitFillAnswerTagFilter(value)
        viewModelScope.launch { fontSettingsRepository.setFillAnswerTagFilter(value) }
    }

    fun setPracticeMemoryMode(context: Context, enabled: Boolean) {
        fontSettings.emitPracticeMemoryMode(enabled)
        viewModelScope.launch { fontSettingsRepository.setPracticeMemoryMode(if (enabled) 1 else 0) }
    }

    fun setPracticeMemoryBatchSize(context: Context, count: Int) {
        fontSettings.emitPracticeMemoryBatchSize(count)
        viewModelScope.launch { fontSettingsRepository.setPracticeMemoryBatchSize(count) }
    }

    fun setPracticeMemoryWrongMode(context: Context, mode: Int) {
        fontSettings.emitPracticeMemoryWrongMode(mode)
        viewModelScope.launch { fontSettingsRepository.setPracticeMemoryWrongMode(mode) }
    }

    fun setPracticeMemoryPoolMode(context: Context, mode: Int) {
        fontSettings.emitPracticeMemoryPoolMode(mode)
        viewModelScope.launch { fontSettingsRepository.setPracticeMemoryPoolMode(mode) }
    }

    fun setExamMemoryMode(context: Context, enabled: Boolean) {
        fontSettings.emitExamMemoryMode(enabled)
        viewModelScope.launch { fontSettingsRepository.setExamMemoryMode(if (enabled) 1 else 0) }
    }

    fun setExamMemoryBatchSize(context: Context, count: Int) {
        fontSettings.emitExamMemoryBatchSize(count)
        viewModelScope.launch { fontSettingsRepository.setExamMemoryBatchSize(count) }
    }

    fun setExamMemoryWrongMode(context: Context, mode: Int) {
        fontSettings.emitExamMemoryWrongMode(mode)
        viewModelScope.launch { fontSettingsRepository.setExamMemoryWrongMode(mode) }
    }

    fun setExamMemoryPoolMode(context: Context, mode: Int) {
        fontSettings.emitExamMemoryPoolMode(mode)
        viewModelScope.launch { fontSettingsRepository.setExamMemoryPoolMode(mode) }
    }

    fun setMemoryMode(context: Context, enabled: Boolean) {
        setPracticeMemoryMode(context, enabled)
        setExamMemoryMode(context, enabled)
    }

    fun setMemoryBatchSize(context: Context, count: Int) {
        setPracticeMemoryBatchSize(context, count)
        setExamMemoryBatchSize(context, count)
    }

    fun setMemoryWrongMode(context: Context, mode: Int) {
        setPracticeMemoryWrongMode(context, mode)
        setExamMemoryWrongMode(context, mode)
    }

    fun setMemoryPoolMode(context: Context, mode: Int) {
        setPracticeMemoryPoolMode(context, mode)
        setExamMemoryPoolMode(context, mode)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        fontSettings.emitSoundEnabled(enabled)
        viewModelScope.launch { fontSettingsRepository.setSoundEnabled(enabled) }
    }

    fun setDarkTheme(context: Context, enabled: Boolean) {
        fontSettings.emitDarkTheme(enabled)
        viewModelScope.launch { fontSettingsRepository.setDarkTheme(enabled) }
    }

    // --- Import ---

    fun importQuestionsFromUris(context: Context, uris: List<Uri>, onResult: (Boolean, List<String>?) -> Unit) {
        actionPipeline.launchCancellable(
            onError = { e ->
                val reason = e.message?.take(30)
                onResult(false, reason?.let { listOf(it) })
                actionPipeline.setMessage(LocalizedResult("import_failed_detail", listOf(reason ?: "")))
            }
        ) {
            val result = importCoordinator.importQuestionsFromUris(
                context, uris, repositories.questions,
                onProgress = ::setProgress,
                onMessage = { msg -> setMessage(LocalizedResult(msg.key, msg.args)) }
            )
            onResult(result.success, result.errorMessage)
            setMessage(result.toLocalizedResult())
        }
    }

    fun importQuestionsFromFiles(context: Context, files: List<java.io.File>, onResult: (Boolean, List<String>?) -> Unit) {
        actionPipeline.launchCancellable(
            onError = { e ->
                val reason = e.message?.take(30)
                onResult(false, reason?.let { listOf(it) })
                actionPipeline.setMessage(LocalizedResult("import_failed_detail", listOf(reason ?: "")))
            }
        ) {
            val result = importCoordinator.importQuestionsFromFiles(
                context, files, repositories.questions,
                onProgress = ::setProgress,
                onMessage = { msg -> setMessage(LocalizedResult(msg.key, msg.args)) }
            )
            onResult(result.success, result.errorMessage)
            setMessage(result.toLocalizedResult())
        }
    }

    fun importWrongBookFromUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        actionPipeline.launch {
            val success = importCoordinator.importWrongBookFromUri(
                context, uri, repositories.wrongBook,
                onMessage = { msg -> setMessage(LocalizedResult(msg.key, msg.args)) }
            )
            onResult(success)
        }
    }

    fun importFavoritesFromUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        actionPipeline.launch {
            val success = importCoordinator.importFavoritesFromUri(
                context, uri, repositories.favorites,
                onMessage = { msg -> setMessage(LocalizedResult(msg.key, msg.args)) }
            )
            onResult(success)
        }
    }

    // --- Export ---

    fun exportQuestionsToFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        actionPipeline.launch {
            jsonExport.exportQuestionsToFile(
                context, uri, repositories.questions, onResult,
                onMessage = ::setMessage
            )
        }
    }

    fun exportQuestionsToExcelFile(context: Context, uri: Uri, fileName: String?, onResult: (Boolean) -> Unit) {
        actionPipeline.launchLoading {
            excelExport.exportQuestionsToExcelFile(
                context, uri, fileName, repositories.questions,
                repositories.analysis, repositories.asks, repositories.notes,
                onLoading = { _isLoading.value = it }, onResult,
                onMessage = ::setMessage
            )
        }
    }

    fun exportWrongBookToUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        actionPipeline.launch {
            jsonExport.exportWrongBookToUri(
                context, uri, repositories.wrongBook, onResult,
                onMessage = ::setMessage
            )
        }
    }

    fun exportFavoritesToUri(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        actionPipeline.launch {
            jsonExport.exportFavoritesToUri(
                context, uri, repositories.favorites, onResult,
                onMessage = ::setMessage
            )
        }
    }

    fun exportWrongBookToExcelFile(context: Context, uri: Uri, fileName: String?, onResult: (Boolean) -> Unit) {
        actionPipeline.launchLoading {
            excelExport.exportWrongBookToExcelFile(
                context, uri, fileName, repositories.wrongBook,
                repositories.analysis, repositories.asks, repositories.notes, onResult,
                onMessage = ::setMessage
            )
        }
    }

    fun exportFavoritesToExcelFile(context: Context, uri: Uri, fileName: String?, onResult: (Boolean) -> Unit) {
        actionPipeline.launchLoading {
            excelExport.exportFavoritesToExcelFile(
                context, uri, fileName, repositories.favorites,
                repositories.analysis, repositories.asks, repositories.notes, onResult,
                onMessage = ::setMessage
            )
        }
    }

    fun exportHistoryToExcelFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        actionPipeline.launchLoading {
            excelExport.exportHistoryToExcelFile(
                context, uri, repositories.history, onResult,
                onMessage = ::setMessage
            )
        }
    }

    // --- Cancel & cleanup ---

    fun cancelImportExport() {
        actionPipeline.cancel()
    }

    fun clearMessageResult() {
        actionPipeline.setMessage(null)
    }

    // --- Import extension ---

    private fun ImportResult.toLocalizedResult(): LocalizedResult {
        val msg = this
        return if (msg.success) {
            LocalizedResult(com.example.testapp.domain.IOConstants.IMPORT_SUCCESS)
        } else {
            LocalizedResult("import_failed_detail", listOf(msg.errorMessage?.joinToString("\n") ?: ""))
        }
    }
}

