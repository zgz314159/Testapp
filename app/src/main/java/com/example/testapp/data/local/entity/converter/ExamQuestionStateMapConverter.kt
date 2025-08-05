package com.example.testapp.data.local.entity.converter

import androidx.room.TypeConverter
import com.example.testapp.domain.model.ExamQuestionState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class ExamQuestionStateMapConverter {
    @TypeConverter
    fun fromMap(map: Map<Int, ExamQuestionState>?): String {
        if (map == null) return ""
        return Json.encodeToString(map)
    }

    @TypeConverter
    fun toMap(data: String?): Map<Int, ExamQuestionState> {
        if (data.isNullOrBlank()) return emptyMap()
        
        return try {
            Json.decodeFromString(data)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
