package com.example.testapp.data.mappers

import com.example.testapp.data.local.entity.ExamHistoryRecordEntity
import com.example.testapp.domain.model.ExamHistoryRecord

fun ExamHistoryRecord.toEntity(): ExamHistoryRecordEntity {
    return ExamHistoryRecordEntity(
        score = score,
        total = total,
        unanswered = unanswered,
        fileName = fileName,
        time = time.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
        duration = duration,
        examType = examType,
        examId = examId
    )
}

fun ExamHistoryRecordEntity.toDomain(): ExamHistoryRecord {
    return ExamHistoryRecord(
        score = score,
        total = total,
        unanswered = unanswered,
        fileName = fileName,
        time = java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(time),
            java.time.ZoneId.systemDefault()
        ),
        duration = duration,
        examType = examType,
        examId = examId
    )
}
