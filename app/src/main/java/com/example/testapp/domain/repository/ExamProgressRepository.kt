package com.example.testapp.domain.repository

import com.example.testapp.domain.model.ExamProgress
import kotlinx.coroutines.flow.Flow

interface ExamProgressRepository {
    suspend fun saveProgress(progress: ExamProgress)
    fun getProgressFlow(id: String = "exam_default"): Flow<ExamProgress?>
    suspend fun clearProgress(id: String = "exam_default")
}
