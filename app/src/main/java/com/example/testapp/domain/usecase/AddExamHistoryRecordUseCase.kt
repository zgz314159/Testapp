package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.ExamHistoryRecord
import com.example.testapp.domain.repository.ExamHistoryRepository
import javax.inject.Inject

class AddExamHistoryRecordUseCase @Inject constructor(
    private val repository: ExamHistoryRepository
) {
    suspend operator fun invoke(record: ExamHistoryRecord) {
        repository.add(record)
    }
}
