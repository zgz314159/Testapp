package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_analysis")
data class QuestionAnalysisEntity(
    @PrimaryKey val questionId: Int,
    val analysis: String
)