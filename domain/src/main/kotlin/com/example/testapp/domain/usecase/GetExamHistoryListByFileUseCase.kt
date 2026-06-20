package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.ExamHistoryRecord
import com.example.testapp.domain.repository.ExamHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExamHistoryListByFileUseCase @Inject constructor(
    private val repository: ExamHistoryRepository
) {
    operator fun invoke(fileName: String): Flow<List<ExamHistoryRecord>> {
        return repository.getByFileName(fileName)
    }
}
