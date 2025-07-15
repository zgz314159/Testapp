package com.example.testapp.domain.repository

import com.example.testapp.domain.model.HistoryRecord
import kotlinx.coroutines.flow.Flow


interface HistoryRepository {
    fun getAll(): Flow<List<HistoryRecord>>
    fun getByFileName(fileName: String): Flow<List<HistoryRecord>>
    fun getByFileNames(fileNames: List<String>): Flow<List<HistoryRecord>>
    suspend fun add(record: HistoryRecord)
    suspend fun clear()
    suspend fun importFromFile(file: java.io.File): Int
    suspend fun exportToFile(file: java.io.File): Boolean
}
