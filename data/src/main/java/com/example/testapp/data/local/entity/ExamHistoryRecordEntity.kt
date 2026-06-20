package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_history_records")
data class ExamHistoryRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val total: Int,
    val unanswered: Int,
    val fileName: String?,
    val time: Long,
    val duration: Int = 0,
    val examType: String = "normal",
    val examId: String = ""
)
