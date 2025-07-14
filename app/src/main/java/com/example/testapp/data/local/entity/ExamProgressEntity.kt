package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testapp.data.local.entity.converter.BooleanListConverter
import com.example.testapp.data.local.entity.converter.IntListConverter
import com.example.testapp.data.local.entity.converter.NestedIntListConverter
import com.example.testapp.data.local.entity.converter.StringListConverter


@Entity(tableName = "exam_progress")
data class ExamProgressEntity(
    @PrimaryKey val id: String = "exam_default",
    val currentIndex: Int,
    @TypeConverters(NestedIntListConverter::class)
    val selectedOptions: List<List<Int>>,
    @TypeConverters(BooleanListConverter::class)
    val showResultList: List<Boolean>,
    @TypeConverters(StringListConverter::class)
    val analysisList: List<String>,
    @TypeConverters(StringListConverter::class)
    val noteList: List<String>,
    val finished: Boolean,
    val timestamp: Long
)