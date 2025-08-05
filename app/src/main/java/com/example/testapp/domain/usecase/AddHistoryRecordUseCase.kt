package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.repository.HistoryRepository
import javax.inject.Inject

class AddHistoryRecordUseCase @Inject constructor(
    private val repo: HistoryRepository
) {
    suspend operator fun invoke(record: HistoryRecord) = repo.add(record)
}
