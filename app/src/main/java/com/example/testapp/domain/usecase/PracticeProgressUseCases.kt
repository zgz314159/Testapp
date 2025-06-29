package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.repository.PracticeProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SavePracticeProgressUseCase @Inject constructor(
    private val repository: PracticeProgressRepository
) {
    suspend operator fun invoke(progress: PracticeProgress) = repository.saveProgress(progress)
}

class GetPracticeProgressFlowUseCase @Inject constructor(
    private val repository: PracticeProgressRepository
) {
    operator fun invoke(id: String = "default"): Flow<PracticeProgress?> = repository.getProgressFlow(id)
}

class ClearPracticeProgressUseCase @Inject constructor(
    private val repository: PracticeProgressRepository
) {
    suspend operator fun invoke(id: String = "default") = repository.clearProgress(id)
}
