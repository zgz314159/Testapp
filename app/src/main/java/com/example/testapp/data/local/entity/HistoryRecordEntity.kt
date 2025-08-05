package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_records")
data class HistoryRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val total: Int,
    val unanswered: Int,
    val fileName: String?,
    val time: Long // 时间戳
    // 暂时移除mode字段
    // val mode: String = "practice" // 新增：区分练习("practice")和考试("exam")模式
)
