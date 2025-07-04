package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wrong_questions")
data class WrongQuestionEntity(
    @PrimaryKey val questionId: Int,
    val selected: Int
)
