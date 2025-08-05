package com.example.testapp.data.local.entity.converter

import androidx.room.TypeConverter

class NestedIntListConverter {
    @TypeConverter
    fun fromList(list: List<List<Int>>?): String =
        list?.joinToString(";") { it.joinToString(",") } ?: ""

    @TypeConverter
    fun toList(data: String?): List<List<Int>> =
        if (data.isNullOrBlank()) emptyList()
        else data.split(";").map { part ->
            if (part.isBlank()) emptyList() else part.split(",").map { it.toInt() }
        }
}