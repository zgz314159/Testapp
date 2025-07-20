package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_ask")
data class QuestionAskEntity(
    @PrimaryKey val questionId: Int,
    val deepSeekResult: String = "",
    val sparkResult: String = ""
)