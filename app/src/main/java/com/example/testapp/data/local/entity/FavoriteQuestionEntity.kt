package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_questions")
data class FavoriteQuestionEntity(
    @PrimaryKey val questionId: Int,
    val questionJson: String // 用 JSON 存储完整 Question
)
