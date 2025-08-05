package com.example.testapp.domain.repository

import com.example.testapp.domain.model.ExamHistoryRecord
import kotlinx.coroutines.flow.Flow

interface ExamHistoryRepository {
    fun getAll(): Flow<List<ExamHistoryRecord>>
    fun getByFileName(fileName: String): Flow<List<ExamHistoryRecord>>
    fun getByFileNames(fileNames: List<String>): Flow<List<ExamHistoryRecord>>
    fun getByExamType(examType: String): Flow<List<ExamHistoryRecord>>
    fun getByFileNameAndExamType(fileName: String, examType: String): Flow<List<ExamHistoryRecord>>
    
    suspend fun add(record: ExamHistoryRecord)
    suspend fun clear()
    suspend fun removeByFileName(fileName: String)
    suspend fun removeByExamType(examType: String)
    suspend fun removeByFileNameAndExamType(fileName: String, examType: String)
}
