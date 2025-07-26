package com.example.testapp.domain.repository

import com.example.testapp.domain.model.HistoryRecord
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAll(): Flow<List<HistoryRecord>>
    fun getByFileName(fileName: String): Flow<List<HistoryRecord>>
    fun getByFileNames(fileNames: List<String>): Flow<List<HistoryRecord>>
    
    // 暂时注释掉包含mode的方法
    // fun getByMode(mode: String): Flow<List<HistoryRecord>>
    // fun getByFileNameAndMode(fileName: String, mode: String): Flow<List<HistoryRecord>>
    
    suspend fun add(record: HistoryRecord)
    suspend fun clear()
    suspend fun removeByFileName(fileName: String)
    
    // 暂时注释掉包含mode的删除方法
    // suspend fun removeByMode(mode: String)
    // suspend fun removeByFileNameAndMode(fileName: String, mode: String)
    
    suspend fun importFromFile(file: java.io.File): Int
    suspend fun exportToFile(file: java.io.File): Boolean
}
