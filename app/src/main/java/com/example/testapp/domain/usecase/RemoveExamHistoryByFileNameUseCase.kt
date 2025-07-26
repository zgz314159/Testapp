package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.ExamHistoryRepository
import javax.inject.Inject

class RemoveExamHistoryByFileNameUseCase @Inject constructor(
    private val repository: ExamHistoryRepository
) {
    suspend operator fun invoke(fileName: String) {
        repository.removeByFileName(fileName)
    }
}
