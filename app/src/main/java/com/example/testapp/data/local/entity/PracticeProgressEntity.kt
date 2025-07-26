package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testapp.data.local.entity.converter.BooleanListConverter
import com.example.testapp.data.local.entity.converter.IntListConverter
import com.example.testapp.data.local.entity.converter.NestedIntListConverter
import com.example.testapp.data.local.entity.converter.StringListConverter
import com.example.testapp.data.local.entity.converter.QuestionAnswerStateMapConverter
import com.example.testapp.domain.model.QuestionAnswerState

@Entity(tableName = "practice_progress")
data class PracticeProgressEntity(
    @PrimaryKey val id: String = "practice_default", // 可根据题库唯一标识扩展
    val currentIndex: Int,
    @TypeConverters(IntListConverter::class)
    val answeredList: List<Int>, // 已答题目下标
    @TypeConverters(NestedIntListConverter::class)
    val selectedOptions: List<List<Int>>, // 支持多选的选项序号
    @TypeConverters(BooleanListConverter::class) // <--- 修改为BooleanListConverter
    val showResultList: List<Boolean>,           // <--- 类型直接用Boolean
    @TypeConverters(StringListConverter::class)
    val analysisList: List<String>,
    @TypeConverters(StringListConverter::class)
    val sparkAnalysisList: List<String> = emptyList(),
    @TypeConverters(StringListConverter::class)
    val baiduAnalysisList: List<String> = emptyList(),
    @TypeConverters(StringListConverter::class)
    val noteList: List<String>, // 每题的笔记内容
    val timestamp: Long, // 保存时间戳
    // 🚀 新增：固定题序支持字段
    val sessionId: String = "", // 会话ID，用于区分不同轮次的练习
    @TypeConverters(IntListConverter::class)
    val fixedQuestionOrder: List<Int> = emptyList(), // 固定的题目ID顺序
    @TypeConverters(QuestionAnswerStateMapConverter::class)
    val questionStateMap: Map<Int, QuestionAnswerState> = emptyMap() // 题目ID -> 答题状态映射
)
