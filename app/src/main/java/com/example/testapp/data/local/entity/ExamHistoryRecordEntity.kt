package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_history_records")
data class ExamHistoryRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,        // 本次考试答对数
    val total: Int,        // 本次考试总题数
    val unanswered: Int,   // 本次考试未答数
    val fileName: String?, // 题库文件名
    val time: Long,        // 考试完成时间戳
    val duration: Int = 0, // 考试用时（秒）
    val examType: String = "normal", // 考试类型：normal/wrong_book/favorite
    val examId: String = ""  // 考试ID，用于关联ExamProgressEntity
)
