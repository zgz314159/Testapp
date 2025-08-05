package com.example.testapp.domain.model

import kotlinx.serialization.Serializable
import androidx.room.PrimaryKey
import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.testapp.data.local.entity.converter.IntListConverter

@Serializable
@Entity(tableName = "questions")
@TypeConverters(IntListConverter::class) // 确保使用正确的转换器
data class Question(
@PrimaryKey(autoGenerate = true) val id: Int = 0,
val content: String,
val type: String, // 判断题、单选题、多选题
val options: List<String>,
val answer: String,
val explanation: String,
val isFavorite: Boolean = false,
val isWrong: Boolean = false,
val fileName: String? = null // <-- 新增字段，用于关联导入的文件
)
