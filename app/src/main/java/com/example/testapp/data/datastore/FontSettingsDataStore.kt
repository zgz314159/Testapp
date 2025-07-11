package com.example.testapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "font_settings")

object FontSettingsDataStore {
    private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
    private val FONT_STYLE_KEY = stringPreferencesKey("font_style")
    private val EXAM_COUNT_KEY = intPreferencesKey("exam_question_count")
    private val RANDOM_PRACTICE_KEY = intPreferencesKey("random_practice")
    private val RANDOM_EXAM_KEY = intPreferencesKey("random_exam")
    private val CORRECT_DELAY_KEY = intPreferencesKey("correct_delay")
    private val WRONG_DELAY_KEY = intPreferencesKey("wrong_delay")
    private val EXAM_DELAY_KEY = intPreferencesKey("exam_delay")
    fun getFontSize(context: Context, default: Float = 18f): Flow<Float> =
        context.dataStore.data.map { preferences -> preferences[FONT_SIZE_KEY] ?: default }

    suspend fun setFontSize(context: Context, size: Float) {
        context.dataStore.edit { preferences -> preferences[FONT_SIZE_KEY] = size }
    }

    fun getFontStyle(context: Context, default: String = "Normal"): Flow<String> =
        context.dataStore.data.map { preferences -> preferences[FONT_STYLE_KEY] ?: default }

    suspend fun setFontStyle(context: Context, style: String) {
        context.dataStore.edit { preferences -> preferences[FONT_STYLE_KEY] = style }
    }
    fun getExamQuestionCount(context: Context, default: Int = 10): Flow<Int> =
        context.dataStore.data.map { preferences -> preferences[EXAM_COUNT_KEY] ?: default }

    suspend fun setExamQuestionCount(context: Context, count: Int) {
        context.dataStore.edit { preferences -> preferences[EXAM_COUNT_KEY] = count }
    }
    fun getRandomPractice(context: Context, default: Boolean = false): Flow<Boolean> =
        context.dataStore.data.map { preferences -> (preferences[RANDOM_PRACTICE_KEY] ?: if (default) 1 else 0) != 0 }

    fun getRandomExam(context: Context, default: Boolean = true): Flow<Boolean> =
        context.dataStore.data.map { preferences -> (preferences[RANDOM_EXAM_KEY] ?: if (default) 1 else 0) != 0 }

    suspend fun setRandomPractice(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[RANDOM_PRACTICE_KEY] = if (enabled) 1 else 0 }
    }

    suspend fun setRandomExam(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[RANDOM_EXAM_KEY] = if (enabled) 1 else 0 }
    }

    fun getCorrectDelay(context: Context, default: Int = 1): Flow<Int> =
        context.dataStore.data.map { preferences -> preferences[CORRECT_DELAY_KEY] ?: default }

    suspend fun setCorrectDelay(context: Context, delay: Int) {
        context.dataStore.edit { preferences -> preferences[CORRECT_DELAY_KEY] = delay }
    }

    fun getWrongDelay(context: Context, default: Int = 2): Flow<Int> =
        context.dataStore.data.map { preferences -> preferences[WRONG_DELAY_KEY] ?: default }

    suspend fun setWrongDelay(context: Context, delay: Int) {
        context.dataStore.edit { preferences -> preferences[WRONG_DELAY_KEY] = delay }
    }
    fun getExamDelay(context: Context, default: Int = 1): Flow<Int> =
        context.dataStore.data.map { preferences -> preferences[EXAM_DELAY_KEY] ?: default }

    suspend fun setExamDelay(context: Context, delay: Int) {
        context.dataStore.edit { preferences -> preferences[EXAM_DELAY_KEY] = delay }
    }
}
