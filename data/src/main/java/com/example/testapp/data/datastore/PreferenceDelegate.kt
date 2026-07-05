package com.example.testapp.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferenceDelegate<T>(
    private val key: Preferences.Key<T>,
    private val default: T
) {
    fun get(context: Context, defaultOverride: T = default): Flow<T> =
        context.dataStore.data.map { it[key] ?: defaultOverride }

    suspend fun set(context: Context, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}

class BooleanPreferenceDelegate(
    private val key: Preferences.Key<Int>,
    private val default: Boolean
) {
    fun get(context: Context, defaultOverride: Boolean = default): Flow<Boolean> =
        context.dataStore.data.map { (it[key] ?: if (defaultOverride) 1 else 0) != 0 }

    suspend fun set(context: Context, value: Boolean) {
        context.dataStore.edit { it[key] = if (value) 1 else 0 }
    }
}
