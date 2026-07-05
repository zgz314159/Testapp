package com.example.testapp.data.local.entity.converter

import androidx.room.TypeConverter
import com.example.testapp.domain.model.UnifiedQuestionState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UnifiedQuestionStateMapConverter {
    @TypeConverter
    fun fromMap(map: Map<Int, UnifiedQuestionState>?): String {
        if (map == null) return ""
        return Json.encodeToString(map)
    }

    @TypeConverter
    fun toMap(data: String?): Map<Int, UnifiedQuestionState> {
        if (data.isNullOrBlank()) return emptyMap()
        return try {
            Json.decodeFromString(data)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
