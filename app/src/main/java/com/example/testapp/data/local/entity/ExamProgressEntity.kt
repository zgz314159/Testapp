package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testapp.data.local.entity.converter.IntListConverter

@Entity(tableName = "exam_progress")
data class ExamProgressEntity(
    @PrimaryKey val id: String = "default",
    val currentIndex: Int,
    @TypeConverters(IntListConverter::class)
    val selectedOptions: List<Int>,
    @TypeConverters(IntListConverter::class)
    val showResultList: List<Int>,
    val finished: Boolean,
    val timestamp: Long
)