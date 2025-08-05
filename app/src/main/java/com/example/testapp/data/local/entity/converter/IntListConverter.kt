package com.example.testapp.data.local.entity.converter

import androidx.room.TypeConverter

class IntListConverter {
    @TypeConverter
    fun fromList(list: List<Int>?): String = list?.joinToString(",") ?: ""

    @TypeConverter
    fun toList(data: String?): List<Int> =
        if (data.isNullOrEmpty()) emptyList() else data.split(",").map { it.toInt() }
}
