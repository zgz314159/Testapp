package com.example.testapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.testapp.core.util.FillQuestionGenerationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "font_settings")

object FontSettingsDataStore {
    private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
    private val FONT_STYLE_KEY = stringPreferencesKey("font_style")
    private val EXAM_COUNT_KEY = intPreferencesKey("exam_question_count")
    private val PRACTICE_COUNT_KEY = intPreferencesKey("practice_question_count")
    private val RANDOM_PRACTICE_KEY = intPreferencesKey("random_practice")
    private val RANDOM_EXAM_KEY = intPreferencesKey("random_exam")
    private val CORRECT_DELAY_KEY = intPreferencesKey("correct_delay")
    private val WRONG_DELAY_KEY = intPreferencesKey("wrong_delay")
    private val EXAM_DELAY_KEY = intPreferencesKey("exam_delay")
    private val SOUND_ENABLED_KEY = intPreferencesKey("sound_enabled")
    private val DARK_THEME_KEY = intPreferencesKey("dark_theme")
    private val LAST_SELECTED_FILE_KEY = stringPreferencesKey("last_selected_file")
    private val LAST_SELECTED_NAV_KEY = intPreferencesKey("last_selected_nav")
    private val RECENT_SELECTED_FILES_KEY = stringPreferencesKey("recent_selected_files")
    private val PRACTICE_FONT_SIZE_KEY = floatPreferencesKey("practice_font_size")
    private val PRACTICE_LINE_SPACING_KEY = floatPreferencesKey("practice_line_spacing")
    private val PRACTICE_LETTER_SPACING_KEY = floatPreferencesKey("practice_letter_spacing")
    private val EXAM_FONT_SIZE_KEY = floatPreferencesKey("exam_font_size")
    private val DEEPSEEK_FONT_SIZE_KEY = floatPreferencesKey("deepseek_font_size")
    private val SPARK_FONT_SIZE_KEY = floatPreferencesKey("spark_font_size")
    private val BAIDU_FONT_SIZE_KEY = floatPreferencesKey("baidu_font_size")
    private val CUMULATIVE_EXAM_COUNT_KEY = intPreferencesKey("cumulative_exam_count")
    private val FILL_BLANK_COUNT_KEY = intPreferencesKey("fill_blank_count")
    private val FILL_GENERATION_MODE_KEY = stringPreferencesKey("fill_generation_mode")
    private val FILL_FULL_ANSWER_RANDOM_ORDER_KEY = intPreferencesKey("fill_full_answer_random_order")
    private val FILL_FULL_ANSWER_REQUIRE_CORRECT_KEY = intPreferencesKey("fill_full_answer_require_correct")
    private val FILL_ANSWER_SCORE_MIN_KEY = intPreferencesKey("fill_answer_score_min")
    private val FILL_ANSWER_SCORE_MAX_KEY = intPreferencesKey("fill_answer_score_max")
    private val FILL_ANSWER_TAG_FILTER_KEY = stringPreferencesKey("fill_answer_tag_filter")
    private val EXAM_LINE_SPACING_KEY = floatPreferencesKey("exam_line_spacing")
    private val PRACTICE_MEMORY_MODE_KEY = intPreferencesKey("practice_memory_mode")
    private val PRACTICE_MEMORY_BATCH_SIZE_KEY = intPreferencesKey("practice_memory_batch_size")
    private val PRACTICE_MEMORY_WRONG_MODE_KEY = intPreferencesKey("practice_memory_wrong_mode")
    private val PRACTICE_MEMORY_POOL_MODE_KEY = intPreferencesKey("practice_memory_pool_mode")
    private val EXAM_MEMORY_MODE_KEY = intPreferencesKey("exam_memory_mode")
    private val EXAM_MEMORY_BATCH_SIZE_KEY = intPreferencesKey("exam_memory_batch_size")
    private val EXAM_MEMORY_WRONG_MODE_KEY = intPreferencesKey("exam_memory_wrong_mode")
    private val EXAM_MEMORY_POOL_MODE_KEY = intPreferencesKey("exam_memory_pool_mode")

    private val fontSize = PreferenceDelegate(FONT_SIZE_KEY, 18f)
    private val fontStyle = PreferenceDelegate(FONT_STYLE_KEY, "Normal")
    private val examQuestionCount = PreferenceDelegate(EXAM_COUNT_KEY, 10)
    private val practiceQuestionCount = PreferenceDelegate(PRACTICE_COUNT_KEY, 0)
    private val randomPractice = BooleanPreferenceDelegate(RANDOM_PRACTICE_KEY, false)
    private val randomExam = BooleanPreferenceDelegate(RANDOM_EXAM_KEY, true)
    private val correctDelay = PreferenceDelegate(CORRECT_DELAY_KEY, 1)
    private val wrongDelay = PreferenceDelegate(WRONG_DELAY_KEY, 2)
    private val examDelay = PreferenceDelegate(EXAM_DELAY_KEY, 1)
    private val soundEnabled = BooleanPreferenceDelegate(SOUND_ENABLED_KEY, true)
    private val darkTheme = BooleanPreferenceDelegate(DARK_THEME_KEY, false)
    private val lastSelectedFile = PreferenceDelegate(LAST_SELECTED_FILE_KEY, "")
    private val lastSelectedNav = PreferenceDelegate(LAST_SELECTED_NAV_KEY, 3)
    private val practiceFontSize = PreferenceDelegate(PRACTICE_FONT_SIZE_KEY, 18f)
    private val practiceLineSpacing = PreferenceDelegate(PRACTICE_LINE_SPACING_KEY, 1.3f)
    private val practiceLetterSpacing = PreferenceDelegate(PRACTICE_LETTER_SPACING_KEY, 0f)
    private val examFontSize = PreferenceDelegate(EXAM_FONT_SIZE_KEY, 18f)
    private val deepSeekFontSize = PreferenceDelegate(DEEPSEEK_FONT_SIZE_KEY, 18f)
    private val sparkFontSize = PreferenceDelegate(SPARK_FONT_SIZE_KEY, 18f)
    private val baiduFontSize = PreferenceDelegate(BAIDU_FONT_SIZE_KEY, 18f)
    private val cumulativeExamCount = PreferenceDelegate(CUMULATIVE_EXAM_COUNT_KEY, 0)
    private val fillBlankCount = PreferenceDelegate(FILL_BLANK_COUNT_KEY, 4)
    private val fillFullAnswerRandomOrder = BooleanPreferenceDelegate(FILL_FULL_ANSWER_RANDOM_ORDER_KEY, true)
    private val fillFullAnswerRequireCorrect = BooleanPreferenceDelegate(FILL_FULL_ANSWER_REQUIRE_CORRECT_KEY, false)
    private val fillAnswerScoreMin = PreferenceDelegate(FILL_ANSWER_SCORE_MIN_KEY, 0)
    private val fillAnswerScoreMax = PreferenceDelegate(FILL_ANSWER_SCORE_MAX_KEY, 10)
    private val fillAnswerTagFilter = PreferenceDelegate(FILL_ANSWER_TAG_FILTER_KEY, "")
    private val examLineSpacing = PreferenceDelegate(EXAM_LINE_SPACING_KEY, 1.0f)
    private val practiceMemoryMode = PreferenceDelegate(PRACTICE_MEMORY_MODE_KEY, 0)
    private val practiceMemoryBatchSize = PreferenceDelegate(PRACTICE_MEMORY_BATCH_SIZE_KEY, 10)
    private val practiceMemoryWrongMode = PreferenceDelegate(PRACTICE_MEMORY_WRONG_MODE_KEY, 0)
    private val practiceMemoryPoolMode = PreferenceDelegate(PRACTICE_MEMORY_POOL_MODE_KEY, 0)
    private val examMemoryMode = PreferenceDelegate(EXAM_MEMORY_MODE_KEY, 0)
    private val examMemoryBatchSize = PreferenceDelegate(EXAM_MEMORY_BATCH_SIZE_KEY, 10)
    private val examMemoryWrongMode = PreferenceDelegate(EXAM_MEMORY_WRONG_MODE_KEY, 0)
    private val examMemoryPoolMode = PreferenceDelegate(EXAM_MEMORY_POOL_MODE_KEY, 0)

    fun getFontSize(context: Context, default: Float = 18f): Flow<Float> = fontSize.get(context, default)
    suspend fun setFontSize(context: Context, size: Float) = fontSize.set(context, size)
    fun getFontStyle(context: Context, default: String = "Normal"): Flow<String> = fontStyle.get(context, default)
    suspend fun setFontStyle(context: Context, style: String) = fontStyle.set(context, style)
    fun getExamQuestionCount(context: Context, default: Int = 10): Flow<Int> = examQuestionCount.get(context, default)
    suspend fun setExamQuestionCount(context: Context, count: Int) = examQuestionCount.set(context, count)
    fun getPracticeQuestionCount(context: Context, default: Int = 0): Flow<Int> = practiceQuestionCount.get(context, default)
    suspend fun setPracticeQuestionCount(context: Context, count: Int) = practiceQuestionCount.set(context, count)
    fun getRandomPractice(context: Context, default: Boolean = false): Flow<Boolean> = randomPractice.get(context, default)
    fun getRandomExam(context: Context, default: Boolean = true): Flow<Boolean> = randomExam.get(context, default)
    suspend fun setRandomPractice(context: Context, enabled: Boolean) = randomPractice.set(context, enabled)
    suspend fun setRandomExam(context: Context, enabled: Boolean) = randomExam.set(context, enabled)
    fun getCorrectDelay(context: Context, default: Int = 1): Flow<Int> = correctDelay.get(context, default)
    suspend fun setCorrectDelay(context: Context, delay: Int) = correctDelay.set(context, delay)
    fun getWrongDelay(context: Context, default: Int = 2): Flow<Int> = wrongDelay.get(context, default)
    suspend fun setWrongDelay(context: Context, delay: Int) = wrongDelay.set(context, delay)
    fun getExamDelay(context: Context, default: Int = 1): Flow<Int> = examDelay.get(context, default)
    suspend fun setExamDelay(context: Context, delay: Int) = examDelay.set(context, delay)
    fun getSoundEnabled(context: Context, default: Boolean = true): Flow<Boolean> = soundEnabled.get(context, default)
    suspend fun setSoundEnabled(context: Context, enabled: Boolean) = soundEnabled.set(context, enabled)
    fun getDarkTheme(context: Context, default: Boolean = false): Flow<Boolean> = darkTheme.get(context, default)
    suspend fun setDarkTheme(context: Context, enabled: Boolean) = darkTheme.set(context, enabled)
    fun getLastSelectedFile(context: Context, default: String = ""): Flow<String> = lastSelectedFile.get(context, default)
    suspend fun setLastSelectedFile(context: Context, fileName: String) = lastSelectedFile.set(context, fileName)
    fun getLastSelectedNav(context: Context, default: Int = 3): Flow<Int> = lastSelectedNav.get(context, default)
    suspend fun setLastSelectedNav(context: Context, index: Int) = lastSelectedNav.set(context, index)
    fun getRecentSelectedFiles(context: Context, default: List<String> = emptyList()): Flow<List<String>> =
        context.dataStore.data.map { preferences ->
            preferences[RECENT_SELECTED_FILES_KEY]
                ?.split('|')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: default
        }
    suspend fun setRecentSelectedFiles(context: Context, fileNames: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[RECENT_SELECTED_FILES_KEY] = fileNames.joinToString("|")
        }
    }
    suspend fun markFileAsRecent(context: Context, fileName: String) {
        context.dataStore.edit { preferences ->
            val existing = preferences[RECENT_SELECTED_FILES_KEY]
                ?.split('|')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() && it != fileName }
                ?: emptyList()
            val updated = buildList {
                add(fileName)
                addAll(existing)
            }
            preferences[RECENT_SELECTED_FILES_KEY] = updated.joinToString("|")
        }
    }
    fun getPracticeFontSize(context: Context, default: Float = 18f): Flow<Float> = practiceFontSize.get(context, default)
    suspend fun setPracticeFontSize(context: Context, size: Float) = practiceFontSize.set(context, size)
    fun getPracticeLineSpacing(context: Context, default: Float = 1.3f): Flow<Float> = practiceLineSpacing.get(context, default)
    suspend fun setPracticeLineSpacing(context: Context, value: Float) = practiceLineSpacing.set(context, value)
    fun getPracticeLetterSpacing(context: Context, default: Float = 0f): Flow<Float> = practiceLetterSpacing.get(context, default)
    suspend fun setPracticeLetterSpacing(context: Context, value: Float) = practiceLetterSpacing.set(context, value)
    fun getExamFontSize(context: Context, default: Float = 18f): Flow<Float> = examFontSize.get(context, default)
    suspend fun setExamFontSize(context: Context, size: Float) = examFontSize.set(context, size)
    fun getDeepSeekFontSize(context: Context, default: Float = 18f): Flow<Float> = deepSeekFontSize.get(context, default)
    suspend fun setDeepSeekFontSize(context: Context, size: Float) = deepSeekFontSize.set(context, size)
    fun getSparkFontSize(context: Context, default: Float = 18f): Flow<Float> = sparkFontSize.get(context, default)
    suspend fun setSparkFontSize(context: Context, size: Float) = sparkFontSize.set(context, size)
    fun getBaiduFontSize(context: Context, default: Float = 18f): Flow<Float> = baiduFontSize.get(context, default)
    suspend fun setBaiduFontSize(context: Context, size: Float) = baiduFontSize.set(context, size)
    fun getCumulativeExamCount(context: Context, default: Int = 0): Flow<Int> = cumulativeExamCount.get(context, default)
    suspend fun setCumulativeExamCount(context: Context, count: Int) = cumulativeExamCount.set(context, count)
    fun getFillBlankCount(context: Context, default: Int = 4): Flow<Int> = fillBlankCount.get(context, default)
    suspend fun setFillBlankCount(context: Context, count: Int) = fillBlankCount.set(context, count)

    fun getFillQuestionGenerationMode(context: Context, default: FillQuestionGenerationMode = FillQuestionGenerationMode.SCORE_RANGE_RANDOM): Flow<FillQuestionGenerationMode> =
        context.dataStore.data.map { preferences ->
            val str = preferences[FILL_GENERATION_MODE_KEY] ?: default.storageValue
            FillQuestionGenerationMode.fromStorageValue(str)
        }

    suspend fun setFillQuestionGenerationMode(context: Context, mode: FillQuestionGenerationMode) {
        context.dataStore.edit { preferences -> preferences[FILL_GENERATION_MODE_KEY] = mode.storageValue }
    }

    fun getFillFullAnswerRandomOrder(context: Context, default: Boolean = true): Flow<Boolean> =
        fillFullAnswerRandomOrder.get(context, default)
    suspend fun setFillFullAnswerRandomOrder(context: Context, value: Boolean) =
        fillFullAnswerRandomOrder.set(context, value)
    fun getFillFullAnswerRequireCorrect(context: Context, default: Boolean = false): Flow<Boolean> =
        fillFullAnswerRequireCorrect.get(context, default)
    suspend fun setFillFullAnswerRequireCorrect(context: Context, value: Boolean) =
        fillFullAnswerRequireCorrect.set(context, value)
    fun getFillAnswerScoreMin(context: Context, default: Int = 0): Flow<Int> = fillAnswerScoreMin.get(context, default)
    suspend fun setFillAnswerScoreMin(context: Context, value: Int) = fillAnswerScoreMin.set(context, value)
    fun getFillAnswerScoreMax(context: Context, default: Int = 10): Flow<Int> = fillAnswerScoreMax.get(context, default)
    suspend fun setFillAnswerScoreMax(context: Context, value: Int) = fillAnswerScoreMax.set(context, value)
    fun getFillAnswerTagFilter(context: Context, default: String = ""): Flow<String> =
        fillAnswerTagFilter.get(context, default)
    suspend fun setFillAnswerTagFilter(context: Context, value: String) = fillAnswerTagFilter.set(context, value)
    fun getExamLineSpacing(context: Context, default: Float = 1.0f): Flow<Float> =
        examLineSpacing.get(context, default)
    suspend fun setExamLineSpacing(context: Context, value: Float) = examLineSpacing.set(context, value)
    fun getPracticeMemoryMode(context: Context, default: Int = 0): Flow<Int> = practiceMemoryMode.get(context, default)
    suspend fun setPracticeMemoryMode(context: Context, value: Int) = practiceMemoryMode.set(context, value)
    fun getPracticeMemoryBatchSize(context: Context, default: Int = 10): Flow<Int> =
        practiceMemoryBatchSize.get(context, default)
    suspend fun setPracticeMemoryBatchSize(context: Context, value: Int) =
        practiceMemoryBatchSize.set(context, value)
    fun getPracticeMemoryWrongMode(context: Context, default: Int = 0): Flow<Int> =
        practiceMemoryWrongMode.get(context, default)
    suspend fun setPracticeMemoryWrongMode(context: Context, value: Int) = practiceMemoryWrongMode.set(context, value)
    fun getPracticeMemoryPoolMode(context: Context, default: Int = 0): Flow<Int> =
        practiceMemoryPoolMode.get(context, default)
    suspend fun setPracticeMemoryPoolMode(context: Context, value: Int) = practiceMemoryPoolMode.set(context, value)
    fun getExamMemoryMode(context: Context, default: Int = 0): Flow<Int> = examMemoryMode.get(context, default)
    suspend fun setExamMemoryMode(context: Context, value: Int) = examMemoryMode.set(context, value)
    fun getExamMemoryBatchSize(context: Context, default: Int = 10): Flow<Int> =
        examMemoryBatchSize.get(context, default)
    suspend fun setExamMemoryBatchSize(context: Context, value: Int) = examMemoryBatchSize.set(context, value)
    fun getExamMemoryWrongMode(context: Context, default: Int = 0): Flow<Int> =
        examMemoryWrongMode.get(context, default)
    suspend fun setExamMemoryWrongMode(context: Context, value: Int) = examMemoryWrongMode.set(context, value)
    fun getExamMemoryPoolMode(context: Context, default: Int = 0): Flow<Int> =
        examMemoryPoolMode.get(context, default)
    suspend fun setExamMemoryPoolMode(context: Context, value: Int) = examMemoryPoolMode.set(context, value)

    suspend fun readSettingsSnapshot(context: Context): SettingsSnapshot {
        val it = context.dataStore.data.first()
        return SettingsSnapshot(
            fontSize = it[FONT_SIZE_KEY] ?: 18f,
            fontStyle = it[FONT_STYLE_KEY] ?: "Normal",
            examQuestionCount = it[EXAM_COUNT_KEY] ?: 10,
            practiceQuestionCount = it[PRACTICE_COUNT_KEY] ?: 0,
            randomPractice = (it[RANDOM_PRACTICE_KEY] ?: 0) != 0,
            randomExam = (it[RANDOM_EXAM_KEY] ?: 1) != 0,
            correctDelay = it[CORRECT_DELAY_KEY] ?: 1,
            wrongDelay = it[WRONG_DELAY_KEY] ?: 2,
            examDelay = it[EXAM_DELAY_KEY] ?: 1,
            soundEnabled = (it[SOUND_ENABLED_KEY] ?: 1) != 0,
            darkTheme = (it[DARK_THEME_KEY] ?: 0) != 0,
            fillBlankCount = it[FILL_BLANK_COUNT_KEY] ?: 4,
            randomFillBlanks = false,
            fillQuestionGenerationMode = FillQuestionGenerationMode.fromStorageValue(it[FILL_GENERATION_MODE_KEY]),
            fillFullAnswerRandomOrder = (it[FILL_FULL_ANSWER_RANDOM_ORDER_KEY] ?: 1) != 0,
            fillFullAnswerRequireCorrect = (it[FILL_FULL_ANSWER_REQUIRE_CORRECT_KEY] ?: 0) != 0,
            fillAnswerScoreMin = it[FILL_ANSWER_SCORE_MIN_KEY] ?: 1,
            fillAnswerScoreMax = it[FILL_ANSWER_SCORE_MAX_KEY] ?: 10,
            fillAnswerTagFilter = it[FILL_ANSWER_TAG_FILTER_KEY] ?: "",
            practiceMemoryMode = (it[PRACTICE_MEMORY_MODE_KEY] ?: 0) != 0,
            practiceMemoryBatchSize = it[PRACTICE_MEMORY_BATCH_SIZE_KEY] ?: 10,
            practiceMemoryWrongMode = it[PRACTICE_MEMORY_WRONG_MODE_KEY] ?: 0,
            practiceMemoryPoolMode = it[PRACTICE_MEMORY_POOL_MODE_KEY] ?: 0,
            examMemoryMode = (it[EXAM_MEMORY_MODE_KEY] ?: 0) != 0,
            examMemoryBatchSize = it[EXAM_MEMORY_BATCH_SIZE_KEY] ?: 10,
            examMemoryWrongMode = it[EXAM_MEMORY_WRONG_MODE_KEY] ?: 0,
            examMemoryPoolMode = it[EXAM_MEMORY_POOL_MODE_KEY] ?: 0
        )
    }

    data class SettingsSnapshot(
        val fontSize: Float,
        val fontStyle: String,
        val examQuestionCount: Int,
        val practiceQuestionCount: Int,
        val randomPractice: Boolean,
        val randomExam: Boolean,
        val correctDelay: Int,
        val wrongDelay: Int,
        val examDelay: Int,
        val soundEnabled: Boolean,
        val darkTheme: Boolean,
        val fillBlankCount: Int,
        val randomFillBlanks: Boolean,
        val fillQuestionGenerationMode: FillQuestionGenerationMode,
        val fillFullAnswerRandomOrder: Boolean,
        val fillFullAnswerRequireCorrect: Boolean,
        val fillAnswerScoreMin: Int,
        val fillAnswerScoreMax: Int,
        val fillAnswerTagFilter: String,
        val practiceMemoryMode: Boolean,
        val practiceMemoryBatchSize: Int,
        val practiceMemoryWrongMode: Int,
        val practiceMemoryPoolMode: Int,
        val examMemoryMode: Boolean,
        val examMemoryBatchSize: Int,
        val examMemoryWrongMode: Int,
        val examMemoryPoolMode: Int
    )
}

