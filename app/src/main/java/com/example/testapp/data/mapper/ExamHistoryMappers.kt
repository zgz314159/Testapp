package com.example.testapp.data.mapper

import com.example.testapp.data.local.entity.ExamHistoryRecordEntity
import com.example.testapp.domain.model.ExamHistoryRecord
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// ExamHistoryRecord映射器
fun ExamHistoryRecordEntity.toDomain() = ExamHistoryRecord(
    score = score,
    total = total,
    unanswered = unanswered,
    fileName = fileName,
    time = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()),
    duration = duration,
    examType = examType,
    examId = examId
)

fun ExamHistoryRecord.toEntity() = ExamHistoryRecordEntity(
    score = score,
    total = total,
    unanswered = unanswered,
    fileName = fileName,
    time = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    duration = duration,
    examType = examType,
    examId = examId
)
