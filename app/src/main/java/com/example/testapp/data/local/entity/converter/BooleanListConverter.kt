package com.example.testapp.data.local.entity.converter

import androidx.room.TypeConverter

class BooleanListConverter {
    @TypeConverter
    fun fromList(list: List<Boolean>?): String =
        list?.joinToString(",") { if (it) "1" else "0" } ?: ""

    @TypeConverter
    fun toList(data: String?): List<Boolean> =
        if (data.isNullOrBlank()) emptyList()
        else data.split(",").map { it == "1" }
}

