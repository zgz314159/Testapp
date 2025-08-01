﻿package com.example.testapp.data.datastore

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

    fun getFontSize(context: Context, default: Float = 18f): Flow<Float> =
        context.dataStore.data.map { preferences ->
            val value = preferences[FONT_SIZE_KEY] ?: default
            
            value
        }

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

    fun getPracticeQuestionCount(context: Context, default: Int = 0): Flow<Int> =
        context.dataStore.data.map { preferences -> preferences[PRACTICE_COUNT_KEY] ?: default }

    suspend fun setPracticeQuestionCount(context: Context, count: Int) {
        context.dataStore.edit { preferences -> preferences[PRACTICE_COUNT_KEY] = count }
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

    fun getSoundEnabled(context: Context, default: Boolean = true): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            (preferences[SOUND_ENABLED_KEY] ?: if (default) 1 else 0) != 0
        }

    suspend fun setSoundEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[SOUND_ENABLED_KEY] = if (enabled) 1 else 0 }
    }

    fun getDarkTheme(context: Context, default: Boolean = false): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            (preferences[DARK_THEME_KEY] ?: if (default) 1 else 0) != 0
        }

    suspend fun setDarkTheme(context: Context, enabled: Boolean) {
        context.dataStore.edit { preferences -> preferences[DARK_THEME_KEY] = if (enabled) 1 else 0 }
    }

    fun getLastSelectedFile(context: Context, default: String = ""): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[LAST_SELECTED_FILE_KEY] ?: default
        }

    suspend fun setLastSelectedFile(context: Context, fileName: String) {
        context.dataStore.edit { preferences -> preferences[LAST_SELECTED_FILE_KEY] = fileName }
    }

    fun getLastSelectedNav(context: Context, default: Int = 3): Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[LAST_SELECTED_NAV_KEY] ?: default
        }

    suspend fun setLastSelectedNav(context: Context, index: Int) {
        context.dataStore.edit { preferences -> preferences[LAST_SELECTED_NAV_KEY] = index }
    }

    fun getPracticeFontSize(context: Context, default: Float = 18f): Flow<Float> =
        context.dataStore.data.map { preferences ->
            val value = preferences[PRACTICE_FONT_SIZE_KEY] ?: default
            
            value
        }

    suspend fun setPracticeFontSize(context: Context, size: Float) {
        
        context.dataStore.edit { preferences -> preferences[PRACTICE_FONT_SIZE_KEY] = size }
    }

    fun getExamFontSize(context: Context, default: Float = 18f): Flow<Float> =
        context.dataStore.data.map { preferences ->
            val value = preferences[EXAM_FONT_SIZE_KEY] ?: default
            
            value
        }

    suspend fun setExamFontSize(context: Context, size: Float) {
        
        context.dataStore.edit { preferences -> preferences[EXAM_FONT_SIZE_KEY] = size }
    }

    fun getDeepSeekFontSize(context: Context, default: Float = 18f): Flow<Float> =
        context.dataStore.data.map { preferences ->
            val value = preferences[DEEPSEEK_FONT_SIZE_KEY] ?: default
            
            value
        }

    suspend fun setDeepSeekFontSize(context: Context, size: Float) {
        
        context.dataStore.edit { preferences -> preferences[DEEPSEEK_FONT_SIZE_KEY] = size }
    }

    fun getSparkFontSize(context: Context, default: Float = 18f): Flow<Float> =
        context.dataStore.data.map { preferences ->
            val value = preferences[SPARK_FONT_SIZE_KEY] ?: default
            
            value
        }

    suspend fun setSparkFontSize(context: Context, size: Float) {
        
        context.dataStore.edit { preferences -> preferences[SPARK_FONT_SIZE_KEY] = size }
    }
    
    fun getBaiduFontSize(context: Context, default: Float = 18f): Flow<Float> =
        context.dataStore.data.map { preferences ->
            val value = preferences[BAIDU_FONT_SIZE_KEY] ?: default
            
            value
        }

    suspend fun setBaiduFontSize(context: Context, size: Float) {
        
        context.dataStore.edit { preferences -> preferences[BAIDU_FONT_SIZE_KEY] = size }
    }

    fun getCumulativeExamCount(context: Context, default: Int = 0): Flow<Int> =
        context.dataStore.data.map { preferences ->
            val value = preferences[CUMULATIVE_EXAM_COUNT_KEY] ?: default
            
            value
        }

    suspend fun setCumulativeExamCount(context: Context, count: Int) {
        
        context.dataStore.edit { preferences -> preferences[CUMULATIVE_EXAM_COUNT_KEY] = count }
    }
}
