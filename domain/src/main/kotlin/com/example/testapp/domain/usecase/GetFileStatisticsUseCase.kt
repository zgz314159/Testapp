package com.example.testapp.domain.usecase

import com.example.testapp.domain.repository.FileStatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFileStatisticsUseCase @Inject constructor(
    private val repository: FileStatisticsRepository
) {
    operator fun invoke(): Flow<Map<String, FileStatistics>> = repository.observeFileStatistics()
}
