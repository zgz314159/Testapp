package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testapp.data.local.entity.converter.BooleanListConverter
import com.example.testapp.data.local.entity.converter.IntListConverter
import com.example.testapp.data.local.entity.converter.NestedIntListConverter
import com.example.testapp.data.local.entity.converter.StringListConverter
import com.example.testapp.data.local.entity.converter.ExamQuestionStateMapConverter
import com.example.testapp.domain.model.ExamQuestionState

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
    val sparkAnalysisList: List<String> = emptyList(),
    @TypeConverters(StringListConverter::class)
    val baiduAnalysisList: List<String> = emptyList(),
    @TypeConverters(StringListConverter::class)
    val noteList: List<String>,
    val finished: Boolean,
    val timestamp: Long,
    // 🚀 新增：固定题序支持字段
    val sessionId: String = "", // 会话ID，用于区分不同轮次的考试
    @TypeConverters(IntListConverter::class)
    val fixedQuestionOrder: List<Int> = emptyList(), // 固定的题目ID顺序
    @TypeConverters(ExamQuestionStateMapConverter::class)
    val questionStateMap: Map<Int, ExamQuestionState> = emptyMap() // 题目ID -> 答题状态映射
)