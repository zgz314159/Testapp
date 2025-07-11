package com.example.testapp.domain.repository

import com.example.testapp.domain.model.PracticeProgress
import kotlinx.coroutines.flow.Flow

interface PracticeProgressRepository {
    suspend fun saveProgress(progress: PracticeProgress)
    fun getProgressFlow(id: String = "practice_default"): Flow<PracticeProgress?>
    suspend fun clearProgress(id: String = "practice_default")
}
