package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.HistoryRepository
import javax.inject.Inject

class GetHistoryListByFileNamesUseCase @Inject constructor(
    private val repo: HistoryRepository
) {
    operator fun invoke(fileNames: List<String>) = repo.getByFileNames(fileNames)
}