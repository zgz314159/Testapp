package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val type: String,
    val options: String, // 存储为JSON字符串
    val answer: String,
    val explanation: String,
    val isFavorite: Boolean = false,
    val isWrong: Boolean = false,
    val fileName: String? = null
)
