package com.example.testapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteQuestionRepository {
    fun getAll(): Flow<List<Int>> // 收藏题目ID列表
    suspend fun add(questionId: Int)
    suspend fun remove(questionId: Int)
    suspend fun isFavorite(questionId: Int): Boolean
}
