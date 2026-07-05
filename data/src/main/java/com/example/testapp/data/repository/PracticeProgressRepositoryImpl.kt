package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.PracticeProgressDao
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.repository.PracticeProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PracticeProgressRepositoryImpl @Inject constructor(
    private val dao: PracticeProgressDao
) : PracticeProgressRepository {
    override suspend fun saveProgress(progress: PracticeProgress) {
        dao.upsertProgress(progress.toEntity())
    }

    override fun getAllProgressFlow(): Flow<List<PracticeProgress>> {
        return dao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override fun getProgressFlow(id: String): Flow<PracticeProgress?> {
        return dao.getProgressFlow(id).map { it?.toDomain() }
    }

    override suspend fun clearProgress(id: String) {

        dao.deleteProgress(id)

    }

    override suspend fun clearProgressByFileNamePattern(fileNamePattern: String) {
        dao.deleteProgressByFileNamePattern(fileNamePattern)
    }
}
