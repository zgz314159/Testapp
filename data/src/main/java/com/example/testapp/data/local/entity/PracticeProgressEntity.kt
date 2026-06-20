package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testapp.data.local.entity.converter.BooleanListConverter
import com.example.testapp.data.local.entity.converter.IntListConverter
import com.example.testapp.data.local.entity.converter.NestedIntListConverter
import com.example.testapp.data.local.entity.converter.StringListConverter

@Entity(tableName = "practice_progress")
data class PracticeProgressEntity(
    @PrimaryKey val id: String = "practice_default",
    val currentIndex: Int,
    @TypeConverters(IntListConverter::class)
    val answeredList: List<Int>,
    @TypeConverters(NestedIntListConverter::class)
    val selectedOptions: List<List<Int>>,
    @TypeConverters(BooleanListConverter::class)
    val showResultList: List<Boolean>,
    @TypeConverters(StringListConverter::class)
    val analysisList: List<String>,
    @TypeConverters(StringListConverter::class)
    val sparkAnalysisList: List<String> = emptyList(),
    @TypeConverters(StringListConverter::class)
    val baiduAnalysisList: List<String> = emptyList(),
    @TypeConverters(StringListConverter::class)
    val noteList: List<String>,
    val timestamp: Long,
    val sessionId: String = "",
    @TypeConverters(IntListConverter::class)
    val fixedQuestionOrder: List<Int> = emptyList(),
    val questionStateJson: String = ""
)
