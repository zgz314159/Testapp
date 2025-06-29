package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_questions")
data class FavoriteQuestionEntity(
    @PrimaryKey val questionId: Int
)
