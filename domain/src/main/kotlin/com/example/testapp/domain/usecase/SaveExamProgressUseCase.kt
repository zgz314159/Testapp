package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.repository.ExamProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveExamProgressUseCase @Inject constructor(
    private val repository: ExamProgressRepository
) { suspend operator fun invoke(progress: ExamProgress) = repository.saveProgress(progress) }

class GetExamProgressFlowUseCase @Inject constructor(
    private val repository: ExamProgressRepository
) { operator fun invoke(id: String = "exam_default"): Flow<ExamProgress?> = repository.getProgressFlow(id) }

class ClearExamProgressUseCase @Inject constructor(
    private val repository: ExamProgressRepository
) { suspend operator fun invoke(id: String = "exam_default") = repository.clearProgress(id) }

class ClearExamProgressByFileNameUseCase @Inject constructor(
    private val repository: ExamProgressRepository
) { 
    suspend operator fun invoke(fileName: String) {
        val pattern = "exam_${fileName}%"
        
        repository.clearProgressByFileNamePattern(pattern)
        
    }
}