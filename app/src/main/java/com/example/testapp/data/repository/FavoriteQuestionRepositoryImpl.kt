package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FavoriteQuestionRepositoryImpl @Inject constructor(
    private val dao: FavoriteQuestionDao
) : FavoriteQuestionRepository {
    override fun getAll(): Flow<List<FavoriteQuestion>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }


    override suspend fun add(favorite: FavoriteQuestion) =
        dao.add(favorite.toEntity())

    override suspend fun remove(questionId: Int) =
        dao.removeById(questionId)

    override suspend fun isFavorite(questionId: Int): Boolean =
        dao.getAll().map { list -> list.any { it.questionId == questionId } }.firstOrNull() ?: false
    private suspend fun getAllSuspend(): List<FavoriteQuestion> {
        val entities = dao.getAll().firstOrNull() ?: emptyList()
        return entities.map { it.toDomain() }
    }

    override suspend fun importFromFile(file: java.io.File): Int {
        return try {
            val content = file.readText()
            val favorites = Json.decodeFromString<List<FavoriteQuestion>>(content)
            favorites.forEach { dao.add(it.toEntity()) }
            favorites.size
        } catch (e: Exception) { 0 }
    }

    override suspend fun exportToFile(file: java.io.File): Boolean {
        return try {
            val favorites = getAllSuspend()
            val json = Json.encodeToString(favorites)
            file.writeText(json)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun removeByFileName(fileName: String) {
        val all = dao.getAll().firstOrNull().orEmpty()
        val json = kotlinx.serialization.json.Json
        all.forEach { entity ->
            // 反序列化 Question
            val question = try {
                json.decodeFromString<com.example.testapp.domain.model.Question>(entity.questionJson)
            } catch (e: Exception) { null }
            if (question != null && question.fileName == fileName) {
                dao.removeById(question.id)
            }
        }
    }


}

