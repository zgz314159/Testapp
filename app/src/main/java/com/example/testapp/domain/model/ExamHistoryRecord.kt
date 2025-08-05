package com.example.testapp.domain.model

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ExamHistoryRecord(
    val score: Int,        // 本次考试答对数
    val total: Int,        // 本次考试总题数
    val unanswered: Int,   // 本次考试未答数
    val fileName: String?, // 题库文件名
    @Contextual
    val time: LocalDateTime = LocalDateTime.now(), // 考试完成时间
    val duration: Int = 0, // 考试用时（秒）
    val examType: String = "normal", // 考试类型：normal/wrong_book/favorite
    val examId: String = ""  // 考试ID，用于关联ExamProgressEntity
)
