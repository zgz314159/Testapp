package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.ExamHistoryRecordDao
import com.example.testapp.data.local.entity.ExamHistoryRecordEntity
import com.example.testapp.domain.model.ExamHistoryRecord
import java.time.Instant
import java.time.ZoneId
import com.example.testapp.domain.repository.ExamHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExamHistoryRepositoryImpl @Inject constructor(
    private val dao: ExamHistoryRecordDao
) : ExamHistoryRepository {
    override suspend fun add(record: ExamHistoryRecord) {
        val millis = record.time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        dao.insert(ExamHistoryRecordEntity(
            score = record.score,
            total = record.total,
            unanswered = record.unanswered,
            fileName = record.fileName,
            time = millis,
            duration = record.duration,
            examType = record.examType,
            examId = record.examId
        ))
    }

    override fun getAll(): Flow<List<ExamHistoryRecord>> = dao.getAll().map { list ->
        list.map {
            ExamHistoryRecord(
                score = it.score,
                total = it.total,
                unanswered = it.unanswered,
                fileName = it.fileName,
                time = Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                duration = it.duration,
                examType = it.examType,
                examId = it.examId
            )
        }
    }
    override suspend fun removeByFileName(fileName: String) {
        dao.deleteByFileName(fileName)
    }

    override fun getByFileName(fileName: String): Flow<List<ExamHistoryRecord>> =
        dao.getAll().map { list -> list.filter { it.fileName == fileName }.map {
            ExamHistoryRecord(
                score = it.score,
                total = it.total,
                unanswered = it.unanswered,
                fileName = it.fileName,
                time = Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                duration = it.duration,
                examType = it.examType,
                examId = it.examId
            )
        } }

    override fun getByFileNames(fileNames: List<String>): Flow<List<ExamHistoryRecord>> =
        dao.getAll().map { list -> list.filter { it.fileName in fileNames }.map {
            ExamHistoryRecord(
                score = it.score,
                total = it.total,
                unanswered = it.unanswered,
                fileName = it.fileName,
                time = Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                duration = it.duration,
                examType = it.examType,
                examId = it.examId
            )
        } }

    override fun getByExamType(examType: String): Flow<List<ExamHistoryRecord>> =
        dao.getAll().map { list -> list.filter { it.examType == examType }.map {
            ExamHistoryRecord(
                score = it.score,
                total = it.total,
                unanswered = it.unanswered,
                fileName = it.fileName,
                time = Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                duration = it.duration,
                examType = it.examType,
                examId = it.examId
            )
        } }

    override fun getByFileNameAndExamType(fileName: String, examType: String): Flow<List<ExamHistoryRecord>> =
        dao.getAll().map { list -> list.filter { it.fileName == fileName && it.examType == examType }.map {
            ExamHistoryRecord(
                score = it.score,
                total = it.total,
                unanswered = it.unanswered,
                fileName = it.fileName,
                time = Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                duration = it.duration,
                examType = it.examType,
                examId = it.examId
            )
        } }

    override suspend fun clear() {
        // no-op: clear all history is not implemented at DAO level; can be added if needed
    }

    override suspend fun removeByExamType(examType: String) {
        // Not implemented in DAO; implement if needed
    }

    override suspend fun removeByFileNameAndExamType(fileName: String, examType: String) {
        // Not implemented in DAO; implement if needed
    }
}
