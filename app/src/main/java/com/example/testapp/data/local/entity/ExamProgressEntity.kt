package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testapp.data.local.entity.converter.BooleanListConverter
import com.example.testapp.data.local.entity.converter.IntListConverter
import com.example.testapp.data.local.entity.converter.NestedIntListConverter


@Entity(tableName = "exam_progress")
data class ExamProgressEntity(
    @PrimaryKey val id: String = "default",
    val currentIndex: Int,
    @TypeConverters(NestedIntListConverter::class)
    val selectedOptions: List<List<Int>>,
    @TypeConverters(BooleanListConverter::class)
    val showResultList: List<Boolean>,
    val finished: Boolean,
    val timestamp: Long
)