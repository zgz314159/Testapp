package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.FavoriteFileCountRow
import com.example.testapp.data.local.dao.FileQuestionCountRow
import com.example.testapp.data.local.dao.FileQuestionTypeCountRow
import com.example.testapp.data.local.dao.FileRelatedCountRow
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.domain.usecase.QuestionTypeStat
import com.example.testapp.domain.util.canonicalQuestionType

object FileStatisticsAggregateMapper {
    fun assemble(
        questionCounts: List<FileQuestionCountRow>,
        typeCounts: List<FileQuestionTypeCountRow>,
        wrongCounts: List<FileRelatedCountRow>,
        favoriteCounts: List<FavoriteFileCountRow>
    ): Map<String, FileStatistics> {
        val questionCountByFile = questionCounts.associate { it.fileName to it.questionCount }
        val wrongCountByFile = wrongCounts.associate { it.fileName to it.count }
        val favoriteCountByFile = favoriteCounts.associate { it.fileName to it.count }
        val typeStatsByFile = typeCounts
            .groupBy { it.fileName }
            .mapValues { (_, rows) ->
                rows
                    .groupBy { canonicalQuestionType(it.type) }
                    .map { (type, grouped) -> QuestionTypeStat(type = type, count = grouped.sumOf { row -> row.count }) }
                    .sortedWith(compareByDescending<QuestionTypeStat> { it.count }.thenBy { it.type })
            }

        return (questionCountByFile.keys + wrongCountByFile.keys + favoriteCountByFile.keys)
            .filter { it.isNotBlank() }
            .associateWith { fileName ->
                val typeStats = typeStatsByFile[fileName].orEmpty()
                FileStatistics(
                    questionCount = questionCountByFile[fileName] ?: 0,
                    wrongCount = wrongCountByFile[fileName] ?: 0,
                    favoriteCount = favoriteCountByFile[fileName] ?: 0,
                    primaryQuestionType = typeStats.firstOrNull()?.type.orEmpty(),
                    questionTypeStats = typeStats
                )
            }
    }
}
