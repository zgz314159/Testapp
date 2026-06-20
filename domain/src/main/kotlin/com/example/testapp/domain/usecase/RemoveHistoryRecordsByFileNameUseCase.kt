package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.HistoryRepository
import javax.inject.Inject

class RemoveHistoryRecordsByFileNameUseCase @Inject constructor(
    private val repo: HistoryRepository
) {
    suspend operator fun invoke(fileName: String) = repo.removeByFileName(fileName)
}