package com.example.testapp.domain.repository

import com.example.testapp.domain.usecase.FileStatistics
import kotlinx.coroutines.flow.Flow

interface FileStatisticsRepository {
    fun observeFileStatistics(): Flow<Map<String, FileStatistics>>
}
