package com.example.testapp.domain.model

import java.time.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable

data class HistoryRecord(
    val score: Int,
    val total: Int,
    val unanswered: Int = 0,
    val fileName: String? = null,
    @Contextual
    val time: LocalDateTime = LocalDateTime.now()
    // 暂时移除mode字段
    // val mode: String = "practice"  // 新增：区分练习("practice")和考试("exam")模式
)
