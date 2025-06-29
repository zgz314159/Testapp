package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.testapp.data.local.entity.converter.BooleanListConverter
import com.example.testapp.data.local.entity.converter.IntListConverter

@Entity(tableName = "practice_progress")
data class PracticeProgressEntity(
    @PrimaryKey val id: String = "default", // 可根据题库唯一标识扩展
    val currentIndex: Int,
    @TypeConverters(IntListConverter::class)
    val answeredList: List<Int>, // 已答题目下标
    @TypeConverters(IntListConverter::class)
    val selectedOptions: List<Int>, // 选项序号，按题目顺序存储
    @TypeConverters(IntListConverter::class)
    val showResultList: List<Int>, // 用Int代替Boolean，0=未提交，1=已提交
    val timestamp: Long // 保存时间戳
)
