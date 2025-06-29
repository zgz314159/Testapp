package com.example.testapp.domain.repository

import com.example.testapp.domain.model.WrongQuestion
import kotlinx.coroutines.flow.Flow

interface WrongBookRepository {
    fun getAll(): Flow<List<WrongQuestion>>
    suspend fun add(wrong: WrongQuestion)
    suspend fun clear()
    suspend fun importFromFile(file: java.io.File): Int
    suspend fun exportToFile(file: java.io.File): Boolean
}
