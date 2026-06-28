package com.example.testapp.domain.repository

import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.LibraryCatalog
import kotlinx.coroutines.flow.Flow

interface FavoriteQuestionRepository {
    fun getAll(): Flow<List<FavoriteQuestion>> // 收藏题目完整对象列表
    fun observeLibraryCatalog(): Flow<LibraryCatalog>
    suspend fun add(favorite: FavoriteQuestion)
    suspend fun remove(questionId: Int)
    suspend fun isFavorite(questionId: Int): Boolean
    suspend fun importFromFile(file: java.io.File): Int
    suspend fun exportToFile(file: java.io.File): Boolean
    suspend fun removeByFileName(fileName: String)

}
