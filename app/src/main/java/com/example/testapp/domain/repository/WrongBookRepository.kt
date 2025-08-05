package com.example.testapp.domain.repository

import com.example.testapp.domain.model.WrongQuestion
import kotlinx.coroutines.flow.Flow

interface WrongBookRepository {
    fun getAll(): Flow<List<WrongQuestion>>
    suspend fun add(wrong: WrongQuestion)
    suspend fun clear()
    suspend fun importFromFile(file: java.io.File): Int
    suspend fun exportToFile(file: java.io.File): Boolean
    // 新增：按文件名批量删除错题
    suspend fun removeByFileName(fileName: String)

}
