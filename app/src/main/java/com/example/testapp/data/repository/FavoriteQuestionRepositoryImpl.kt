package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteQuestionRepositoryImpl @Inject constructor(
    private val dao: FavoriteQuestionDao

) : FavoriteQuestionRepository {
    override fun getAll(): Flow<List<Int>> = dao.getAll().map { it.map { e -> e.questionId } }
    override suspend fun add(questionId: Int) = dao.add(FavoriteQuestionEntity(questionId))
    override suspend fun remove(questionId: Int) = dao.remove(FavoriteQuestionEntity(questionId))
    override suspend fun isFavorite(questionId: Int): Boolean =
        dao.getAll().map { list -> list.any { it.questionId == questionId } }.firstOrNull() ?: false
}
