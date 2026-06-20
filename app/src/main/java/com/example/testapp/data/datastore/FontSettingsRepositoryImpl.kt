package com.example.testapp.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.FontSettingsSnapshot
import com.example.testapp.core.util.FillQuestionGenerationMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FontSettingsRepository {

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
    private val PRACTICE_FONT_SIZE_KEY = floatPreferencesKey("practice_font_size")
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

    override val fontSize: Flow<Float> = readFlow(FONT_SIZE_KEY, 18f)
    override val fontStyle: Flow<String> = readFlow(FONT_STYLE_KEY, "Normal")
    override val examQuestionCount: Flow<Int> = readFlow(EXAM_COUNT_KEY, 10)
    override val practiceQuestionCount: Flow<Int> = readFlow(PRACTICE_COUNT_KEY, 0)
    override val randomPractice: Flow<Boolean> = readBoolFlow(RANDOM_PRACTICE_KEY, false)
    override val randomExam: Flow<Boolean> = readBoolFlow(RANDOM_EXAM_KEY, true)
    override val correctDelay: Flow<Int> = readFlow(CORRECT_DELAY_KEY, 1)
    override val wrongDelay: Flow<Int> = readFlow(WRONG_DELAY_KEY, 2)
    override val examDelay: Flow<Int> = readFlow(EXAM_DELAY_KEY, 1)
    override val soundEnabled: Flow<Boolean> = readBoolFlow(SOUND_ENABLED_KEY, true)
    override val darkTheme: Flow<Boolean> = readBoolFlow(DARK_THEME_KEY, false)
    override val lastSelectedFile: Flow<String> = readFlow(LAST_SELECTED_FILE_KEY, "")
    override val lastSelectedNav: Flow<Int> = readFlow(LAST_SELECTED_NAV_KEY, 3)
    override val practiceFontSize: Flow<Float> = readFlow(PRACTICE_FONT_SIZE_KEY, 18f)
    override val examFontSize: Flow<Float> = readFlow(EXAM_FONT_SIZE_KEY, 18f)
    override val deepSeekFontSize: Flow<Float> = readFlow(DEEPSEEK_FONT_SIZE_KEY, 18f)
    override val sparkFontSize: Flow<Float> = readFlow(SPARK_FONT_SIZE_KEY, 18f)
    override val baiduFontSize: Flow<Float> = readFlow(BAIDU_FONT_SIZE_KEY, 18f)
    override val cumulativeExamCount: Flow<Int> = readFlow(CUMULATIVE_EXAM_COUNT_KEY, 0)
    override val fillBlankCount: Flow<Int> = readFlow(FILL_BLANK_COUNT_KEY, 4)
    override val fillQuestionGenerationMode: Flow<FillQuestionGenerationMode> =
        context.dataStore.data.map { prefs ->
            val str = prefs[FILL_GENERATION_MODE_KEY] ?: FillQuestionGenerationMode.SCORE_RANGE_RANDOM.storageValue
            FillQuestionGenerationMode.fromStorageValue(str)
        }
    override val fillFullAnswerRandomOrder: Flow<Boolean> = readBoolFlow(FILL_FULL_ANSWER_RANDOM_ORDER_KEY, true)
    override val fillFullAnswerRequireCorrect: Flow<Boolean> = readBoolFlow(FILL_FULL_ANSWER_REQUIRE_CORRECT_KEY, false)
    override val fillAnswerScoreMin: Flow<Int> = readFlow(FILL_ANSWER_SCORE_MIN_KEY, 0)
    override val fillAnswerScoreMax: Flow<Int> = readFlow(FILL_ANSWER_SCORE_MAX_KEY, 10)
    override val fillAnswerTagFilter: Flow<String> = readFlow(FILL_ANSWER_TAG_FILTER_KEY, "")
    override val examLineSpacing: Flow<Float> = readFlow(EXAM_LINE_SPACING_KEY, 1.0f)
    override val practiceMemoryMode: Flow<Int> = readFlow(PRACTICE_MEMORY_MODE_KEY, 0)
    override val practiceMemoryBatchSize: Flow<Int> = readFlow(PRACTICE_MEMORY_BATCH_SIZE_KEY, 10)
    override val practiceMemoryWrongMode: Flow<Int> = readFlow(PRACTICE_MEMORY_WRONG_MODE_KEY, 0)
    override val practiceMemoryPoolMode: Flow<Int> = readFlow(PRACTICE_MEMORY_POOL_MODE_KEY, 0)
    override val examMemoryMode: Flow<Int> = readFlow(EXAM_MEMORY_MODE_KEY, 0)
    override val examMemoryBatchSize: Flow<Int> = readFlow(EXAM_MEMORY_BATCH_SIZE_KEY, 10)
    override val examMemoryWrongMode: Flow<Int> = readFlow(EXAM_MEMORY_WRONG_MODE_KEY, 0)
    override val examMemoryPoolMode: Flow<Int> = readFlow(EXAM_MEMORY_POOL_MODE_KEY, 0)

    override suspend fun setFontSize(size: Float) = write(FONT_SIZE_KEY, size)
    override suspend fun setFontStyle(style: String) = write(FONT_STYLE_KEY, style)
    override suspend fun setExamQuestionCount(count: Int) = write(EXAM_COUNT_KEY, count)
    override suspend fun setPracticeQuestionCount(count: Int) = write(PRACTICE_COUNT_KEY, count)
    override suspend fun setRandomPractice(enabled: Boolean) = writeBool(RANDOM_PRACTICE_KEY, enabled)
    override suspend fun setRandomExam(enabled: Boolean) = writeBool(RANDOM_EXAM_KEY, enabled)
    override suspend fun setCorrectDelay(delay: Int) = write(CORRECT_DELAY_KEY, delay)
    override suspend fun setWrongDelay(delay: Int) = write(WRONG_DELAY_KEY, delay)
    override suspend fun setExamDelay(delay: Int) = write(EXAM_DELAY_KEY, delay)
    override suspend fun setSoundEnabled(enabled: Boolean) = writeBool(SOUND_ENABLED_KEY, enabled)
    override suspend fun setDarkTheme(enabled: Boolean) = writeBool(DARK_THEME_KEY, enabled)
    override suspend fun setLastSelectedFile(fileName: String) = write(LAST_SELECTED_FILE_KEY, fileName)
    override suspend fun setLastSelectedNav(index: Int) = write(LAST_SELECTED_NAV_KEY, index)
    override suspend fun setPracticeFontSize(size: Float) = write(PRACTICE_FONT_SIZE_KEY, size)
    override suspend fun setExamFontSize(size: Float) = write(EXAM_FONT_SIZE_KEY, size)
    override suspend fun setDeepSeekFontSize(size: Float) = write(DEEPSEEK_FONT_SIZE_KEY, size)
    override suspend fun setSparkFontSize(size: Float) = write(SPARK_FONT_SIZE_KEY, size)
    override suspend fun setBaiduFontSize(size: Float) = write(BAIDU_FONT_SIZE_KEY, size)
    override suspend fun setCumulativeExamCount(count: Int) = write(CUMULATIVE_EXAM_COUNT_KEY, count)
    override suspend fun setFillBlankCount(count: Int) = write(FILL_BLANK_COUNT_KEY, count)
    override suspend fun setFillQuestionGenerationMode(mode: FillQuestionGenerationMode) {
        context.dataStore.edit { it[FILL_GENERATION_MODE_KEY] = mode.storageValue }
    }
    override suspend fun setFillFullAnswerRandomOrder(value: Boolean) = writeBool(FILL_FULL_ANSWER_RANDOM_ORDER_KEY, value)
    override suspend fun setFillFullAnswerRequireCorrect(value: Boolean) = writeBool(FILL_FULL_ANSWER_REQUIRE_CORRECT_KEY, value)
    override suspend fun setFillAnswerScoreMin(value: Int) = write(FILL_ANSWER_SCORE_MIN_KEY, value)
    override suspend fun setFillAnswerScoreMax(value: Int) = write(FILL_ANSWER_SCORE_MAX_KEY, value)
    override suspend fun setFillAnswerTagFilter(value: String) = write(FILL_ANSWER_TAG_FILTER_KEY, value)
    override suspend fun setExamLineSpacing(value: Float) = write(EXAM_LINE_SPACING_KEY, value)
    override suspend fun setPracticeMemoryMode(value: Int) = write(PRACTICE_MEMORY_MODE_KEY, value)
    override suspend fun setPracticeMemoryBatchSize(value: Int) = write(PRACTICE_MEMORY_BATCH_SIZE_KEY, value)
    override suspend fun setPracticeMemoryWrongMode(value: Int) = write(PRACTICE_MEMORY_WRONG_MODE_KEY, value)
    override suspend fun setPracticeMemoryPoolMode(value: Int) = write(PRACTICE_MEMORY_POOL_MODE_KEY, value)
    override suspend fun setExamMemoryMode(value: Int) = write(EXAM_MEMORY_MODE_KEY, value)
    override suspend fun setExamMemoryBatchSize(value: Int) = write(EXAM_MEMORY_BATCH_SIZE_KEY, value)
    override suspend fun setExamMemoryWrongMode(value: Int) = write(EXAM_MEMORY_WRONG_MODE_KEY, value)
    override suspend fun setExamMemoryPoolMode(value: Int) = write(EXAM_MEMORY_POOL_MODE_KEY, value)

    override suspend fun readSettingsSnapshot(): FontSettingsSnapshot {
        val it = context.dataStore.data.first()
        return FontSettingsSnapshot(
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
            fillQuestionGenerationMode = FillQuestionGenerationMode.fromStorageValue(it[FILL_GENERATION_MODE_KEY] ?: ""),
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

    private fun <T> readFlow(key: Preferences.Key<T>, default: T): Flow<T> =
        context.dataStore.data.map { it[key] ?: default }

    private fun readBoolFlow(key: Preferences.Key<Int>, default: Boolean): Flow<Boolean> =
        context.dataStore.data.map { (it[key] ?: if (default) 1 else 0) != 0 }

    private suspend fun <T> write(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    private suspend fun writeBool(key: Preferences.Key<Int>, value: Boolean) {
        context.dataStore.edit { it[key] = if (value) 1 else 0 }
    }
}
