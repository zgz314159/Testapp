package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.FavoriteFileCountRow
import com.example.testapp.data.local.dao.FileQuestionTypeCountRow
import com.example.testapp.data.local.dao.FileRelatedCountRow
import com.example.testapp.domain.model.LibraryCatalog
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.domain.usecase.QuestionTypeStat
import com.example.testapp.domain.util.canonicalQuestionType

object ScopedLibraryCatalogPipeline {
    fun buildWrongBookCatalog(
        counts: List<FileRelatedCountRow>,
        typeCounts: List<FileQuestionTypeCountRow>
    ): LibraryCatalog = buildScopedCatalog(
        counts = counts.map { it.fileName to it.count },
        typeCounts = typeCounts,
        wrongCounts = counts.associate { it.fileName to it.count },
        favoriteCounts = emptyMap()
    )

    fun buildFavoriteCatalog(
        counts: List<FavoriteFileCountRow>,
        typeCounts: List<FileQuestionTypeCountRow>
    ): LibraryCatalog = buildScopedCatalog(
        counts = counts.map { it.fileName to it.count },
        typeCounts = typeCounts,
        wrongCounts = emptyMap(),
        favoriteCounts = counts.associate { it.fileName to it.count }
    )

    private fun buildScopedCatalog(
        counts: List<Pair<String, Int>>,
        typeCounts: List<FileQuestionTypeCountRow>,
        wrongCounts: Map<String, Int>,
        favoriteCounts: Map<String, Int>
    ): LibraryCatalog {
        val typeStatsByFile = typeCounts
            .groupBy { it.fileName }
            .mapValues { (_, rows) ->
                rows
                    .groupBy { canonicalQuestionType(it.type) }
                    .map { (type, grouped) ->
                        QuestionTypeStat(type = type, count = grouped.sumOf { row -> row.count })
                    }
                    .sortedWith(compareByDescending<QuestionTypeStat> { it.count }.thenBy { it.type })
            }
        val fileNames = counts
            .map { it.first }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
        val fileStatistics = fileNames.associateWith { fileName ->
            val itemCount = counts.firstOrNull { it.first == fileName }?.second ?: 0
            val typeStats = typeStatsByFile[fileName].orEmpty()
            FileStatistics(
                questionCount = itemCount,
                wrongCount = wrongCounts[fileName] ?: 0,
                favoriteCount = favoriteCounts[fileName] ?: 0,
                primaryQuestionType = typeStats.firstOrNull()?.type.orEmpty(),
                questionTypeStats = typeStats
            )
        }
        return LibraryCatalog(fileNames = fileNames, fileStatistics = fileStatistics)
    }
}
