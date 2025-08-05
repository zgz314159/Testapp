package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_notes")
data class QuestionNoteEntity(
    @PrimaryKey val questionId: Int,
    val note: String
)