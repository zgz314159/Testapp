package com.example.testapp.data.local.entity.converter

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StringListConverter {
    @TypeConverter
    fun fromList(list: List<String>?): String =
        if (list == null) "" else Json.encodeToString(list)

    @TypeConverter
    fun toList(data: String?): List<String> =
        if (data.isNullOrBlank()) emptyList() else Json.decodeFromString(data)
}
