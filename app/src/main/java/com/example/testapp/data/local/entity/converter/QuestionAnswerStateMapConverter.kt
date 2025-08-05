package com.example.testapp.data.local.entity.converter

import androidx.room.TypeConverter
import com.example.testapp.domain.model.QuestionAnswerState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class QuestionAnswerStateMapConverter {
    @TypeConverter
    fun fromMap(map: Map<Int, QuestionAnswerState>?): String {
        if (map == null) return ""
        return Json.encodeToString(map)
    }

    @TypeConverter
    fun toMap(data: String?): Map<Int, QuestionAnswerState> {
        if (data.isNullOrBlank()) return emptyMap()
        
        return try {
            Json.decodeFromString(data)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
