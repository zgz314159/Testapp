package com.example.testapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "font_settings")

object FontSettingsDataStore {
    private val FONT_SIZE_KEY = floatPreferencesKey("font_size")
    private val FONT_STYLE_KEY = stringPreferencesKey("font_style")

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
}
