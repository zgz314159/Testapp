package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.ExamHistoryRecordDao
import com.example.testapp.data.mappers.toDomain
import com.example.testapp.data.mappers.toEntity
import com.example.testapp.domain.model.ExamHistoryRecord
import com.example.testapp.domain.repository.ExamHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExamHistoryRepositoryImpl @Inject constructor(
    private val dao: ExamHistoryRecordDao
) : ExamHistoryRepository {
    
    override fun getAll(): Flow<List<ExamHistoryRecord>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByFileName(fileName: String): Flow<List<ExamHistoryRecord>> =
        dao.getByFileName(fileName).map { list -> list.map { it.toDomain() } }

    override fun getByFileNames(fileNames: List<String>): Flow<List<ExamHistoryRecord>> =
        dao.getByFileNames(fileNames).map { list -> list.map { it.toDomain() } }

    override fun getByExamType(examType: String): Flow<List<ExamHistoryRecord>> =
        dao.getByExamType(examType).map { list -> list.map { it.toDomain() } }

    override fun getByFileNameAndExamType(fileName: String, examType: String): Flow<List<ExamHistoryRecord>> =
        dao.getByFileNameAndExamType(fileName, examType).map { list -> list.map { it.toDomain() } }

    override suspend fun add(record: ExamHistoryRecord) {
        dao.add(record.toEntity())
    }

    override suspend fun clear() = dao.clear()

    override suspend fun removeByFileName(fileName: String) {
        dao.deleteByFileName(fileName)
    }

    override suspend fun removeByExamType(examType: String) {
        dao.deleteByExamType(examType)
    }

    override suspend fun removeByFileNameAndExamType(fileName: String, examType: String) {
        dao.deleteByFileNameAndExamType(fileName, examType)
    }
}
