package com.example.testapp.presentation.screen.settings

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.util.FillQuestionFilterSummary
import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontSettingsCoordinator @Inject constructor(
    private val fontSettings: FontSettingsRepository
) {

    private val _fontSize = MutableStateFlow(18f)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()
    private val _fontStyle = MutableStateFlow("Normal")
    val fontStyle: StateFlow<String> = _fontStyle.asStateFlow()
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
    private val _fillBlankCount = MutableStateFlow(0)
    val fillBlankCount: StateFlow<Int> = _fillBlankCount.asStateFlow()
    private val _randomFillBlanks = MutableStateFlow(false)
    val randomFillBlanks: StateFlow<Boolean> = _randomFillBlanks.asStateFlow()
    private val _fillQuestionGenerationMode = MutableStateFlow(FillQuestionGenerationMode.SCORE_RANGE_RANDOM)
    val fillQuestionGenerationMode: StateFlow<FillQuestionGenerationMode> = _fillQuestionGenerationMode.asStateFlow()
    private val _fillFullAnswerRandomOrder = MutableStateFlow(true)
    val fillFullAnswerRandomOrder: StateFlow<Boolean> = _fillFullAnswerRandomOrder.asStateFlow()
    private val _fillFullAnswerRequireCorrect = MutableStateFlow(false)
    val fillFullAnswerRequireCorrect: StateFlow<Boolean> = _fillFullAnswerRequireCorrect.asStateFlow()
    private val _fillAnswerScoreMin = MutableStateFlow(1)
    val fillAnswerScoreMin: StateFlow<Int> = _fillAnswerScoreMin.asStateFlow()
    private val _fillAnswerScoreMax = MutableStateFlow(10)
    val fillAnswerScoreMax: StateFlow<Int> = _fillAnswerScoreMax.asStateFlow()
    private val _fillAnswerTagFilter = MutableStateFlow("")
    val fillAnswerTagFilter: StateFlow<String> = _fillAnswerTagFilter.asStateFlow()
    private val _availableFillAnswerTags = MutableStateFlow<List<String>>(emptyList())
    val availableFillAnswerTags: StateFlow<List<String>> = _availableFillAnswerTags.asStateFlow()
    private val _fillQuestionFilterSummary = MutableStateFlow(FillQuestionFilterSummary())
    val fillQuestionFilterSummary: StateFlow<FillQuestionFilterSummary> = _fillQuestionFilterSummary.asStateFlow()
    private val _practiceMemoryMode = MutableStateFlow(false)
    val practiceMemoryMode: StateFlow<Boolean> = _practiceMemoryMode.asStateFlow()
    private val _practiceMemoryBatchSize = MutableStateFlow(10)
    val practiceMemoryBatchSize: StateFlow<Int> = _practiceMemoryBatchSize.asStateFlow()
    private val _practiceMemoryWrongMode = MutableStateFlow(0)
    val practiceMemoryWrongMode: StateFlow<Int> = _practiceMemoryWrongMode.asStateFlow()
    private val _practiceMemoryPoolMode = MutableStateFlow(0)
    val practiceMemoryPoolMode: StateFlow<Int> = _practiceMemoryPoolMode.asStateFlow()
    private val _examMemoryMode = MutableStateFlow(false)
    val examMemoryMode: StateFlow<Boolean> = _examMemoryMode.asStateFlow()
    private val _examMemoryBatchSize = MutableStateFlow(10)
    val examMemoryBatchSize: StateFlow<Int> = _examMemoryBatchSize.asStateFlow()
    private val _examMemoryWrongMode = MutableStateFlow(0)
    val examMemoryWrongMode: StateFlow<Int> = _examMemoryWrongMode.asStateFlow()
    private val _examMemoryPoolMode = MutableStateFlow(0)
    val examMemoryPoolMode: StateFlow<Int> = _examMemoryPoolMode.asStateFlow()
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()
    private val _darkTheme = MutableStateFlow(false)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()
    private val _quizFileNames = MutableStateFlow<List<String>>(emptyList())
    val quizFileNames: StateFlow<List<String>> = _quizFileNames.asStateFlow()
    private val _wrongBookFileNames = MutableStateFlow<List<String>>(emptyList())
    val wrongBookFileNames: StateFlow<List<String>> = _wrongBookFileNames.asStateFlow()
    private val _favoriteFileNames = MutableStateFlow<List<String>>(emptyList())
    val favoriteFileNames: StateFlow<List<String>> = _favoriteFileNames.asStateFlow()
    private val _settingsReady = MutableStateFlow(false)
    val settingsReady: StateFlow<Boolean> = _settingsReady.asStateFlow()

    internal var cachedQuestionsSnapshot: List<Question>? = null

    // --- State-only setters (no DataStore IO) ---

    fun emitFontSize(size: Float) { _fontSize.value = size }
    fun emitFontStyle(style: String) { _fontStyle.value = style }
    fun emitExamQuestionCount(count: Int) { _examQuestionCount.value = count }
    fun emitPracticeQuestionCount(count: Int) { _practiceQuestionCount.value = count }
    fun emitRandomPractice(enabled: Boolean) { _randomPractice.value = enabled }
    fun emitRandomExam(enabled: Boolean) { _randomExam.value = enabled }
    fun emitCorrectDelay(delay: Int) { _correctDelay.value = delay }
    fun emitWrongDelay(delay: Int) { _wrongDelay.value = delay }
    fun emitExamDelay(delay: Int) { _examDelay.value = delay }
    fun emitFillBlankCount(count: Int) { _fillBlankCount.value = count }
    fun emitRandomFillBlanks(enabled: Boolean) { _randomFillBlanks.value = enabled }
    fun emitFillQuestionGenerationMode(mode: FillQuestionGenerationMode) {
        _fillQuestionGenerationMode.value = mode
        _randomFillBlanks.value = mode == FillQuestionGenerationMode.SCORE_RANGE_RANDOM
        if (mode == FillQuestionGenerationMode.FULL_ANSWER && _fillBlankCount.value <= 0) {
            _fillBlankCount.value = 1
        }
    }
    fun emitFillFullAnswerRandomOrder(enabled: Boolean) { _fillFullAnswerRandomOrder.value = enabled }
    fun emitFillFullAnswerRequireCorrect(enabled: Boolean) { _fillFullAnswerRequireCorrect.value = enabled }
    fun emitFillAnswerScoreRange(minScore: Int, maxScore: Int) {
        _fillAnswerScoreMin.value = minScore.coerceIn(1, 10)
        _fillAnswerScoreMax.value = maxScore.coerceIn(minScore, 10)
    }
    fun emitFillAnswerTagFilter(value: String) { _fillAnswerTagFilter.value = value }
    fun emitPracticeMemoryMode(enabled: Boolean) { _practiceMemoryMode.value = enabled }
    fun emitPracticeMemoryBatchSize(count: Int) { _practiceMemoryBatchSize.value = count }
    fun emitPracticeMemoryWrongMode(mode: Int) { _practiceMemoryWrongMode.value = mode }
    fun emitPracticeMemoryPoolMode(mode: Int) { _practiceMemoryPoolMode.value = mode }
    fun emitExamMemoryMode(enabled: Boolean) { _examMemoryMode.value = enabled }
    fun emitExamMemoryBatchSize(count: Int) { _examMemoryBatchSize.value = count }
    fun emitExamMemoryWrongMode(mode: Int) { _examMemoryWrongMode.value = mode }
    fun emitExamMemoryPoolMode(mode: Int) { _examMemoryPoolMode.value = mode }
    fun emitSoundEnabled(enabled: Boolean) { _soundEnabled.value = enabled }
    fun emitDarkTheme(enabled: Boolean) { _darkTheme.value = enabled }
    fun emitSettingsReady() { _settingsReady.value = true }

    // --- Batch load from DataStore snapshot (requires coroutine context) ---

    suspend fun loadFontSettings() {
        val snapshot = fontSettings.readSettingsSnapshot()
        _fontSize.value = snapshot.fontSize
        _fontStyle.value = snapshot.fontStyle
        _examQuestionCount.value = snapshot.examQuestionCount
        _practiceQuestionCount.value = snapshot.practiceQuestionCount
        _randomPractice.value = snapshot.randomPractice
        _randomExam.value = snapshot.randomExam
        _correctDelay.value = snapshot.correctDelay
        _wrongDelay.value = snapshot.wrongDelay
        _examDelay.value = snapshot.examDelay
        _fillBlankCount.value = snapshot.fillBlankCount
        _randomFillBlanks.value = snapshot.randomFillBlanks
        _fillQuestionGenerationMode.value = snapshot.fillQuestionGenerationMode
        if (_fillQuestionGenerationMode.value == FillQuestionGenerationMode.FULL_ANSWER && _fillBlankCount.value <= 0) {
            _fillBlankCount.value = 1
        }
        _fillFullAnswerRandomOrder.value = snapshot.fillFullAnswerRandomOrder
        _fillFullAnswerRequireCorrect.value = snapshot.fillFullAnswerRequireCorrect
        _fillAnswerScoreMin.value = snapshot.fillAnswerScoreMin
        _fillAnswerScoreMax.value = snapshot.fillAnswerScoreMax.coerceAtLeast(snapshot.fillAnswerScoreMin)
        _fillAnswerTagFilter.value = snapshot.fillAnswerTagFilter
        _practiceMemoryMode.value = snapshot.practiceMemoryMode
        _practiceMemoryBatchSize.value = snapshot.practiceMemoryBatchSize
        _practiceMemoryWrongMode.value = snapshot.practiceMemoryWrongMode
        _practiceMemoryPoolMode.value = snapshot.practiceMemoryPoolMode
        _examMemoryMode.value = snapshot.examMemoryMode
        _examMemoryBatchSize.value = snapshot.examMemoryBatchSize
        _examMemoryWrongMode.value = snapshot.examMemoryWrongMode
        _examMemoryPoolMode.value = snapshot.examMemoryPoolMode
        _soundEnabled.value = snapshot.soundEnabled
        _darkTheme.value = snapshot.darkTheme
        _settingsReady.value = true
    }

    // --- Collections startup ---

    fun ensureCollectionsStarted(
        questionRepository: QuestionRepository,
        wrongBookRepository: WrongBookRepository,
        favoriteRepository: FavoriteQuestionRepository,
        fillFilter: FillQuestionFilterCoordinator,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.Default) {
            questionRepository.getQuestionFileNames().collect { names ->
                _quizFileNames.value = names
            }
        }
        scope.launch(Dispatchers.Default) {
            questionRepository.getQuestions().collect { questions ->
                cachedQuestionsSnapshot = questions
                _availableFillAnswerTags.value = fillFilter.refreshTags(questions)
                refreshFillSummaryLocked(fillFilter, questions)
            }
        }
        scope.launch(Dispatchers.Default) {
            wrongBookRepository.getAll().collect { wrongs ->
                _wrongBookFileNames.value = wrongs.mapNotNull { it.question.fileName }.distinct().sorted()
            }
        }
        scope.launch(Dispatchers.Default) {
            favoriteRepository.getAll().collect { favorites ->
                _favoriteFileNames.value = favorites.mapNotNull { it.question.fileName }.distinct().sorted()
            }
        }
    }

    // --- Fill filter summary refresh (async) ---

    fun refreshFillQuestionFilterSummary(
        fillFilter: FillQuestionFilterCoordinator,
        questionRepository: QuestionRepository,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.Default) {
            val questions = cachedQuestionsSnapshot ?: questionRepository.getQuestions().first().also {
                cachedQuestionsSnapshot = it
            }
            refreshFillSummaryLocked(fillFilter, questions)
        }
    }

    private fun refreshFillSummaryLocked(
        fillFilter: FillQuestionFilterCoordinator,
        questions: List<Question>
    ) {
        _fillQuestionFilterSummary.value = fillFilter.publish(
            questions, _fillBlankCount.value,
            _fillQuestionGenerationMode.value, _fillFullAnswerRandomOrder.value,
            _fillAnswerScoreMin.value, _fillAnswerScoreMax.value, _fillAnswerTagFilter.value
        )
    }
}

