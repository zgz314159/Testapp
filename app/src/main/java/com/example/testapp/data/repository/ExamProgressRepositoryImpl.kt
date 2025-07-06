package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.ExamProgressDao
import com.example.testapp.data.local.entity.ExamProgressEntity
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.repository.ExamProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExamProgressRepositoryImpl @Inject constructor(
    private val dao: ExamProgressDao
) : ExamProgressRepository {
    override suspend fun saveProgress(progress: ExamProgress) {
        dao.upsertProgress(progress.toEntity())
    }

    override fun getProgressFlow(id: String): Flow<ExamProgress?> {
        return dao.getProgressFlow(id).map { it?.toDomain() }
    }

    override suspend fun clearProgress(id: String) {
        dao.deleteProgress(id)
    }
}