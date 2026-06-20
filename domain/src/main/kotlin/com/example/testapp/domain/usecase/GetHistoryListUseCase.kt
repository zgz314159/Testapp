package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.HistoryRepository
import javax.inject.Inject

class GetHistoryListUseCase @Inject constructor(
    private val repo: HistoryRepository
) {
    operator fun invoke() = repo.getAll()
}
