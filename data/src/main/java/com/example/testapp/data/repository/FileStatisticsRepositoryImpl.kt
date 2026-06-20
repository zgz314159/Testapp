package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.WrongQuestionDao
import com.example.testapp.domain.repository.FileStatisticsRepository
import com.example.testapp.domain.usecase.FileStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileStatisticsRepositoryImpl @Inject constructor(
    private val questionDao: QuestionDao,
    private val wrongQuestionDao: WrongQuestionDao,
    private val favoriteQuestionDao: FavoriteQuestionDao
) : FileStatisticsRepository {
    override fun observeFileStatistics(): Flow<Map<String, FileStatistics>> =
        combine(
            questionDao.getFileQuestionCounts(),
            questionDao.getFileQuestionTypeCounts(),
            wrongQuestionDao.getCountsByFileName(),
            favoriteQuestionDao.getCountsByFileName()
        ) { questionCounts, typeCounts, wrongCounts, favoriteCounts ->
            FileStatisticsAggregateMapper.assemble(
                questionCounts = questionCounts,
                typeCounts = typeCounts,
                wrongCounts = wrongCounts,
                favoriteCounts = favoriteCounts
            )
        }.flowOn(Dispatchers.Default)
}
